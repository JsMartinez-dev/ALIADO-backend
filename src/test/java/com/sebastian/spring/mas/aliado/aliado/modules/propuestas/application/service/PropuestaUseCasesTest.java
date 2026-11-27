package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.exception.*;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.exception.PropuestaNoActivaException;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.model.*;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.repository.PropuestaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.*;
import java.time.OffsetDateTime;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PropuestaUseCasesTest {
    private final UUID prestador = UUID.randomUUID();
    private final UUID cliente = UUID.randomUUID();
    private final UUID solicitudId = UUID.randomUUID();
    private final Repo repo = new Repo();
    private final List<Object> eventos = new ArrayList<>();
    private final ValidarDisponibilidadPropuestaService disponibilidad = new ValidarDisponibilidadPropuestaService();
    private final ValidarMensajePropuestaService mensaje = new ValidarMensajePropuestaService();

    private Solicitud solicitud(EstadoSolicitud estado) {
        var ahora = OffsetDateTime.now();
        return new Solicitud(solicitudId, cliente, UUID.randomUUID(), UUID.randomUUID(), "Problema", Urgencia.MEDIA,
                estado, ahora, ahora, 0, List.of(), List.of());
    }
    private CrearPropuestaService crear(EstadoSolicitud estado, boolean compatible) {
        return new CrearPropuestaService(repo, id -> Optional.of(solicitud(estado)), (s,p) -> compatible,
                eventos::add, disponibilidad, mensaje);
    }
    private CrearPropuestaCommand command(String d, String m) { return new CrearPropuestaCommand(prestador, solicitudId, d, m, 250000); }
    private Propuesta propuesta(EstadoPropuesta estado) {
        var ahora = OffsetDateTime.now();
        return new Propuesta(UUID.randomUUID(), solicitudId, prestador, "Lunes", "Mensaje", estado, ahora, ahora, 4);
    }
    @Test void crearSinMensajeConservaIdentidadYPublicaUnEvento() {
        var result = crear(EstadoSolicitud.PUBLICADA, true).ejecutar(command(" Lunes 8-12 ", null));
        assertEquals("ACTIVA", result.estado()); assertEquals("Lunes 8-12", result.disponibilidad());
        assertNull(result.mensaje()); assertEquals(prestador, result.prestadorId());
        assertEquals(solicitudId, result.solicitudId()); assertEquals(1, repo.datos.size()); assertEquals(1, eventos.size());
    }
    @Test void crearConMensajeLoConserva() {
        assertEquals("Puedo asistir", crear(EstadoSolicitud.PUBLICADA, true).ejecutar(command("Lunes", "Puedo asistir")).mensaje());
    }
    @Test void disponibilidadVaciaNoPersisteNiPublica() {
        assertThrows(DatosPropuestaInvalidosException.class, () -> crear(EstadoSolicitud.PUBLICADA, true).ejecutar(command(" ", null)));
        assertTrue(repo.datos.isEmpty()); assertTrue(eventos.isEmpty());
    }
    @Test void disponibilidadNulaRechazada() { assertThrows(DatosPropuestaInvalidosException.class, () -> disponibilidad.ejecutar(null)); }
    @Test void disponibilidadMayorQueColumnaRechazada() {
        var ex = assertThrows(DatosPropuestaInvalidosException.class, () -> crear(EstadoSolicitud.PUBLICADA,true).ejecutar(command("a".repeat(256),null)));
        assertEquals("DISPONIBILIDAD_MUY_LARGA", ex.codigo()); assertTrue(repo.datos.isEmpty());
    }
    @Test void limitesDeTextoPermitidos() {
        var result = crear(EstadoSolicitud.PUBLICADA,true).ejecutar(command("a".repeat(255),"b".repeat(1000)));
        assertEquals(255,result.disponibilidad().length()); assertEquals(1000,result.mensaje().length());
    }
    @Test void mensajeExcesivoNoPersiste() {
        assertThrows(DatosPropuestaInvalidosException.class, () -> crear(EstadoSolicitud.PUBLICADA,true).ejecutar(command("Lunes","a".repeat(1001))));
        assertTrue(repo.datos.isEmpty());
    }
    @Test void solicitudCerradaNoPersiste() {
        for (var estado : List.of(EstadoSolicitud.BORRADOR,EstadoSolicitud.CANCELADA,EstadoSolicitud.CONTRATADA)) {
            var ex=assertThrows(ConflictoPropuestaException.class,()->crear(estado,true).ejecutar(command("Lunes",null)));
            assertEquals("SOLICITUD_NO_ABIERTA",ex.codigo());
        }
        assertTrue(repo.datos.isEmpty()); assertTrue(eventos.isEmpty());
    }
    @Test void solicitudIncompatibleNoPersiste() {
        var ex=assertThrows(ConflictoPropuestaException.class,()->crear(EstadoSolicitud.PUBLICADA,false).ejecutar(command("Lunes",null)));
        assertEquals("SOLICITUD_NO_COMPATIBLE",ex.codigo()); assertTrue(repo.datos.isEmpty());
    }
    @Test void solicitudInexistenteNoPersiste() {
        var service=new CrearPropuestaService(repo,id->Optional.empty(),(s,p)->true,eventos::add,disponibilidad,mensaje);
        assertThrows(RecursoPropuestaNoEncontradoException.class,()->service.ejecutar(command("Lunes",null))); assertTrue(eventos.isEmpty());
    }
    @Test void propuestaActivaDuplicadaNoGeneraOtroAviso() {
        var service=crear(EstadoSolicitud.PUBLICADA,true); service.ejecutar(command("Lunes",null));
        assertThrows(ConflictoPropuestaException.class,()->service.ejecutar(command("Martes",null)));
        assertEquals(1,repo.datos.size()); assertEquals(1,eventos.size());
    }
    @Test void editarConservaOmitidosYVersion() {
        var actual=propuesta(EstadoPropuesta.ACTIVA); repo.guardar(actual);
        var service=new EditarPropuestaService(repo,disponibilidad,mensaje);
        var result=service.ejecutar(new EditarPropuestaCommand(actual.id(),prestador,null,"Nuevo",250000));
        assertEquals("Lunes",result.disponibilidad()); assertEquals("Nuevo",result.mensaje());
        assertEquals(4,repo.porId(actual.id()).orElseThrow().version());
    }
    @Test void editarAjenaEInexistenteUsanMismoError() {
        var actual=propuesta(EstadoPropuesta.ACTIVA);repo.guardar(actual);
        var service=new EditarPropuestaService(repo,disponibilidad,mensaje);
        var ajena=assertThrows(RecursoPropuestaNoEncontradoException.class,()->service.ejecutar(new EditarPropuestaCommand(actual.id(),UUID.randomUUID(),null,"Otro",250000)));
        var ausente=assertThrows(RecursoPropuestaNoEncontradoException.class,()->service.ejecutar(new EditarPropuestaCommand(UUID.randomUUID(),prestador,null,"Otro",250000)));
        assertEquals(ajena.codigo(),ausente.codigo()); assertEquals(actual,repo.porId(actual.id()).orElseThrow());
    }
    @Test void editarInactivaNoGuarda() {
        for(var estado: List.of(EstadoPropuesta.RETIRADA,EstadoPropuesta.ACEPTADA,EstadoPropuesta.CERRADA)) {
            var actual=propuesta(estado);repo.guardar(actual);
            assertThrows(PropuestaNoActivaException.class,()->new EditarPropuestaService(repo,disponibilidad,mensaje).ejecutar(new EditarPropuestaCommand(actual.id(),prestador,"Martes",null,250000)));
            assertEquals(actual,repo.porId(actual.id()).orElseThrow());
        }
    }
    @Test void retirarActivaYRechazarSegundoRetiro() {
        var actual=propuesta(EstadoPropuesta.ACTIVA);repo.guardar(actual);var service=new RetirarPropuestaService(repo);
        assertEquals("RETIRADA",service.ejecutar(new RetirarPropuestaCommand(actual.id(),prestador)).estado());
        assertThrows(PropuestaNoActivaException.class,()->service.ejecutar(new RetirarPropuestaCommand(actual.id(),prestador)));
    }
    @Test void retirarAjenaNoModifica() {
        var actual=propuesta(EstadoPropuesta.ACTIVA);repo.guardar(actual);
        assertThrows(RecursoPropuestaNoEncontradoException.class,()->new RetirarPropuestaService(repo).ejecutar(new RetirarPropuestaCommand(actual.id(),UUID.randomUUID())));
        assertEquals(actual,repo.porId(actual.id()).orElseThrow());
    }
    @Test void notificarSoloActivaAlDuenoReal() {
        var actual=propuesta(EstadoPropuesta.ACTIVA);repo.guardar(actual);List<UUID> destinatarios=new ArrayList<>();
        var service=new NotificarPropuestaRecibidaService(repo,id->Optional.of(solicitud(EstadoSolicitud.PUBLICADA)),(p,c)->destinatarios.add(c));
        service.ejecutar(actual.id());assertEquals(List.of(cliente),destinatarios);
    }
    @Test void replayDeRetiradaNoRegistraNuevoAviso() {
        var actual=propuesta(EstadoPropuesta.RETIRADA);repo.guardar(actual);List<UUID> registros=new ArrayList<>();
        var service=new NotificarPropuestaRecibidaService(repo,id->Optional.of(solicitud(EstadoSolicitud.PUBLICADA)),(p,c)->registros.add(p));
        service.ejecutar(actual.id());service.ejecutar(UUID.randomUUID());assertTrue(registros.isEmpty());
    }
    @Test void edicionDeDominioNoReactivaEstadosCerrados() {
        for(var estado:List.of(EstadoPropuesta.RETIRADA,EstadoPropuesta.ACEPTADA,EstadoPropuesta.CERRADA)) {
            assertThrows(PropuestaNoActivaException.class,()->propuesta(estado).editada("Martes",null,OffsetDateTime.now()));
        }
    }
    @Test void tarifaObligatoriaEnCreacionYPositiva() {
        assertThrows(DatosPropuestaInvalidosException.class, () -> crear(EstadoSolicitud.PUBLICADA,true)
                .ejecutar(new CrearPropuestaCommand(prestador,solicitudId,"Lunes",null,null)));
        assertThrows(DatosPropuestaInvalidosException.class, () -> crear(EstadoSolicitud.PUBLICADA,true)
                .ejecutar(new CrearPropuestaCommand(prestador,solicitudId,"Lunes",null,0)));
        assertThrows(DatosPropuestaInvalidosException.class, () -> crear(EstadoSolicitud.PUBLICADA,true)
                .ejecutar(new CrearPropuestaCommand(prestador,solicitudId,"Lunes",null,-1)));
        var result=crear(EstadoSolicitud.PUBLICADA,true)
                .ejecutar(new CrearPropuestaCommand(prestador,solicitudId,"Lunes",null,250000));
        assertEquals(250000,result.tarifaCop());
    }

    @Test void editarActualizaTarifa() {
        var actual=propuesta(EstadoPropuesta.ACTIVA); repo.guardar(actual);
        var result=new EditarPropuestaService(repo,disponibilidad,mensaje)
                .ejecutar(new EditarPropuestaCommand(actual.id(),prestador,null,null,180000));
        assertEquals(180000,result.tarifaCop());
    }

    @Test
    void listaSoloPropuestasPropiasIncluidasLasRetiradas() {
        var propia = repo.guardar(propuesta(EstadoPropuesta.RETIRADA));
        repo.guardar(new Propuesta(UUID.randomUUID(), solicitudId, UUID.randomUUID(),
                "Lunes", null, EstadoPropuesta.ACTIVA, propia.fechaCreacion(), propia.fechaActualizacion(), 0));

        var resultados = new ListarPropuestasPropiasService(repo).ejecutar(prestador, 0, 20);

        assertEquals(List.of(PropuestaResult.desde(propia)), resultados);
    }

    static class Repo implements PropuestaRepository {
        final Map<UUID,Propuesta> datos=new HashMap<>();
        public Optional<Propuesta> porId(UUID id){return Optional.ofNullable(datos.get(id));}
        public Optional<Propuesta> activaDePrestadorEnSolicitud(UUID s,UUID p){return datos.values().stream().filter(x->x.solicitudId().equals(s)&&x.prestadorId().equals(p)&&x.estado()==EstadoPropuesta.ACTIVA).findFirst();}
        public List<Propuesta> activasDeSolicitud(UUID id){return porSolicitud(id).stream().filter(x->x.estado()==EstadoPropuesta.ACTIVA).toList();}
        public List<Propuesta> porSolicitud(UUID id){return datos.values().stream().filter(x->x.solicitudId().equals(id)).toList();}
        public List<Propuesta> porPrestador(UUID prestadorId, int pagina, int tamano) {
            return datos.values().stream().filter(p -> p.prestadorId().equals(prestadorId))
                    .skip((long) pagina * tamano).limit(tamano).toList();
        }
        public Propuesta guardar(Propuesta p){datos.put(p.id(),p);return p;}
    }
}

