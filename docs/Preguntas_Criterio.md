# Criterios de Decisión: Estructuras de Datos en la Red de Sensores

Este documento es la parte más importante de tu entrega. No basta con que el código funcione; debes justificar **por qué** elegiste una colección de Java sobre otra.

Responde a las siguientes preguntas basándote en la teoría de nodos y el rendimiento del JCF.

### 1. El dilema del Historial
Para el `historialEventos`, podrías usar un `ArrayList` o un `LinkedList`. 
- Si la plataforma registra miles de eventos por segundo y necesitas eliminar los más antiguos frecuentemente para ahorrar memoria, ¿cuál de las dos colecciones es más eficiente y por qué? (Pista: piensa en el costo de eliminar el primer elemento).

**Respuesta (pregunta 1):** `LinkedList` es más eficiente para eliminar el primer elemento. `ArrayList.remove(0)` tiene que desplazar una posición todos los elementos restantes (O(n)), mientras que `LinkedList.removeFirst()` solo mueve la referencia al primer nodo (O(1)). Con miles de eventos por segundo y borrados frecuentes de los más antiguos, el `ArrayList` hace trabajo proporcional al tamaño del historial en cada borrado. La contrapartida es que `LinkedList` gasta más memoria (un nodo con dos referencias por elemento) y no tiene acceso directo por posición. En este proyecto el historial se mantiene como `ArrayList` porque predominan agregar al final y leer los más recientes, y el borrado se hace de una sola vez con límite. Si el borrado fuera el caso dominante, la mejor opción sería una estructura con borrado O(1) al inicio, como `ArrayDeque`.

### 2. Navegación Bidireccional
En el `navegadorAlertas`, utilizamos un `Deque` (Double Ended Queue). 
- ¿Qué ventaja ofrece un `Deque` sobre una `Queue` simple para implementar la funcionalidad de "Atrás" y "Adelante"? 
- ¿Cómo se relaciona esto con el concepto de un `NodoDoble`?

**Respuesta (pregunta 2):** Una `Queue` solo permite insertar por un extremo y sacar por el otro, así que no sirve para ir y volver. Un `Deque` permite insertar y quitar por los dos extremos en O(1), y eso es justo lo que necesitan "Adelante" (sacar del frente y reinsertar al final) y "Atrás" (sacar del final y reinsertar al frente). Se relaciona con `NodoDoble` porque un nodo con referencias `anterior` y `siguiente`, junto con punteros a `primero` y `ultimo`, es lo que permite operar por ambos extremos sin recorrer la lista; `LinkedList` implementa `Deque` así, y `ArrayDeque` ofrece el mismo contrato con un arreglo circular. Es la misma idea de `HistorialNavegable` de la Semana 5.

### 3. El Ciclo de Monitoreo
Para la `colaMonitoreo`, implementaste una rotación (sacar del frente y meter al final).
- ¿Por qué una `Queue` es la estructura natural para este problema? 
- ¿Qué pasaría con la equidad del sistema si usaras una `Stack` (Pila) en su lugar?

**Respuesta (pregunta 3):** La `Queue` es la estructura natural porque el primer sensor programado debe ser el primero consultado (FIFO) y, con la rotación (sacar del frente y reinsertar al final), todos los sensores reciben su turno por igual. Con una `Stack` (LIFO) saldría siempre el último sensor programado y los más antiguos del fondo podrían no ser consultados nunca mientras sigan llegando sensores nuevos: es la inanición, y el sistema sería injusto.

### 4. Topología de Red (El Grafo)
Representaste la red de sensores como un `Map<String, List<String>>`.
- ¿Por qué usamos un `Map` y no un arreglo bidimensional (matriz de adyacencia) considerando que la mayoría de los sensores solo están conectados a 2 o 3 vecinos?
- ¿Cómo se traduce esta estructura de `Map` a la idea de un `NodoGrafo` con una lista de adyacencias?

**Respuesta (pregunta 4):** Con una matriz de adyacencia de n sensores se reservan n × n celdas, aunque cada sensor tenga solo 2 o 3 vecinos: casi todo son ceros, y agregar un sensor nuevo obliga a redimensionar la matriz. Con `Map<String, List<String>>` la memoria es proporcional a los sensores más las conexiones reales (O(n + e)), y se agregan sensores sin redimensionar nada. La matriz solo gana al preguntar si dos sensores concretos están conectados (O(1)); el mapa gana al recorrer los vecinos de un sensor (O(grado) frente a O(n)). Un `Map` equivale a un `NodoGrafo` así: cada clave es el nodo (el identificador del sensor) y su `List<String>` es la lista de adyacencia de ese nodo, es decir, sus enlaces; la diferencia es que los enlaces guardan identificadores en lugar de referencias directas a otros nodos.

### 5. Buffer de Ingesta y Rendimiento
El `bufferIngesta` procesa datos en orden FIFO.
- Si el volumen de datos aumenta drásticamente y necesitas procesar las lecturas en paralelo usando múltiples hilos, ¿seguiría siendo adecuado un `ArrayDeque` o deberías cambiar a una `ConcurrentLinkedQueue`? Justifica tu respuesta basándote en la seguridad de hilos (Thread-safety).

**Respuesta (pregunta 5):** Un `ArrayDeque` no es seguro entre hilos: si varios hilos hacen `add` y `poll` a la vez, pueden perderse elementos, repetirse o dejar la estructura inconsistente, y habría que protegerlo con `synchronized` (que bloquea a los demás hilos y crea contención). Para procesar en paralelo conviene cambiar a `ConcurrentLinkedQueue`: es una cola FIFO sin bloqueos, que usa CAS (compare-and-swap) para actualizar las referencias de forma atómica, y los hilos nunca esperan un candado. A cambio, `size()` cuesta O(n) y sus iteradores son de consistencia débil. Si el programa tiene un solo hilo, `ArrayDeque` sigue siendo mejor: es más rápido y usa menos memoria. Si se quisiera frenar a los productores cuando el buffer se llene, la opción sería una cola bloqueante como `LinkedBlockingQueue`.
