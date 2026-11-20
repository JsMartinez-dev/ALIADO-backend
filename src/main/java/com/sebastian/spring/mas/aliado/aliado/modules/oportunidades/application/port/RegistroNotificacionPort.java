package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.port;
import java.util.UUID;
/** Registro del aviso producido por una solicitud compatible; entrega a cargo de notificaciones. */
public interface RegistroNotificacionPort {void registrar(UUID solicitudId, UUID prestadorId, int version, String detalle);}
