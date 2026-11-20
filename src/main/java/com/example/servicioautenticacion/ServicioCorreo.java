
package com.example.servicioautenticacion;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class ServicioCorreo {

    @Value("${BREVO_API_KEY}")
    private String apiKey;

    @Value("${app.mail.from}")
    private String correoRemitente;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public ServicioCorreo(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    public void enviarCorreoRecuperacion(String destinatario, String enlace) {

        try {
            Map<String, Object> mensaje = Map.of(
                    "sender", Map.of(
                            "name", "Take Care Of Your Mind Health",
                            "email", correoRemitente
                    ),
                    "to", List.of(
                            Map.of("email", destinatario)
                    ),
                    "subject", "Recuperación de contraseña - Take Care",
                    "textContent",
                    "Hola,\n\n"
                    + "Hemos recibido una solicitud para recuperar tu contraseña.\n\n"
                    + "Ingresa al siguiente enlace para establecer una nueva contraseña:\n\n"
                    + enlace + "\n\n"
                    + "Si no solicitaste este cambio, puedes ignorar este mensaje.\n\n"
                    + "Take Care Of Your Mind Health"
            );

            String contenidoJson = objectMapper.writeValueAsString(mensaje);

            HttpRequest solicitud = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .timeout(Duration.ofSeconds(30))
                    .header("accept", "application/json")
                    .header("api-key", apiKey)
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(contenidoJson))
                    .build();

            HttpResponse<String> respuesta = httpClient.send(
                    solicitud,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (respuesta.statusCode() < 200 || respuesta.statusCode() >= 300) {
                throw new IllegalStateException(
                        "Brevo no pudo enviar el correo. Código HTTP: "
                        + respuesta.statusCode()
                );
            }

            System.out.println("Correo de recuperación enviado correctamente.");

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Error al preparar o enviar el correo mediante Brevo.", e
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "El envío del correo fue interrumpido.", e
            );
        }
    }
}
