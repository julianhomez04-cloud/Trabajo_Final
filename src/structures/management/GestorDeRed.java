package structures.management;

import model.LecturaSensor;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * GestorDeRed - Módulo Integrador de Estructuras de Datos
 * VERSION ESTUDIANTE
 *
 * Objetivo: Implementar la gestión de la red utilizando las colecciones de Java (JCF).
 * El éxito de este módulo no es solo que el código compile, sino que elijas
 * la estructura correcta para cada problema.
 */
public class GestorDeRed {
    // 1. Historial de Eventos: Registro lineal de todas las actividades
    private List<String> historialEventos;

    // 2. Navegador de Alertas: Permite moverse adelante y atrás entre alertas
    private Deque<String> navegadorAlertas;
    private String alertaActual;

    // 3. Ciclo de Monitoreo: Gestión de la rotación de sensores a consultar
    private Queue<String> colaMonitoreo;

    // 4. Topología de la Red: Grafo de conexiones entre estaciones (Lista de adyacencia)
    private Map<String, List<String>> topologiaRed;

    // 5. Buffer de Ingesta: Cola de espera para procesar lecturas entrantes
    private Queue<LecturaSensor> bufferIngesta;

    public GestorDeRed() {
        // TODO: Inicializa las colecciones usando las implementaciones más adecuadas de Java
        // (ArrayList, ArrayDeque, HashMap, etc.)
        this.historialEventos = new ArrayList<>();
        this.navegadorAlertas = new ArrayDeque<>();
        this.colaMonitoreo = new ArrayDeque<>();
        // LinkedHashMap: igual de rapido que HashMap para buscar por clave, pero conserva
        // el orden de registro, asi detectarEstacionesAisladas() devuelve siempre el mismo orden.
        this.topologiaRed = new LinkedHashMap<>();
        this.bufferIngesta = new ArrayDeque<>();
    }

    // =========================================================================
    // SECCIÓN 1: HISTORIAL Y EVENTOS
    // =========================================================================

    public void agregarEvento(String evento) {
        // TODO 1 (resuelto): fecha/hora actual + descripcion, al final de la lista.
        // add() agrega al final, asi el historial queda en orden cronologico.
        String marca = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        historialEventos.add("[" + marca + "] " + evento);
    }

    public List<String> obtenerEventosRecientes(int limite) {
        if (historialEventos.size() <= limite) return new ArrayList<>(historialEventos);
        return historialEventos.subList(historialEventos.size() - limite, historialEventos.size());
    }

    public void limpiarHistorialAntiguo(int mantener) {
        // TODO 10 (resuelto): se borra el elemento de la posicion 0 (el mas antiguo)
        // mientras el historial supere el limite. Un limite negativo se trata como 0.
        // Nota: ArrayList.remove(0) desplaza todos los elementos (O(n)); para historiales
        // enormes convendria historialEventos.subList(0, exceso).clear(), que desplaza una
        // sola vez, o cambiar a LinkedList/ArrayDeque (ver docs/Preguntas_Criterio.md, pregunta 1).
        int limite = Math.max(mantener, 0);
        while (historialEventos.size() > limite) {
            historialEventos.remove(0);
        }
    }

    // =========================================================================
    // SECCIÓN 2: NAVEGACIÓN DE ALERTAS
    // =========================================================================

    public void registrarAlerta(String alerta) {
        navegadorAlertas.addLast(alerta);
        alertaActual = alerta;
    }

    public String navegarAlSiguiente() {
        // TODO 2 (resuelto): el frente se devuelve y se reinserta al final, formando un
        // ciclo. Solo devuelve null cuando no hay ninguna alerta registrada.
        String alerta = navegadorAlertas.pollFirst();
        if (alerta != null) {          // addLast(null) lanzaria NullPointerException en un ArrayDeque
            navegadorAlertas.addLast(alerta);
            alertaActual = alerta;
        }
        return alerta;
    }

