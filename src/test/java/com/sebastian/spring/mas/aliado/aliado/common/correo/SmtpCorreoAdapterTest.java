package com.sebastian.spring.mas.aliado.aliado.common.correo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

class SmtpCorreoAdapterTest {
    @Test
    void enviaMensajeHtmlAlDestinatarioConfigurado() throws Exception {
        JavaMailSender sender = mock(JavaMailSender.class);
        MimeMessage mensaje = new MimeMessage(Session.getInstance(new Properties()));
        when(sender.createMimeMessage()).thenReturn(mensaje);
        SmtpCorreoAdapter adapter = new SmtpCorreoAdapter(sender, "no-reply@example.test", "ALIADO");

        adapter.enviarHtml("persona@example.test", "Recupera tu acceso", "<h1>123456</h1>");

        verify(sender).send(mensaje);
        assertEquals("persona@example.test", mensaje.getAllRecipients()[0].toString());
        assertTrue(mensaje.getContentType().toLowerCase().contains("text/html"));
        assertTrue(mensaje.getContent().toString().contains("123456"));
    }
}
