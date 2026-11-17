package structures.management;

import model.LecturaSensor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

/**
 * Pruebas de comportamiento del GestorDeRed. Los tests originales (TestGestorDeRed)
 * casi solo comprueban "no es null"; estas verifican ORDEN, DUPLICADOS, CASOS VACIOS
 * y los efectos laterales de cada reto.
 */
public class GestorDeRedComportamientoTest {
    private GestorDeRed gestor;

    @BeforeEach
    void setUp() {
        gestor = new GestorDeRed();
    }

    private static LecturaSensor lectura(String id, double pm25) {
        return new LecturaSensor(id, "T", 20.0, 50.0, pm25);
    }

    // ---------- Reto 1: historial con fecha ----------
    @Test
    void eventoLlevaFechaAlPrincipioYElTextoDespues() {
        gestor.agregarEvento("Sensor EST-01 activado");
        String guardado = gestor.obtenerEventosRecientes(1).get(0);
        assertTrue(guardado.matches("\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}\\] Sensor EST-01 activado"), guardado);
    }

    @Test
    void historialQuedaEnOrdenCronologico() {
        gestor.agregarEvento("uno");
        gestor.agregarEvento("dos");
        gestor.agregarEvento("tres");
        List<String> ultimos = gestor.obtenerEventosRecientes(3);
        assertEquals(3, ultimos.size());
        assertTrue(ultimos.get(0).endsWith("] uno"));
        assertTrue(ultimos.get(2).endsWith("] tres"));
    }

    // ---------- Retos 2 y 3: navegacion ----------
    @Test
    void sinAlertasNavegarDevuelveNullYNoLanzaExcepcion() {
        assertNull(gestor.navegarAlSiguiente());
        assertNull(gestor.navegarAlAnterior());
    }

    @Test
    void navegarAlSiguienteRecorreEnCiclo() {
        gestor.registrarAlerta("A");
        gestor.registrarAlerta("B");
        gestor.registrarAlerta("C");
        assertEquals("A", gestor.navegarAlSiguiente());
        assertEquals("B", gestor.navegarAlSiguiente());
        assertEquals("C", gestor.navegarAlSiguiente());
        assertEquals("A", gestor.navegarAlSiguiente());
    }

    @Test
    void navegarAlAnteriorRecorreEnCicloInverso() {
        gestor.registrarAlerta("A");
        gestor.registrarAlerta("B");
        gestor.registrarAlerta("C");
        assertEquals("C", gestor.navegarAlAnterior());
        assertEquals("B", gestor.navegarAlAnterior());
        assertEquals("A", gestor.navegarAlAnterior());
        assertEquals("C", gestor.navegarAlAnterior());
    }

    @Test
    void navegarActualizaLaAlertaActualYNoPierdeAlertas() {
        gestor.registrarAlerta("A");
        gestor.registrarAlerta("B");
        gestor.navegarAlSiguiente();
        assertEquals("A", gestor.getAlertaActual());
        gestor.navegarAlSiguiente();
        gestor.navegarAlSiguiente();
        gestor.navegarAlSiguiente();
        assertEquals("B", gestor.getAlertaActual());      // 4 pasos sobre 2 alertas: A, B, A, B
        // siguen existiendo las dos: dos pasos mas devuelven A y B
        assertEquals("A", gestor.navegarAlSiguiente());
        assertEquals("B", gestor.navegarAlSiguiente());
    }

    // ---------- Retos 4 y 5: ciclo de monitoreo ----------
    @Test
    void programarConsultaNoDuplicaSensores() {
        gestor.programarConsulta("S1");
        gestor.programarConsulta("S2");
        gestor.programarConsulta("S1");
        assertEquals("S1", gestor.obtenerSiguienteSensor());
        assertEquals("S2", gestor.obtenerSiguienteSensor());
        assertEquals("S1", gestor.obtenerSiguienteSensor());
        // Con S1 duplicado la cuarta consulta seria S1 otra vez; sin duplicados la rotacion es S1, S2, S1, S2.
        assertEquals("S2", gestor.obtenerSiguienteSensor());
    }

    @Test
    void cicloVacioDevuelveNull() {
        assertNull(gestor.obtenerSiguienteSensor());
    }

    @Test
    void cicloConUnSoloSensorSiempreDevuelveEseSensor() {
        gestor.programarConsulta("S1");
        for (int i = 0; i < 5; i++) {
            assertEquals("S1", gestor.obtenerSiguienteSensor());
        }
    }

