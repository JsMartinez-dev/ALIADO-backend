package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto;

import java.util.UUID;

/**
 * Alta o sincronización de una instalación Android (F09-PUSH-01/D-08).
 * El secreto de instalación y la credencial de revocación viajan en claro solo en la
 * petición; el caso de uso los transforma en huellas antes de persistir.
 */
public record RegistrarDispositivoPushCommand(UUID instalacionId, UUID usuarioId, String pruebaInstalacion,
                                              String plataforma, boolean permisoConcedido, String tokenFcm,
                                              String credencialRevocacion) {}
