package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sebastian.spring.mas.aliado.aliado.common.web.GlobalExceptionHandler;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.ConfigurarDisponibilidadCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.DisponibilidadDatos;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.GuardarCompromisoAgendaCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception.ConflictoAgendaException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.CompromisoAgenda;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.Disponibilidad;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.EstadoExposicion;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.OrigenCompromiso;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.PerfilProfesional;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.repository.PerfilProfesionalRepository;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

class AgendaDisponibilidadConflictoTest {
    private static final UUID PRESTADOR = UUID.randomUUID();
    private static final UUID PERFIL = UUID.randomUUID();
    private static final DisponibilidadDatos LUNES_10_A_12 = new DisponibilidadDatos(1, LocalTime.of(10, 0), LocalTime.of(12, 0));

    @Test
    void compromisoLunesDiezAUnoSeRechazaContraBloqueLunesDiezADoce() {
        var servicio = disponibilidad(perfilCon(compromiso(
                "2026-11-30T10:00:00-05:00", "2026-11-30T11:00:00-05:00"), List.of()));

        var error = assertThrows(ConflictoAgendaException.class,
                () -> servicio.ejecutar(new ConfigurarDisponibilidadCommand(PRESTADOR, List.of(LUNES_10_A_12))));

        assertEquals("La disponibilidad entra en conflicto con un compromiso ya registrado.", error.getMessage());
    }

    @Test
    void mismoInstanteConOffsetZSeRechazaIgualQueConMenosCinco() {
        var conOffset = disponibilidad(perfilCon(compromiso(
                "2026-11-30T10:00:00-05:00", "2026-11-30T11:00:00-05:00"), List.of()));
        var conZ = disponibilidad(perfilCon(compromiso(
                "2026-11-30T15:00:00Z", "2026-11-30T16:00:00Z"), List.of()));

        var errorOffset = assertThrows(ConflictoAgendaException.class,
                () -> conOffset.ejecutar(new ConfigurarDisponibilidadCommand(PRESTADOR, List.of(LUNES_10_A_12))));
        var errorZ = assertThrows(ConflictoAgendaException.class,
                () -> conZ.ejecutar(new ConfigurarDisponibilidadCommand(PRESTADOR, List.of(LUNES_10_A_12))));

        assertEquals(errorOffset.getMessage(), errorZ.getMessage());
    }

    @Test
    void compromisoQueCruzaMedianocheSeCompruebaTambienEnElDiaAnterior() {
        var servicio = disponibilidad(perfilCon(compromiso(
                "2026-11-30T23:30:00-05:00", "2026-12-01T00:30:00-05:00"), List.of()));
        var bloqueLunes = new DisponibilidadDatos(1, LocalTime.of(23, 0), LocalTime.of(23, 59));

        var error = assertThrows(ConflictoAgendaException.class,
                () -> servicio.ejecutar(new ConfigurarDisponibilidadCommand(PRESTADOR, List.of(bloqueLunes))));

        assertEquals("La disponibilidad entra en conflicto con un compromiso ya registrado.", error.getMessage());
    }

    @Test
    void bloquesQueSoloTocanLosLimitesDelCompromisoSePermitenYSeGuardan() {
        var repo = new Repo();
        var servicio = disponibilidad(perfilCon(repo,
                compromiso("2026-11-30T23:30:00-05:00", "2026-12-01T00:30:00-05:00"), List.of()));
        var antesDelInicio = new DisponibilidadDatos(1, LocalTime.of(22, 0), LocalTime.of(23, 30));
        var despuesDelFin = new DisponibilidadDatos(2, LocalTime.of(0, 30), LocalTime.of(1, 0));

        var resultado = servicio.ejecutar(new ConfigurarDisponibilidadCommand(
                PRESTADOR, List.of(antesDelInicio, despuesDelFin)));

        assertEquals(2, resultado.disponibilidades().size());
        assertEquals(1, repo.guardados);
    }

    @Test
    void compromisoQueCruzaMedianocheSeCompruebaEnElDiaSiguiente() {
        var servicio = disponibilidad(perfilCon(compromiso(
                "2026-11-30T23:30:00-05:00", "2026-12-01T00:30:00-05:00"), List.of()));
        var bloqueMartes = new DisponibilidadDatos(2, LocalTime.of(0, 0), LocalTime.of(1, 0));

        var error = assertThrows(ConflictoAgendaException.class,
                () -> servicio.ejecutar(new ConfigurarDisponibilidadCommand(PRESTADOR, List.of(bloqueMartes))));

        assertEquals("La disponibilidad entra en conflicto con un compromiso ya registrado.", error.getMessage());
    }

    @Test
    void bloqueSinSolapeSePermiteYSeGuarda() {
        var repo = new Repo();
        var servicio = disponibilidad(perfilCon(repo,
                compromiso("2026-11-30T10:00:00-05:00", "2026-11-30T11:00:00-05:00"), List.of()));
        var bloque = new DisponibilidadDatos(1, LocalTime.of(12, 0), LocalTime.of(13, 0));

        var resultado = servicio.ejecutar(new ConfigurarDisponibilidadCommand(PRESTADOR, List.of(bloque)));

        assertEquals(1, resultado.disponibilidades().size());
        assertEquals(LocalTime.of(12, 0), resultado.disponibilidades().getFirst().horaInicio());
        assertEquals(1, repo.guardados);
    }