    // ---------- Retos 6, 7 y 9: topologia ----------
    @Test
    void conexionEsBidireccionalYSinDuplicados() {
        gestor.registrarConexion("S1", "S2");
        gestor.registrarConexion("S1", "S2");
        gestor.registrarConexion("S2", "S1");
        assertEquals(Arrays.asList("S2"), gestor.obtenerVecinos("S1"));
        assertEquals(Arrays.asList("S1"), gestor.obtenerVecinos("S2"));
    }

    @Test
    void unSensorPuedeTenerVariosVecinos() {
        gestor.registrarConexion("S1", "S2");
        gestor.registrarConexion("S1", "S3");
        assertEquals(Arrays.asList("S2", "S3"), gestor.obtenerVecinos("S1"));
        assertEquals(Arrays.asList("S1"), gestor.obtenerVecinos("S3"));
    }

    @Test
    void sensorDesconocidoDevuelveListaVaciaNoNull() {
        List<String> vecinos = gestor.obtenerVecinos("NO-EXISTE");
        assertNotNull(vecinos);
        assertTrue(vecinos.isEmpty());
    }

    @Test
    void modificarLaListaDevueltaNoAlteraLaTopologia() {
        gestor.registrarConexion("S1", "S2");
        gestor.obtenerVecinos("S1").clear();
        assertEquals(Arrays.asList("S2"), gestor.obtenerVecinos("S1"));
    }

    @Test
    void sensorRegistradoSinConexionesEsAislado() {
        gestor.registrarConexion("S1", "S2");
        gestor.registrarSensor("S3");
        assertEquals(Arrays.asList("S3"), gestor.detectarEstacionesAisladas());
        gestor.registrarConexion("S3", "S1");
        assertTrue(gestor.detectarEstacionesAisladas().isEmpty());
    }

    @Test
    void addSensorEsEquivalenteARegistrarSensor() {
        gestor.addSensor("S9");
        assertEquals(Arrays.asList("S9"), gestor.detectarEstacionesAisladas());
    }

    @Test
    void redSinSensoresNoTieneAisladas() {
        assertTrue(gestor.detectarEstacionesAisladas().isEmpty());
    }

    // ---------- Reto 8 y extra: buffer ----------
    @Test
    void bufferVacioDevuelveNull() {
        assertNull(gestor.procesarSiguienteLectura());
    }

    @Test
    void bufferProcesaEnFifoYSeVacia() {
        LecturaSensor a = lectura("S1", 10);
        LecturaSensor b = lectura("S2", 20);
        gestor.recibirLectura(a);
        gestor.recibirLectura(b);
        assertSame(a, gestor.procesarSiguienteLectura());
        assertSame(b, gestor.procesarSiguienteLectura());
        assertNull(gestor.procesarSiguienteLectura());
    }

    @Test
    void filtrarAlertasCriticasUsaLimiteEstrictoYNoConsumeElBuffer() {
        LecturaSensor baja = lectura("S1", 10);
        LecturaSensor justo = lectura("S2", 50);     // igual al limite: NO es "superior"
        LecturaSensor alta = lectura("S3", 80);
        gestor.recibirLectura(baja);
        gestor.recibirLectura(justo);
        gestor.recibirLectura(alta);
        List<LecturaSensor> criticas = gestor.filtrarAlertasCriticas(50);
        assertEquals(1, criticas.size());
        assertSame(alta, criticas.get(0));
        assertSame(baja, gestor.procesarSiguienteLectura());   // el buffer sigue intacto
    }

    // ---------- Reto 10: limpiar historial ----------
    @Test
    void limpiarConservaLosEventosMasRecientes() {
        for (int i = 0; i < 100; i++) gestor.agregarEvento("Evento " + i);
        gestor.limpiarHistorialAntiguo(10);
        List<String> quedan = gestor.obtenerEventosRecientes(100);
        assertEquals(10, quedan.size());
        assertTrue(quedan.get(0).endsWith("] Evento 90"));
        assertTrue(quedan.get(9).endsWith("] Evento 99"));
    }

    @Test
    void limpiarConLimiteMayorAlTamanoNoBorraNada() {
        for (int i = 0; i < 5; i++) gestor.agregarEvento("e" + i);
        gestor.limpiarHistorialAntiguo(50);
        assertEquals(5, gestor.obtenerEventosRecientes(50).size());
    }

    @Test
    void limpiarConCeroOLimiteNegativoVaciaElHistorialSinFallar() {
        gestor.agregarEvento("a");
        gestor.limpiarHistorialAntiguo(0);
        assertEquals(0, gestor.obtenerEventosRecientes(10).size());
        gestor.agregarEvento("b");
        gestor.limpiarHistorialAntiguo(-3);
        assertEquals(0, gestor.obtenerEventosRecientes(10).size());
    }
}
