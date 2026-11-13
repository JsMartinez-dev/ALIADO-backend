package com.sebastian.spring.mas.aliado.aliado.common.correo;

public interface CorreoPort {
    void enviarHtml(String destinatario, String asunto, String contenidoHtml);
}
