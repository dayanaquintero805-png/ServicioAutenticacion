
package com.example.servicioautenticacion.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/emociones")
public class EmocionesController {

    // Almacenamiento temporal de los registros emocionales.
    private final List<RegistroEmocional> registros = new ArrayList<>();

    // Servicio para guardar un registro emocional.
    @PostMapping("/registro")
    public synchronized String registrarEmocion(
            @RequestBody RegistroEmocional registro) {

        if (registro.usuario == null || registro.usuario.isBlank()
                || registro.fecha == null || registro.fecha.isBlank()
                || registro.estadoAnimo == null
                || registro.estadoAnimo.isBlank()) {

            return "El usuario, la fecha y el estado de ánimo son obligatorios.";
        }

        if (registro.comentario != null
                && registro.comentario.length() > 250) {

            return "El comentario no puede superar los 250 caracteres.";
        }

        registros.add(registro);

        return "Registro emocional guardado correctamente.";
    }

    // Servicio para consultar el historial emocional.
    @GetMapping("/historial")
    public synchronized List<RegistroEmocional> consultarHistorial(
            @RequestParam(required = false) String usuario) {

        if (usuario == null || usuario.isBlank()) {
            return new ArrayList<>(registros);
        }

        List<RegistroEmocional> historial = new ArrayList<>();

        for (RegistroEmocional registro : registros) {
            if (usuario.equalsIgnoreCase(registro.usuario)) {
                historial.add(registro);
            }
        }

        return historial;
    }

    // Datos que recibe y devuelve el servicio.
    public static class RegistroEmocional {

        public String usuario;
        public String fecha;
        public String estadoAnimo;
        public String comentario;
    }
}

