
package com.example.servicioautenticacion.controller;

import com.example.servicioautenticacion.ServicioContrasenas;
import com.example.servicioautenticacion.ServicioCorreo;
import com.example.servicioautenticacion.ServicioRecuperacion;
import com.example.servicioautenticacion.Usuario;
import com.example.servicioautenticacion.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final ServicioContrasenas servicioContrasenas;
    private final ServicioCorreo servicioCorreo;
    private final ServicioRecuperacion servicioRecuperacion;

    public AuthController(
            UsuarioRepository usuarioRepository,
            ServicioContrasenas servicioContrasenas,
            ServicioCorreo servicioCorreo,
            ServicioRecuperacion servicioRecuperacion) {

        this.usuarioRepository = usuarioRepository;
        this.servicioContrasenas = servicioContrasenas;
        this.servicioCorreo = servicioCorreo;
        this.servicioRecuperacion = servicioRecuperacion;
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

        String correo = usuario.getCorreo();

        if (correo == null || correo.isBlank()
                || correo.length() > 150
                || !correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {

            return ResponseEntity.badRequest().body(
                    Map.of("mensaje", "Ingresa un correo electrónico válido."));
        }

        correo = correo.trim().toLowerCase();

        if (usuarioRepository.existsByCorreo(correo)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("mensaje", "El correo ya está registrado."));
        }

       

        Usuario nuevoUsuario = new Usuario(
                usuario.getUsuario().trim(),
                servicioContrasenas.codificar(usuario.getContrasena()),
                correo
        );

        usuarioRepository.save(nuevoUsuario);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("mensaje", "Usuario registrado correctamente."));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> iniciarSesion(
            @RequestBody Usuario usuario,
            jakarta.servlet.http.HttpSession sesion) {

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
                .map(guardado -> {
                    sesion.setAttribute("usuarioAutenticado",
                            guardado.getUsuario());

                    return ResponseEntity.ok(
                            Map.of("mensaje", "Autenticación satisfactoria."));
                })
                .orElseGet(() -> ResponseEntity.status(
                        HttpStatus.UNAUTHORIZED).body(
                        Map.of("mensaje", "Error en la autenticación.")));
    }

    @PostMapping("/recuperar")
    public ResponseEntity<Map<String, String>> recuperarContrasena(
            @RequestBody Map<String, String> datos) {

        String correo = datos.get("correo");

        if (correo == null || correo.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("mensaje", "El correo es obligatorio."));
        }

        String correoNormalizado = correo.trim().toLowerCase();

        var usuarioEncontrado = usuarioRepository.findByCorreo(correoNormalizado);

        System.out.println("¿Usuario encontrado?: "
                + usuarioEncontrado.isPresent());

        usuarioEncontrado.ifPresent(usuario -> {

            System.out.println("Usuario encontrado. Generando token...");

            String token = servicioRecuperacion.generarToken();

            servicioRecuperacion.guardarToken(token, usuario);

            String enlace =
                    "https://dayanaquintero805-png.github.io/TakeCareWebNueva/"
                    + "?token=" + token;

            System.out.println("Intentando enviar correo...");

            servicioCorreo.enviarCorreoRecuperacion(
                    usuario.getCorreo(), enlace);

            System.out.println("Proceso de envío finalizado.");
        });

        return ResponseEntity.ok(
                Map.of("mensaje",
                        "Si el correo está registrado, recibirás instrucciones para recuperar tu contraseña."));
    }

    @PostMapping("/restablecer")
    public ResponseEntity<Map<String, String>> restablecerContrasena(
            @RequestBody Map<String, String> datos) {

        String token = datos.get("token");
        String nuevaContrasena = datos.get("contrasena");

        if (token == null || token.isBlank()
                || nuevaContrasena == null
                || nuevaContrasena.length() < 8
                || nuevaContrasena.length() > 72) {

            return ResponseEntity.badRequest().body(
                    Map.of("mensaje",
                            "El enlace o la nueva contraseña no son válidos."));
        }

        boolean actualizado =
                servicioRecuperacion.restablecerContrasena(
                        token,
                        nuevaContrasena,
                        servicioContrasenas);

        if (!actualizado) {
            return ResponseEntity.badRequest().body(
                    Map.of("mensaje",
                            "El enlace ha expirado o ya fue utilizado."));
        }

        return ResponseEntity.ok(
                Map.of("mensaje",
                        "La contraseña se actualizó correctamente."));
    }
    
        // ==========================================
    // COMPROBAR SESIÓN ACTIVA
    // ==========================================

    @GetMapping("/sesion")
    public ResponseEntity<Map<String, String>> comprobarSesion(
            HttpSession sesion) {

        String usuario = (String) sesion.getAttribute("usuarioAutenticado");

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("mensaje", "La sesión ha expirado."));
        }

        return ResponseEntity.ok(
                Map.of("usuario", usuario));
    }

    @PostMapping("/mantener-sesion")
    public ResponseEntity<Map<String, String>> mantenerSesion(
            HttpSession sesion) {

        String usuario = (String) sesion.getAttribute("usuarioAutenticado");

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("mensaje", "La sesión ha expirado."));
        }

        sesion.setAttribute("usuarioAutenticado", usuario);

        return ResponseEntity.ok(
                Map.of("mensaje", "La sesión continúa activa."));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> cerrarSesion(
            HttpSession sesion) {

        sesion.invalidate();

        return ResponseEntity.ok(
                Map.of("mensaje", "Sesión cerrada correctamente."));
    }
    
 }
