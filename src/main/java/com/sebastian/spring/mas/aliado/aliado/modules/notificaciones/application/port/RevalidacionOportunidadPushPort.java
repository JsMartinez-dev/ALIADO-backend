package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port;

import java.util.UUID;

/**
 * Revalidación de la compatibilidad de una oportunidad para un destinatario concreto
 * (F09-PUSH-08).
 *
 * <p>El matching global ocurre una sola vez al publicar. Antes de cada envío se vuelve a
 * comprobar que la solicitud siga publicada y que el perfil del prestador siga siendo
 * compatible, reutilizando la evaluación individual de oportunidades. El puerto lo define
 * este módulo; el adaptador usa el caso de uso público del módulo de oportunidades.</p>
 */
public interface RevalidacionOportunidadPushPort {

    /** {@code true} si la solicitud está publicada y el prestador sigue siendo compatible. */
    boolean sigueVigente(UUID solicitudId, UUID prestadorId);
}
