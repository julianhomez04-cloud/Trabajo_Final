# Bitácora individual - Semana 7

1. ## Datos de la actividad

- **Estudiante:** Julian Gomez
- **Equipo:** Individual 
- **Semana:** 7
- **Fecha del laboratorio:** 2026-11-15
- **Fecha del taller:** 2026-11-17
- **Tema principal:** Java Collections Framework (JCF) y Topología de Red en IoT
- **Pregunta de la semana:** ¿Cómo elegir e implementar eficientemente las estructuras del JCF (List, Deque, Queue, Map) para resolver los retos de un gestor de red de sensores IoT asegurando el 100% de pruebas aprobadas?

## 2. Predicción antes de ejecutar

Antes de abrir o ejecutar el programa, responde:

1. **Que creo que va a ocurrir?**
   Al implementar el mapa de adyacencia con `Map<String, List<String>>` y los métodos de navegación con `ArrayDeque`, espero que el sistema gestione correctamente las conexiones bidireccionales y supere las pruebas unitarias de JUnit sin errores de tipo o referencias nulas.
2. **Que parte del programa o del algoritmo puede fallar?**
   El método `registrarConexion` al intentar agregar vecinos a un sensor que aún no ha sido inicializado en el mapa, lo cual podría arrojar un `NullPointerException` si no se usa un método seguro como `computeIfAbsent`.
3. **Como comprobare mi prediccion?**
   Ejecutando la suite de pruebas unitarias específicas de la gestión de red (`GestorDeRedComportamientoTest.java` / `TestGestorDeRed`) y verificando que el indicador de éxito alcance el 100%.

## 3. Evidencia del laboratorio

### Resultado observado

Al ejecutar las pruebas unitarias tras implementar los 10 retos, las pruebas de JUnit validaron de forma exitosa el historial de eventos con marcas de tiempo (`java.util.Date()`), la navegación bidireccional de alertas con `ArrayDeque`, y la detección de estaciones aisladas en el grafo de la red.

### Diferencia entre la prediccion y el resultado

La predicción inicial contemplaba posibles fallos en el manejo de nulos con los vecinos del grafo; sin embargo, al emplear `computeIfAbsent` y `getOrDefault`, el comportamiento fue robusto y el código pasó los tests al primer intento de compilación de pruebas.

### Error o comportamiento inesperado

- **Que ocurrio?** En una prueba preliminar de la cola de rotación de sensores, el método `obtenerSiguienteSensor()` devolvía elementos en orden incorrecto al no reinsertar el nodo extraído al final de la cola.
- **Por que ocurrio?** Se estaba utilizando únicamente `poll()` sin realizar la operación complementaria `offer()` para mantener la rotación cíclica FIFO.
- **Como lo corregimos o que falta corregir?** Se ajustó la lógica para que al extraer el sensor con `poll()`, se invoque inmediatamente `offer(sensor)`, garantizando la rotación circular requerida por el reto.

## 4. Explicacion en lenguaje llano

Una estructura de datos en Java es como organizar diferentes tipos de casilleros en una bodega. Un mapa de adyacencia funciona igual que una libreta de contactos: buscas el nombre de un sensor (la clave) y obtienes instantáneamente la lista completa de todos los dispositivos cercanos con los que se comunica (los valores).

### Ejemplo o analogia

Imagina una red postal de entregas. La cola (`Queue`) representa la fila única en la ventanilla donde los paquetes se despachan estrictamente en el orden en que llegaron (FIFO), evitando que los paquetes antiguos se queden atrapados al fondo como ocurriría en una pila de platos.

## 5. El vacio que encontre

Al intentar explicar el tema, identifica el punto que aun no comprendes bien.

- **Mi duda concreta es:** ¿En qué escenarios específicos de alta concurrencia un `ArrayDeque` deja de ser seguro frente a una `ConcurrentLinkedQueue`?
- **Lo que ya puedo explicar es:** La diferencia de rendimiento en inserciones al inicio entre un `ArrayList` y un `LinkedList`.
- **Para resolver la duda consulte:** Documentación oficial de Java sobre el paquete `java.util.concurrent` y apuntes de la arquitectura de la Semana 7.
- **Ahora lo entiendo asi:** `ArrayDeque` no está sincronizado para hilos múltiples y puede corromper su estructura interna si varios hilos escriben en él a la vez, mientras que `ConcurrentLinkedQueue` utiliza operaciones atómicas de hardware (CAS) para evitar bloqueos.

## 6. Trazado de la solucion

Escoge una ejecución, recorrido o caso representativo y trázalo paso a paso.

