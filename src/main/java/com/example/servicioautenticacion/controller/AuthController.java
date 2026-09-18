package com.example.servicioautenticacion.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Almacenamiento temporal de usuarios registrados.
    // Se utiliza un Map para mantener el proyecto sencillo.
    private final Map<String, String> usuarios = new HashMap<>();

    // Servicio para registrar un nuevo usuario.
    @PostMapping("/registro")
    public String registrar(@RequestBody Usuario usuario) {

        // Verifica que el usuario y la contraseña no estén vacíos.
        if (usuario.usuario == null || usuario.usuario.isBlank()
                || usuario.contrasena == null || usuario.contrasena.isBlank()) {

            return "El usuario y la contraseña son obligatorios.";
        }

        // Verifica si el usuario ya existe.
        if (usuarios.containsKey(usuario.usuario)) {
            return "El usuario ya está registrado.";
        }

        // Guarda temporalmente el usuario y la contraseña.
        usuarios.put(usuario.usuario, usuario.contrasena);

        return "Usuario registrado correctamente.";
    }

    // Servicio para iniciar sesión.
    @PostMapping("/login")
    public String iniciarSesion(@RequestBody Usuario usuario) {

        // Busca la contraseña asociada al usuario.
        String contrasenaGuardada = usuarios.get(usuario.usuario);

        // Comprueba que el usuario exista y que la contraseña coincida.
        if (contrasenaGuardada != null
                && contrasenaGuardada.equals(usuario.contrasena)) {

            return "Autenticación satisfactoria.";
        }

        // Si los datos no coinciden, devuelve un mensaje de error.
        return "Error en la autenticación.";
    }

    // Clase utilizada para recibir el usuario y la contraseña
    // enviados en las solicitudes del servicio web.
    public static class Usuario {

        public String usuario;
        public String contrasena;
    }
}