    public String navegarAlAnterior() {
        // TODO 3 (resuelto): el espejo del anterior: se saca el ultimo y se reinserta al frente.
        String alerta = navegadorAlertas.pollLast();
        if (alerta != null) {
            navegadorAlertas.addFirst(alerta);
            alertaActual = alerta;
        }
        return alerta;
    }

    public String getAlertaActual() { return alertaActual; }

    // =========================================================================
    // SECCIÓN 3: CICLO DE MONITOREO
    // =========================================================================

    public void programarConsulta(String idSensor) {
        // TODO 4 (resuelto): contains() recorre la cola (O(n)), aceptable para pocos sensores.
        // Con miles de sensores convendria un Set auxiliar (LinkedHashSet) para que sea O(1).
        if (idSensor != null && !colaMonitoreo.contains(idSensor)) {
            colaMonitoreo.add(idSensor);
        }
    }

    public String obtenerSiguienteSensor() {
        // TODO 5 (resuelto): rotacion FIFO; poll() devuelve null si la cola esta vacia.
        String sensor = colaMonitoreo.poll();
        if (sensor != null) {
            colaMonitoreo.add(sensor);
        }
        return sensor;
    }

    // =========================================================================
    // SECCIÓN 4: TOPOLOGÍA DE RED
    // =========================================================================

    public void registrarConexion(String sensorA, String sensorB) {
        // TODO 6 (resuelto): conexion bidireccional sin duplicados.
        if (sensorA == null || sensorB == null) {
            return;
        }
        // computeIfAbsent crea la lista de vecinos la primera vez que aparece el sensor.
        List<String> vecinosA = topologiaRed.computeIfAbsent(sensorA, k -> new ArrayList<>());
        List<String> vecinosB = topologiaRed.computeIfAbsent(sensorB, k -> new ArrayList<>());
        if (sensorA.equals(sensorB)) {
            return;                    // un sensor no se conecta consigo mismo (queda registrado, sin vecinos)
        }
        if (!vecinosA.contains(sensorB)) {
            vecinosA.add(sensorB);
        }
        if (!vecinosB.contains(sensorA)) {
            vecinosB.add(sensorA);
        }
    }

    /** Registra un sensor en la red sin conectarlo a nadie (nace aislado). */
    public void registrarSensor(String idSensor) {
        if (idSensor != null) {
            topologiaRed.computeIfAbsent(idSensor, k -> new ArrayList<>());
        }
    }

    /** Alias de registrarSensor(), por el nombre que menciona el test de aisladas. */
    public void addSensor(String idSensor) {
        registrarSensor(idSensor);
    }

    public List<String> obtenerVecinos(String idSensor) {
        // TODO 7 (resuelto): getOrDefault evita el null si el sensor no existe.
        // Se devuelve una COPIA para que quien llame no pueda alterar la topologia por dentro.
        return new ArrayList<>(topologiaRed.getOrDefault(idSensor, Collections.emptyList()));
    }

    public List<String> detectarEstacionesAisladas() {
        // TODO 9 (resuelto): sensores registrados cuya lista de vecinos esta vacia.
        List<String> aisladas = new ArrayList<>();
        for (Map.Entry<String, List<String>> entrada : topologiaRed.entrySet()) {
            if (entrada.getValue().isEmpty()) {
                aisladas.add(entrada.getKey());
            }
        }
        return aisladas;
    }

    // =========================================================================
    // SECCIÓN 5: BUFFER DE INGESTA
    // =========================================================================

    public void recibirLectura(LecturaSensor lectura) {
        bufferIngesta.add(lectura);
    }

    public LecturaSensor procesarSiguienteLectura() {
        // TODO 8 (resuelto): poll() saca la lectura mas antigua (FIFO) y devuelve null,
        // sin lanzar excepcion, si el buffer esta vacio (remove() si la lanzaria).
        return bufferIngesta.poll();
    }

    public List<LecturaSensor> filtrarAlertasCriticas(double limitePm25) {
        // TODO EXTRA (resuelto): filtra sin consumir el buffer (solo lo recorre).
        return bufferIngesta.stream()
                .filter(lectura -> lectura.getPm25() > limitePm25)
                .collect(Collectors.toList());
    }
}
