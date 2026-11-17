
package com.example.servicioautenticacion;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

@Service
public class ServicioRecuperacion {

    private final TokenRecuperacionRepository tokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public ServicioRecuperacion(
            TokenRecuperacionRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public String generarToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    public String protegerToken(String token) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "No se pudo proteger el token.", e);
        }
    }

    @Transactional
    public void guardarToken(String token, Usuario usuario) {

        List<TokenRecuperacion> anteriores =
                tokenRepository.findAllByUsuarioAndUtilizadoFalse(usuario);

        for (TokenRecuperacion anterior : anteriores) {
            anterior.setUtilizado(true);
        }

        String tokenProtegido = protegerToken(token);

        TokenRecuperacion registro = new TokenRecuperacion(
                tokenProtegido,
                usuario,
                LocalDateTime.now().plusMinutes(15)
        );

        tokenRepository.save(registro);
    }

    @Transactional
    public boolean restablecerContrasena(
            String token,
            String nuevaContrasena,
            ServicioContrasenas servicioContrasenas) {

        String tokenProtegido = protegerToken(token);

        TokenRecuperacion registro = tokenRepository
                .findByToken(tokenProtegido)
                .orElse(null);

        if (registro == null
                || registro.isUtilizado()
                || registro.getFechaExpiracion()
                        .isBefore(LocalDateTime.now())) {
            return false;
        }

        Usuario usuario = registro.getUsuario();

        usuario.setContrasena(
                servicioContrasenas.codificar(nuevaContrasena));

        registro.setUtilizado(true);

        tokenRepository.save(registro);

        return true;
    }
}