| Paso | Estado de los datos o estructura | Decision o resultado |
|---|---|---|
| 1 | `topologiaRed` vacío | Se invoca `registrarConexion("SensorA", "SensorB")` |
| 2 | `Map` con clave "SensorA" y lista `["SensorB"]` | Se aplica `computeIfAbsent` para registrar la vía bidireccional recíproca |
| 3 | `Map` con clave "SensorB" y lista `["SensorA"]` | El grafo queda conectado de forma bidireccional |
| 4 | Se consulta `obtenerVecinos("SensorA")` | Retorna con `getOrDefault` la lista `["SensorB"]` en tiempo $O(1)$ aproximado |

## 7. Decision de diseño

Relaciona lo aprendido con la Plataforma de Monitoreo Ambiental Urbano.

- **Problema que debiamos resolver:** Representar las conexiones físicas y vecindades entre los distintos nodos de sensores distribuidos en la red urbana.
- **Estructura, algoritmo o estrategia elegida:** `Map<String, List<String>>` (Lista de adyacencia basada en tablas Hash y Listas del JCF).
- **Alternativa descartada:** Matriz de adyacencia basada en arreglos bidimensionales (`int[][]`).
- **Por que elegimos la primera:** Porque la red de sensores es dispersa (cada nodo se comunica solo con pocos vecinos). La matriz consumiría demasiada memoria estática innecesaria ($O(V^2)$), mientras que la lista de adyacencia optimiza el almacenamiento a $O(V + E)$.
- **Que evidencia respalda la decision:** El paso exitoso de las pruebas unitarias de topología y la eficiencia en las consultas de vecinos con `getOrDefault`.

## 8. Aporte al proyecto

- **Archivo(s) o modulo(s) trabajado(s):** `src/structures/management/GestorDeRed.java` y pruebas asociadas en `test/structures/management/`.
- **Cambio realizado:** Implementación completa de los 10 retos de gestión de red, incluyendo historial con marcas de tiempo, navegación con `Deque`, colas de rotación, manejo de grafos y limpieza de memoria en el historial.
- **Como se conecta con la capa anterior:** Utiliza las bases de ingesta de datos de las semanas anteriores para poblar las estructuras del Java Collections Framework.
- **Que queda pendiente para la siguiente semana:** Preparar la sustentación técnica del proyecto y la revisión final del repositorio en GitHub.

## 9. Commits realizados

Registra los commits que muestran tu aporte individual.

| Commit | Mensaje | Que demuestra |
|---|---|---|
| `a3f8c12` | `Implementar retos de historial y alertas con Deque` | Resolución de los retos 1, 2 y 3 |
| `b7e2d91` | `Agregar topologia de red y gestion de grafo con Map` | Implementación de adyacencia para los retos 6, 7 y 9 |
| `c4a1e88` | `Ajustar rotacion de sensores y asegurar 100% en tests` | Cierre de retos y validación con JUnit |

## 10. Reexplicacion final

El uso del Java Collections Framework permite estructurar el gestor de sensores de forma profesional mediante interfaces desacopladas. Elegimos `Map` y `Deque` porque proveen el rendimiento exacto en tiempo y memoria requerido para procesar eventos en tiempo real sin saturar el sistema.

## 11. Reflexion individual

Responde con honestidad:

1. **Lo que ahora puedo hacer y antes no podia:**
   Diseñar sistemas de gestión basados en grafos y colas doblemente enlazadas aplicando las interfaces estándar de Java.
2. **El error o supuesto que mas me enseno:**
   Comprender la importancia de validar estados vacíos con métodos como `poll()` y `getOrDefault()` para evitar excepciones inesperadas en producción.
3. **La pregunta que llevaria a la proxima clase:**
   ¿Cómo impacta el uso de iteradores concurrentes al escalar la red a miles de sensores activos?
4. **Que parte del trabajo fue realmente mia:**
   La codificación y depuración de la lógica del gestor de red y la adaptación de las estructuras para superar las pruebas unitarias.

## Lista de verificacion antes de entregar

- [x] Escribi la prediccion antes de consultar el resultado.
- [x] Inclui evidencia concreta del laboratorio.
- [x] Explique un concepto sin depender de jerga.
- [x] Registre un vacio, una duda o un error real.
- [x] Trace al menos un caso paso a paso.
- [x] Justifique una decision del proyecto y una alternativa descartada.
- [x] Registre mis commits y mi aporte individual.
- [x] Deje claro que queda pendiente.
- [x] Renombre el archivo con el formato `Trabajo_final-julian.md`.