    @Test
    void rechazoNoAlteraDisponibilidadPreviamenteConfirmada() {
        var previa = new Disponibilidad(UUID.randomUUID(), PERFIL, 1, LocalTime.of(8, 0), LocalTime.of(9, 0));
        var repo = new Repo();
        var servicio = disponibilidad(perfilCon(repo,
                compromiso("2026-11-30T10:00:00-05:00", "2026-11-30T11:00:00-05:00"), List.of(previa)));

        assertThrows(ConflictoAgendaException.class,
                () -> servicio.ejecutar(new ConfigurarDisponibilidadCommand(PRESTADOR, List.of(LUNES_10_A_12))));

        assertEquals(0, repo.guardados);
        assertEquals(List.of(previa), repo.perfil().disponibilidades());
    }

    @Test
    void conflictoAgendaSeTraduceARespuesta409ConCodigo() {
        var req = new MockHttpServletRequest("PUT", "/api/perfiles/mio/disponibilidad");
        var respuesta = new GlobalExceptionHandler().conflictoAgenda(
                new ConflictoAgendaException("La disponibilidad entra en conflicto con un compromiso ya registrado."), req);

        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertEquals(409, respuesta.getBody().status());
        assertEquals("CONFLICTO_AGENDA", respuesta.getBody().code());
    }

    @Test
    void crearYEditarCompromisosConservaLaPrevencionDeSolapamientos() {
        var existente = compromiso("2026-11-30T10:00:00-05:00", "2026-11-30T11:00:00-05:00");
        var repo = new Repo();
        repo.perfiles.put(PRESTADOR, perfil(List.of(), List.of(existente)));
        var guardado = new GuardadoCompromisoAgenda(repo);
        var crear = new CrearCompromisoAgendaService(guardado);
        var editar = new ActualizarCompromisoAgendaService(guardado);

        assertThrows(ConflictoAgendaException.class, () -> crear.ejecutar(new GuardarCompromisoAgendaCommand(
                PRESTADOR, null,
                OffsetDateTime.parse("2026-11-30T10:30:00-05:00"),
                OffsetDateTime.parse("2026-11-30T11:30:00-05:00"))));

        var otro = compromiso("2026-11-30T12:00:00-05:00", "2026-11-30T13:00:00-05:00");
        var creado = crear.ejecutar(new GuardarCompromisoAgendaCommand(
                PRESTADOR, null, otro.fechaHoraInicio(), otro.fechaHoraFin()));
        assertThrows(ConflictoAgendaException.class, () -> editar.ejecutar(new GuardarCompromisoAgendaCommand(
                PRESTADOR, creado.id(),
                OffsetDateTime.parse("2026-11-30T10:30:00-05:00"),
                OffsetDateTime.parse("2026-11-30T11:30:00-05:00"))));
        editar.ejecutar(new GuardarCompromisoAgendaCommand(
                PRESTADOR, creado.id(),
                OffsetDateTime.parse("2026-11-30T12:30:00-05:00"),
                OffsetDateTime.parse("2026-11-30T13:30:00-05:00")));

        assertTrue(repo.perfil().compromisos().stream().anyMatch(c -> c.id().equals(creado.id())));
        assertEquals(2, repo.perfil().compromisos().size());
    }

    private ConfigurarDisponibilidadService disponibilidad(Repo repo) {
        return new ConfigurarDisponibilidadService(repo);
    }

    private Repo perfilCon(CompromisoAgenda compromiso, List<Disponibilidad> disponibilidades) {
        var repo = new Repo();
        repo.perfiles.put(PRESTADOR, perfil(disponibilidades, List.of(compromiso)));
        return repo;
    }

    private Repo perfilCon(Repo repo, CompromisoAgenda compromiso, List<Disponibilidad> disponibilidades) {
        repo.perfiles.put(PRESTADOR, perfil(disponibilidades, List.of(compromiso)));
        return repo;
    }

    private CompromisoAgenda compromiso(String inicio, String fin) {
        return new CompromisoAgenda(UUID.randomUUID(), PERFIL,
                OffsetDateTime.parse(inicio), OffsetDateTime.parse(fin), OrigenCompromiso.MANUAL);
    }

    private PerfilProfesional perfil(List<Disponibilidad> disponibilidades, List<CompromisoAgenda> compromisos) {
        var ahora = OffsetDateTime.now();
        return new PerfilProfesional(PERFIL, PRESTADOR, Map.of(), EstadoExposicion.PUBLICO, ahora, ahora,
                Set.of(), Set.of(), List.of(), List.of(), disponibilidades, compromisos);
    }

    private static class Repo implements PerfilProfesionalRepository {
        final Map<UUID, PerfilProfesional> perfiles = new HashMap<>();
        int guardados;

        @Override
        public Optional<PerfilProfesional> porPrestadorId(UUID prestadorId) {
            return Optional.ofNullable(perfiles.get(prestadorId));
        }

        @Override
        public List<PerfilProfesional> visiblesPorCategoriaYZona(UUID categoriaId, UUID zonaId) {
            return new ArrayList<>();
        }

        @Override
        public PerfilProfesional guardar(PerfilProfesional perfil) {
            guardados++;
            perfiles.put(perfil.prestadorId(), perfil);
            return perfil;
        }

        PerfilProfesional perfil() {
            return perfiles.get(PRESTADOR);
        }
    }
}
