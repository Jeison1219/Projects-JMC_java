package com.app.Proyecto.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.app.Proyecto.model.Proyecto;
import com.app.Proyecto.repository.ProyectoRepository;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProyectoService {

    private static final Logger logger = LoggerFactory.getLogger(ProyectoService.class);

    private final ProyectoRepository proyectoRepository;
    private final NotificacionService notificacionService;

    public List<Proyecto> listarTodos() {
        return proyectoRepository.findAll();
    }
    

    public List<Proyecto> buscarProyectos(String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
        return proyectoRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (nombre != null && !nombre.isEmpty() && !nombre.equalsIgnoreCase("Todos los proyectos")) {
                predicates.add(cb.like(cb.lower(root.get("nombre")), "%" + nombre.toLowerCase() + "%"));
            }

            if (fechaInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaInicio"), fechaInicio));
            }

            if (fechaFin != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaFin"), fechaFin));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }

    public Proyecto guardar(Proyecto proyecto) {
        Proyecto guardado = proyectoRepository.save(proyecto);
        // Notificar a los miembros
        if (guardado.getMiembros() != null) {
            for (var miembro : guardado.getMiembros()) {
                notificacionService.enviarCorreo(
                    miembro.getEmail(),
                    "¡Has sido agregado a un proyecto!",
                    "Hola " + miembro.getName() + ",\n\nHas sido agregado al proyecto: " + guardado.getNombre() + ".\n\n¡Bienvenido!"
                );
            }
        }
        return guardado;
    }

    public Proyecto buscarPorId(Long id) {
        Proyecto proyecto = proyectoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado con ID: " + id));
        // Inicializar miembros para evitar LazyInitializationException
        Hibernate.initialize(proyecto.getMiembros());
        return proyecto;
    }

    public void actualizar(Long id, Proyecto datosActualizados) {
        Proyecto existente = buscarPorId(id);
        existente.setNombre(datosActualizados.getNombre());
        existente.setFechaInicio(datosActualizados.getFechaInicio());
        existente.setFechaFin(datosActualizados.getFechaFin());

        // Detectar miembros nuevos
        List<com.app.Proyecto.model.User> miembrosAntes = existente.getMiembros();
        List<com.app.Proyecto.model.User> miembrosDespues = datosActualizados.getMiembros();
        existente.setMiembros(miembrosDespues);
        proyectoRepository.save(existente);
        logger.info("Miembros antes: {}", miembrosAntes);
        logger.info("Miembros después: {}", miembrosDespues);
        if (miembrosDespues != null) {
            for (var miembro : miembrosDespues) {
                boolean esNuevo = miembrosAntes == null || miembrosAntes.stream().noneMatch(u -> u.getId().equals(miembro.getId()));
                logger.info("¿{} es nuevo? {}", miembro.getEmail(), esNuevo);
                if (esNuevo) {
                    logger.info("Enviando correo a {}...", miembro.getEmail());
                    notificacionService.enviarCorreo(
                        miembro.getEmail(),
                        "¡Has sido agregado a un proyecto!",
                        "Hola " + miembro.getName() + ",\n\nHas sido agregado al proyecto: " + existente.getNombre() + ".\n\n¡Bienvenido!"
                    );
                }
            }
        }
    }

    public void eliminar(Long id) {
        proyectoRepository.deleteById(id);
    }
}
