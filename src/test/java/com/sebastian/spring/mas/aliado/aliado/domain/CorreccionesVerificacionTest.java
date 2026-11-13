package com.sebastian.spring.mas.aliado.aliado.domain;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.common.storage.ArchivoValidacion;
import com.sebastian.spring.mas.aliado.aliado.common.auditoria.AuditoriaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.service.AuthService;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.service.SesionService;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.PerfilBasePort;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.SesionPort;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.model.TokenRecuperacion;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.repository.ConsentimientoRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.repository.TokenRecuperacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.repository.UsuarioRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.application.CatalogoService;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.domain.model.Categoria;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.domain.model.EstadoCatalogo;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.domain.repository.CategoriaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.domain.repository.ZonaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.VerificacionService;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.repository.DocumentoVerificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.repository.SolicitudVerificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.NotificacionService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.CanalEntregaNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Canal;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.PerfilProfesional;
import com.sebastian.spring.mas.aliado.aliado.common.storage.ArchivoStorageService;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.ConsultaPreferenciasPort;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests enfocados de la corrección de la verificación de conformidad (SPEC-01 a 14).
 * Solo reglas de aplicación con mocks; sin contexto Spring ni base de datos.
 */
@ExtendWith(MockitoExtension.class)
class CorreccionesVerificacionTest {

    // ---- ArchivoValidacion: firma/magic bytes (SPEC-04/05/10/11) ----

