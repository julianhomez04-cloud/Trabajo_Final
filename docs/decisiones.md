# Decisiones de diseño — Semana 3

## 1. Punto de entrada

El proyecto mantiene un único punto de entrada:

```text
IngestaSensores.main()
```

No se crean aplicaciones independientes por semana.

`BancoDePruebas` es una clase auxiliar y no contiene `main`.

## 2. Búsqueda por timestamp

Se utilizan dos estrategias:

- Búsqueda lineal: no requiere ordenamiento.
- Búsqueda binaria: requiere que el arreglo esté ordenado por timestamp.

Los datos sintéticos de `GeneradorDatos` se generan en orden cronológico, por lo que la búsqueda binaria por timestamp cumple su precondición.

## 3. Búsqueda por PM2.5

No se asume que los datos estén ordenados por PM2.5.

Por tanto, la búsqueda binaria por PM2.5 se conserva como experimento para demostrar el efecto de una precondición incumplida.

## 4. Comparación de String

Los identificadores de estación se comparan mediante:

```java
equals()
```

y no mediante:

```java
==
```

porque se necesita comparar contenido.

## 5. Medición

La comparación principal entre algoritmos utiliza el número de comparaciones.

El tiempo en milisegundos se conserva como evidencia experimental, pero no es la única medida utilizada.

## 6. Evolución del proyecto

La Semana 3 agrega una nueva capacidad a la misma plataforma:

```text
Sensores
   ↓
Ingesta
   ↓
Repositorio
   ↓
Búsqueda
   ↓
Medición de eficiencia
```

La Semana 4 podrá extender esta misma arquitectura para estudiar ordenamiento.

---

# Decisiones de diseño — Trabajo final (GestorDeRed con el JCF)

## 1. Interfaz en el tipo, implementación en el constructor

Todos los campos de `GestorDeRed` se declaran con la interfaz (`List`, `Deque`, `Queue`, `Map`) y solo el constructor decide la implementación. Así se puede cambiar `ArrayList` por `LinkedList` sin tocar ningún método.

| Campo | Estructura | Por qué |
|---|---|---|
| `historialEventos` | `ArrayList` | Se agrega al final y se leen los más recientes con `subList`. |
| `navegadorAlertas` | `ArrayDeque` | Hay que operar por los dos extremos. |
| `colaMonitoreo` y `bufferIngesta` | `ArrayDeque` como `Queue` | FIFO sin el costo de nodos de `LinkedList`. |
| `topologiaRed` | `LinkedHashMap<String, List<String>>` | Lista de adyacencia; se eligió `LinkedHashMap` en lugar de `HashMap` para que `detectarEstacionesAisladas()` devuelva siempre el mismo orden (el de registro). |

## 2. Navegación de alertas: ciclo, no cursor

`navegarAlSiguiente()` saca el frente y lo reinserta al final; `navegarAlAnterior()` hace lo contrario. Solo devuelven `null` si no hay ninguna alerta, porque `addLast(null)` lanzaría `NullPointerException` en un `ArrayDeque`. La alternativa era un cursor con índice (sin dar la vuelta), pero entonces, tras `registrarAlerta`, que deja `alertaActual` en la última, el primer `navegarAlSiguiente()` devolvería `null` y el test de navegación fallaría. Consecuencia: `navegarAlAnterior()` justo después de `navegarAlSiguiente()` devuelve la misma alerta, porque es un ciclo.

## 3. Historial con fecha y límite

Cada evento se guarda como `[yyyy-MM-dd HH:mm] texto`, con `new java.util.Date()`. `limpiarHistorialAntiguo(mantener)` borra la posición 0 mientras el tamaño supere el límite; un límite negativo se trata como 0. Costo: `ArrayList.remove(0)` es O(n); si el historial fuera enorme, convendría `subList(0, exceso).clear()` (una sola pasada) o una estructura con borrado O(1) al inicio.

## 4. Topología sin duplicados y con sensores aislados

`registrarConexion` es bidireccional, no duplica vecinos ni conecta un sensor consigo mismo. Un sensor solo puede estar "aislado" si el gestor sabe que existe, por eso se agregó `registrarSensor(id)` (y el alias `addSensor`, que nombra el test). `obtenerVecinos` devuelve una copia para que quien llame no pueda alterar la topología por dentro.

## 5. Buffer y Streams

`procesarSiguienteLectura()` usa `poll()` (devuelve `null` con el buffer vacío; `remove()` lanzaría excepción). `filtrarAlertasCriticas` recorre el buffer con un `stream()` sin consumirlo, y usa `>` estricto: una lectura igual al límite no es crítica.

## 6. Pruebas

Los tests originales (`TestGestorDeRed`) casi solo comprueban que el resultado no sea `null`. Se agregó `GestorDeRedComportamientoTest` (22 pruebas) para verificar orden, duplicados, casos vacíos y efectos laterales. Se comprobó que sirve: contra versiones con errores típicos (sin la guarda de `null`, sin control de duplicados, conexión en un solo sentido) la suite falla.
