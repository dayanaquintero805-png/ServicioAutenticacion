
package com.example.servicioautenticacion.controller;

import com.example.servicioautenticacion.ServicioContrasenas;
import com.example.servicioautenticacion.Usuario;
import com.example.servicioautenticacion.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final ServicioContrasenas servicioContrasenas;

    public AuthController(
            UsuarioRepository usuarioRepository,
            ServicioContrasenas servicioContrasenas) {
        this.usuarioRepository = usuarioRepository;
        this.servicioContrasenas = servicioContrasenas;
    }

    @PostMapping("/registro")
    public ResponseEntity<Map<String, String>> registrar(
            @RequestBody Usuario usuario) {

        if (usuario.getUsuario() == null
                || usuario.getUsuario().isBlank()
                || usuario.getContrasena() == null
                || usuario.getContrasena().isBlank()) {

            return ResponseEntity.badRequest().body(
                    Map.of("mensaje",
                            "El usuario y la contraseña son obligatorios."));
        }

        if (usuario.getUsuario().length() > 100
                || usuario.getContrasena().length() < 8
                || usuario.getContrasena().length() > 72) {

            return ResponseEntity.badRequest().body(
                    Map.of("mensaje",
                            "El usuario debe tener máximo 100 caracteres y "
                            + "la contraseña entre 8 y 72 caracteres."));
        }

        if (usuarioRepository.existsByUsuario(usuario.getUsuario().trim())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("mensaje", "El usuario ya está registrado."));
        }

        Usuario nuevoUsuario = new Usuario(
                usuario.getUsuario().trim(),
                servicioContrasenas.codificar(usuario.getContrasena()));

        usuarioRepository.save(nuevoUsuario);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("mensaje", "Usuario registrado correctamente."));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> iniciarSesion(
            @RequestBody Usuario usuario) {

        if (usuario.getUsuario() == null
                || usuario.getUsuario().isBlank()
                || usuario.getContrasena() == null
                || usuario.getContrasena().isBlank()) {

            return ResponseEntity.badRequest().body(
                    Map.of("mensaje",
                            "El usuario y la contraseña son obligatorios."));
        }

        return usuarioRepository.findByUsuario(usuario.getUsuario().trim())
                .filter(guardado -> servicioContrasenas.verificar(
                        usuario.getContrasena(),
                        guardado.getContrasena()))
                .map(guardado -> ResponseEntity.ok(
                        Map.of("mensaje", "Autenticación satisfactoria.")))
                .orElseGet(() -> ResponseEntity.status(
                        HttpStatus.UNAUTHORIZED).body(
                        Map.of("mensaje",
                                "Error en la autenticación.")));
    }
}