    @Test
    void jpegConFirmaValidaPasa() {
        byte[] jpeg = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10};
        ArchivoValidacion.validar("foto.jpg", "image/jpeg", jpeg, ArchivoValidacion.IMAGENES);
        assertEquals("image/jpeg", ArchivoValidacion.detectarTipo(jpeg));
    }

    @Test
    void pdfConFirmaValidaPasa() {
        byte[] pdf = {'%', 'P', 'D', 'F', '-', '1', '.', '7'};
        ArchivoValidacion.validar("doc.pdf", "application/pdf", pdf, ArchivoValidacion.DOCUMENTOS);
        assertEquals("application/pdf", ArchivoValidacion.detectarTipo(pdf));
    }

    @Test
    void contenidoQueNoCorrespondeAlTipoDeclaradoSeRechaza() {
        byte[] jpeg = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
        BusinessException ex = assertThrows(BusinessException.class,
                () -> ArchivoValidacion.validar("foto.png", "image/png", jpeg, ArchivoValidacion.IMAGENES));
        assertEquals("TIPO_ARCHIVO_NO_PERMITIDO", ex.code());
        assertEquals(415, ex.status());
    }

    @Test
    void contenidoSinFirmaConocidaSeRechaza() {
        byte[] basura = {0x7F, 'E', 'L', 'F', 0x02, 0x01};
        BusinessException ex = assertThrows(BusinessException.class,
                () -> ArchivoValidacion.validar("foto.jpg", "image/jpeg", basura, ArchivoValidacion.IMAGENES));
        assertEquals("TIPO_ARCHIVO_NO_PERMITIDO", ex.code());
    }

    // ---- PerfilProfesional.datosPublicos: lista blanca (SPEC-03) ----

    @Test
    void datosPublicosExcluyeTelefonoDireccionYCorreo() {
        Map<String, Object> datos = PerfilProfesional.datosPublicos(Map.of(
                "descripcion", "Plomero con 10 años",
                "telefono", "3001234567",
                "direccion", "Calle 1 # 2-3",
                "correo", "a@b.co",
                "especialidades", List.of("grifería")));
        assertEquals("Plomero con 10 años", datos.get("descripcion"));
        assertTrue(!datos.containsKey("telefono") && !datos.containsKey("direccion")
                && !datos.containsKey("correo"));
        assertTrue(datos.containsKey("especialidades"));
    }

    // ---- NotificacionService respeta preferencias (SPEC-09/14) ----

    @Mock
    private NotificacionRepository notificaciones;
    @Mock
    private CanalEntregaNotificacion canal;
    @Mock
    private ConsultaPreferenciasPort preferencias;

    @Test
    void registrarNoGuardaCuandoElAvisoEstaDeshabilitado() {
        when(notificaciones.existeEvento(any(), any(), any(), anyInt())).thenReturn(false);
        when(preferencias.avisoHabilitado(any(), any(), any())).thenReturn(false);
        var servicio = new NotificacionService(notificaciones, canal, preferencias);
        servicio.registrar(TipoNotificacion.NUEVA_OPORTUNIDAD, UUID.randomUUID(), UUID.randomUUID(), 1,
                "Nueva oportunidad.", Canal.PUSH);
        verify(notificaciones, never()).guardar(any());
    }

    @Test
    void registrarGuardaCuandoElAvisoEstaHabilitado() {
        when(notificaciones.existeEvento(any(), any(), any(), anyInt())).thenReturn(false);
        when(preferencias.avisoHabilitado(any(), any(), any())).thenReturn(true);
        var servicio = new NotificacionService(notificaciones, canal, preferencias);
        servicio.registrar(TipoNotificacion.NUEVA_OPORTUNIDAD, UUID.randomUUID(), UUID.randomUUID(), 1,
                "Nueva oportunidad.", Canal.PUSH);
        verify(notificaciones).guardar(any());
    }

    // ---- CatalogoService.actualizarCategoria valida nombre y duplicado (SPEC-12) ----

    @Mock
    private CategoriaRepository categorias;
    @Mock
    private ZonaRepository zonas;
    @Mock
    private AuditoriaPort auditoria;

    @Test
    void actualizarCategoriaConNombreDeOtraEs409() {
        UUID id = UUID.randomUUID();
        when(categorias.porId(id)).thenReturn(Optional.of(
                new Categoria(id, "Plomería", EstadoCatalogo.ACTIVO)));
        when(categorias.porNombre("Electricidad")).thenReturn(Optional.of(
                new Categoria(UUID.randomUUID(), "Electricidad", EstadoCatalogo.ACTIVO)));
        var servicio = new CatalogoService(categorias, zonas, auditoria);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> servicio.actualizarCategoria(UUID.randomUUID(), id, "Electricidad", null));
        assertEquals("CATEGORIA_DUPLICADA", ex.code());
        assertEquals(409, ex.status());
    }

    @Test
    void actualizarCategoriaConNombreEnBlancoEs400() {
        UUID id = UUID.randomUUID();
        when(categorias.porId(id)).thenReturn(Optional.of(
                new Categoria(id, "Plomería", EstadoCatalogo.ACTIVO)));
        var servicio = new CatalogoService(categorias, zonas, auditoria);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> servicio.actualizarCategoria(UUID.randomUUID(), id, "   ", null));
        assertEquals("NOMBRE_REQUERIDO", ex.code());
        assertEquals(400, ex.status());
    }

    // ---- AuthService.confirmarRecuperacion distingue inválido de expirado (SPEC-01) ----

    @Mock
    private UsuarioRepository usuarios;
    @Mock
    private ConsentimientoRepository consentimientos;
    @Mock
    private TokenRecuperacionRepository tokens;
    @Mock
    private SesionPort sesiones;
    @Mock
    private PerfilBasePort perfilBase;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void confirmarRecuperacionConCodigoInexistenteEs400() {
        when(tokens.porCodigoHash(any())).thenReturn(Optional.empty());
        var servicio = new AuthService(usuarios, consentimientos, tokens, sesiones, perfilBase,
                passwordEncoder);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> servicio.confirmarRecuperacion("codigo-inexistente", "Clave1234"));
        assertEquals("TOKEN_INVALIDO", ex.code());
        assertEquals(400, ex.status());
    }

    @Test
    void confirmarRecuperacionConCodigoUsadoEs410() {
        OffsetDateTime ahora = OffsetDateTime.now();
        var token = new TokenRecuperacion(UUID.randomUUID(), UUID.randomUUID(),
                SesionService.hashToken("codigo"), ahora.minusHours(1), ahora.plusMinutes(30), true);
        when(tokens.porCodigoHash(any())).thenReturn(Optional.of(token));
        var servicio = new AuthService(usuarios, consentimientos, tokens, sesiones, perfilBase,
                passwordEncoder);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> servicio.confirmarRecuperacion("codigo", "Clave1234"));
        assertEquals("TOKEN_EXPIRADO", ex.code());
        assertEquals(410, ex.status());
    }

    // ---- VerificacionService: sin límite de 3 y tope BÁSICO (SPEC-10) ----

    @Mock
    private SolicitudVerificacionRepository verificaciones;
    @Mock
    private DocumentoVerificacionRepository documentos;
    @Mock
    private ArchivoStorageService storage;

    private static VerificacionService.ArchivoSubido documentoPdf(int i) {
        return new VerificacionService.ArchivoSubido("doc" + i + ".pdf", "application/pdf",
                new byte[]{'%', 'P', 'D', 'F', '-', '1'});
    }

    @Test
    void solicitarVerificacionAdmiteMasDeTresDocumentos() {
        UUID prestadorId = UUID.randomUUID();
        when(verificaciones.pendienteDePrestador(prestadorId)).thenReturn(Optional.empty());
        when(verificaciones.contarAprobadasDePrestador(prestadorId)).thenReturn(0L);
        when(verificaciones.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(storage.guardar(any(), any(), any())).thenReturn("clave");
        var servicio = new VerificacionService(verificaciones, documentos, storage);
        var solicitud = servicio.solicitarVerificacion(prestadorId,
                List.of(documentoPdf(1), documentoPdf(2), documentoPdf(3), documentoPdf(4)));
        assertEquals(prestadorId, solicitud.prestadorId());
        verify(documentos, times(4)).guardar(any());
    }

    @Test
    void solicitarVerificacionConNivelBasicoYaAlcanzadoEs409() {
        UUID prestadorId = UUID.randomUUID();
        when(verificaciones.pendienteDePrestador(prestadorId)).thenReturn(Optional.empty());
        when(verificaciones.contarAprobadasDePrestador(prestadorId)).thenReturn(1L);
        var servicio = new VerificacionService(verificaciones, documentos, storage);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> servicio.solicitarVerificacion(prestadorId, List.of(documentoPdf(1))));
        assertEquals("NIVEL_MAXIMO_ALCANZADO", ex.code());
        assertEquals(409, ex.status());
    }
}
