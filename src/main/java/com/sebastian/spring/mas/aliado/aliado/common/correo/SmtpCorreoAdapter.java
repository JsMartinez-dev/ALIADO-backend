package com.sebastian.spring.mas.aliado.aliado.common.correo;

import org.springframework.beans.factory.annotation.Value;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class SmtpCorreoAdapter implements CorreoPort {
    private final JavaMailSender mailSender;
    private final String remitente;
    private final String nombreRemitente;

    public SmtpCorreoAdapter(JavaMailSender mailSender,
                             @Value("${aliado.mail.from}") String remitente,
                             @Value("${aliado.mail.from-name:ALIADO}") String nombreRemitente) {
        this.mailSender = mailSender;
        this.remitente = remitente;
        this.nombreRemitente = nombreRemitente;
    }

    @Override
    public void enviarHtml(String destinatario, String asunto, String contenidoHtml) {
        MimeMessage mensaje = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, "UTF-8");
            helper.setFrom(remitente, nombreRemitente);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(contenidoHtml, true);
        } catch (MessagingException | java.io.UnsupportedEncodingException ex) {
            throw new IllegalArgumentException("No se pudo preparar el correo de recuperación.", ex);
        }
        mailSender.send(mensaje);
    }
}
