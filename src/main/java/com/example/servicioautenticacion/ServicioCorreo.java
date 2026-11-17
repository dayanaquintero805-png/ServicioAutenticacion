package com.example.servicioautenticacion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class ServicioCorreo {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String correoRemitente;

    public ServicioCorreo(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarCorreoRecuperacion(String destinatario, String enlace) {

        SimpleMailMessage mensaje = new SimpleMailMessage();

        mensaje.setFrom(correoRemitente);
        mensaje.setTo(destinatario);
        mensaje.setSubject("Recuperación de contraseña - Take Care");

        mensaje.setText(
                "Hola,\n\n"
                + "Hemos recibido una solicitud para recuperar tu contraseña.\n\n"
                + "Ingresa al siguiente enlace para establecer una nueva contraseña:\n\n"
                + enlace + "\n\n"
                + "Si no solicitaste este cambio, puedes ignorar este mensaje.\n\n"
                + "Take Care Of Your Mind Health"
        );

        mailSender.send(mensaje);
    }
}