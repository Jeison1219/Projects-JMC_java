package com.app.Proyecto.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.app.Proyecto.model.Proyecto;
import com.app.Proyecto.repository.ProyectoRepository;
import com.app.Proyecto.repository.UserRepository;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProyectoService {

    private static final Logger logger = LoggerFactory.getLogger(ProyectoService.class);

    private final ProyectoRepository proyectoRepository;
    private final UserRepository userRepository;
    private final NotificacionService notificacionService;

    // Base URL para links en los emails (configurable en application.properties)
    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // -------------------------
    // Métodos públicos
    // -------------------------

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

    /**
     * Guarda el proyecto y notifica a los miembros (usando plantilla HTML).
     */
    public Proyecto guardar(Proyecto proyecto) {
        Proyecto guardado = proyectoRepository.save(proyecto);

        if (guardado.getMiembros() != null) {
            for (var miembro : guardado.getMiembros()) {
                try {
                    Map<String, Object> vars = crearVariablesProyecto(guardado);
                    vars.put("assigneeName", miembro.getName());
                    vars.put("assignedBy", "Sistema");
                    vars.put("supportEmail", "soporte@miapp.com");
                    vars.put("appName", "Proyecto JMC");

                    String subject = "Has sido añadido(a) al proyecto: " + guardado.getNombre();
                    notificacionService.enviarCorreoConTemplate(miembro.getEmail(), subject, vars, "email/proyecto-asignado", true);
                } catch (Exception e) {
                    logger.error("Error al notificar a {} sobre proyecto {}: {}", miembro.getEmail(), guardado.getId(), e.getMessage(), e);
                }
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

    /**
     * Actualiza el proyecto y notifica solo a los miembros nuevos (usando plantilla).
     */
    public void actualizar(Long id, Proyecto datosActualizados) {
        Proyecto existente = buscarPorId(id);
        existente.setNombre(datosActualizados.getNombre());
        existente.setFechaInicio(datosActualizados.getFechaInicio());
        existente.setFechaFin(datosActualizados.getFechaFin());

        // Detectar miembros nuevos
        List<com.app.Proyecto.model.User> miembrosAntes = existente.getMiembros() != null ? new ArrayList<>(existente.getMiembros()) : null;
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
                    try {
                        Map<String, Object> vars = crearVariablesProyecto(existente);
                        vars.put("assigneeName", miembro.getName());
                        vars.put("assignedBy", obtenerNombreUsuarioAutenticado());
                        vars.put("supportEmail", "projectsjmc91@gmail.com");
                        vars.put("appName", "Projects-JMC");

                        String subject = "Has sido agregado(a) al proyecto: " + existente.getNombre();

                        notificacionService.enviarCorreoConTemplate(miembro.getEmail(), subject, vars, "email/proyecto-asignado", true);
                    } catch (Exception e) {
                        logger.error("Error notificando miembro nuevo {} del proyecto {}: {}", miembro.getEmail(), existente.getId(), e.getMessage(), e);
                    }
                }
            }
        }
    }

    public void eliminar(Long id) {
        proyectoRepository.deleteById(id);
    }

    /**
     * Asigna un usuario al proyecto (añadiéndolo a miembros si no existe) y notifica al usuario.
     */
    public void asignarUsuarioAProyecto(Long proyectoId, Long usuarioId) {
        Proyecto proyecto = buscarPorId(proyectoId);
        var usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + usuarioId));

        if (proyecto.getMiembros() == null) {
            proyecto.setMiembros(new ArrayList<>());
        }

        boolean yaMiembro = proyecto.getMiembros().stream()
                .anyMatch(u -> u.getId().equals(usuario.getId()));

        if (!yaMiembro) {
            proyecto.getMiembros().add(usuario);
            proyectoRepository.save(proyecto);
        }

        // Enviar notificación al usuario (incluso si ya era miembro, opcional)
        try {
            Map<String, Object> vars = crearVariablesProyecto(proyecto);
            vars.put("assigneeName", usuario.getName());
            vars.put("assignedBy", obtenerNombreUsuarioAutenticado());
            vars.put("supportEmail", "projectsjmc91@gmail.com");
            vars.put("appName", "Projects-JMC");

            String subject = "Has sido añadido(a) al proyecto: " + proyecto.getNombre();

            notificacionService.enviarCorreoConTemplate(usuario.getEmail(), subject, vars, "email/proyecto-asignado", true);
        } catch (Exception e) {
            logger.error("Error al notificar a {} sobre proyecto {}: {}", usuario.getEmail(), proyecto.getId(), e.getMessage(), e);
        }
    }

    // -------------------------
    // Helpers privados
    // -------------------------

    private Map<String, Object> crearVariablesProyecto(Proyecto proyecto) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("projectName", proyecto.getNombre());
        vars.put("projectDescription", proyecto.getDescripcion() != null ? proyecto.getDescripcion() : "");
        vars.put("startDate", proyecto.getFechaInicio() != null ? proyecto.getFechaInicio().format(DATE_FMT) : "Sin fecha");
        vars.put("endDate", proyecto.getFechaFin() != null ? proyecto.getFechaFin().format(DATE_FMT) : "Sin fecha");
        vars.put("projectLink", appUrl + "/proyectos/detalles/" + proyecto.getId());
        return vars;
    }

    // Helper para obtener el nombre del usuario autenticado (o "Sistema" si no hay)
    private String obtenerNombreUsuarioAutenticado() {
        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                return auth.getName();
            }
        } catch (Exception e) {
            // ignore
        }
        return "Sistema";
    }
}
