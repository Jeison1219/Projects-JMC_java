package com.app.Proyecto.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.app.Proyecto.model.Proyecto;
import com.app.Proyecto.model.Tarea;
import com.app.Proyecto.model.User;
import com.app.Proyecto.repository.TareaRepository;
import com.app.Proyecto.repository.UserRepository;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TareaService {

    private final TareaRepository tareaRepository;
    private final UserRepository userRepository;
    private final NotificacionService notificacionService;

    @Value("${app.url:https://projects-jmcjava-production.up.railway.app}")
    private String appUrl;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // -------------------------
    // Métodos públicos
    // -------------------------

    // Listar tareas por usuario
    public List<Tarea> listarTareasPorUsuario(User usuario) {
        return tareaRepository.findByUsuario(usuario);
    }

    // Contar por prioridad
    public long contarPorPrioridad(String prioridad) {
        return tareaRepository.countByPrioridad(prioridad);
    }

    public long contarTareasPendientes() {
        return tareaRepository.countByCompletada(false);
    }

    public Map<String, Long> obtenerEstadisticasProgreso() {
        long completadas = tareaRepository.countByCompletadaTrue();
        long pendientes = tareaRepository.countByCompletadaFalse();

        Map<String, Long> estadisticas = new HashMap<>();
        estadisticas.put("completadas", completadas);
        estadisticas.put("pendientes", pendientes);
        return estadisticas;
    }

    // Crear nueva tarea (envía notificación si hay usuario asignado)
    public Tarea crearTarea(Tarea tarea) {
        Tarea guardada = tareaRepository.save(tarea);

        if (guardada.getUsuario() != null) {
            enviarNotificacionTarea(guardada, guardada.getUsuario(), obtenerNombreUsuarioAutenticado());
        }

        return guardada;
    }

    // Eliminar tarea
    public void eliminarTarea(Long id) {
        tareaRepository.deleteById(id);
    }

    // Marcar como completada
    public void marcarComoCompletada(Long id) {
        Tarea tarea = buscarPorId(id);
        tarea.setCompletada(true);
        tareaRepository.save(tarea);
    }

    // Actualizar tarea
    public void actualizar(Long id, Tarea nueva) {
        Tarea existente = buscarPorId(id);
        existente.setTitulo(nueva.getTitulo());
        existente.setDescripcion(nueva.getDescripcion());
        existente.setFechaLimite(nueva.getFechaLimite());
        existente.setPrioridad(nueva.getPrioridad());
        existente.setProyecto(nueva.getProyecto());
        existente.setCompletada(nueva.isCompletada());
        tareaRepository.save(existente);
    }

    // Buscar por ID
    public Tarea buscarPorId(Long id) {
        return tareaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tarea no encontrada con ID: " + id));
    }

    // Asignar usuario a tarea (ahora NOTIFICA al usuario asignado)
    public void asignarUsuario(Long tareaId, Long usuarioId) {
        Tarea tarea = buscarPorId(tareaId);
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + usuarioId));
        tarea.setUsuario(usuario);
        tareaRepository.save(tarea);

        String asignador = obtenerNombreUsuarioAutenticado();
        enviarNotificacionTarea(tarea, usuario, asignador);
    }

    // Listar tareas por proyecto
    public List<Tarea> listarTareasPorProyecto(Proyecto proyecto) {
        return tareaRepository.findByProyecto(proyecto);
    }

    // Buscar con filtros dinámicos
    public List<Tarea> buscarTareasConFiltros(User usuario, String titulo, Boolean completada, String prioridad,
                                              Long proyectoId, java.time.LocalDate fechaInicio, java.time.LocalDate fechaFin) {

    boolean esProjectManager = usuario.getRole() != null && usuario.getRole().equalsIgnoreCase("PROJECT_MANAGER");

        Specification<Tarea> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (!esProjectManager) {
                predicates.add(cb.equal(root.get("usuario"), usuario));
            }

            if (titulo != null && !titulo.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("titulo")), "%" + titulo.toLowerCase() + "%"));
            }

            if (completada != null) {
                predicates.add(cb.equal(root.get("completada"), completada));
            }

            if (prioridad != null && !prioridad.isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("prioridad")), prioridad.toLowerCase()));
            }

            if (proyectoId != null) {
                predicates.add(cb.equal(root.get("proyecto").get("id"), proyectoId));
            }

            if (fechaInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaLimite"), fechaInicio));
            }

            if (fechaFin != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaLimite"), fechaFin));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return tareaRepository.findAll(spec);
    }

    // -------------------------
    // Métodos privados de ayuda
    // -------------------------

    // Construye y envía la notificación usando la plantilla Thymeleaf (plantilla específica para tareas)
    private void enviarNotificacionTarea(Tarea tarea, User destinatarioUsuario, String asignador) {
        try {
            Map<String, Object> vars = new HashMap<>();
            vars.put("assigneeName", destinatarioUsuario.getName());
            vars.put("taskTitle", tarea.getTitulo());
            vars.put("projectName", (tarea.getProyecto() != null && tarea.getProyecto().getNombre() != null)
                    ? tarea.getProyecto().getNombre()
                    : null);
            vars.put("priority", (tarea.getPrioridad() != null) ? tarea.getPrioridad() : "Media");
            vars.put("dueDate", (tarea.getFechaLimite() != null) ? tarea.getFechaLimite().format(DATE_FMT) : "Sin fecha");
            vars.put("taskDescription", escapeHtmlShort(tarea.getDescripcion()));
            vars.put("taskLink", appUrl + "/tareas/detalles/" + tarea.getId());
            vars.put("assignedBy", asignador != null ? asignador : "Sistema");
            vars.put("supportEmail", "projectsjmc91@gmail.com");
            vars.put("appName", "Projects-JMC");

            String subject = "Has sido asignado(a) a la tarea: " + tarea.getTitulo();

            // Usar plantilla específica para tareas
            notificacionService.enviarCorreoConTemplate(destinatarioUsuario.getEmail(), subject, vars, "email/tarea-asignada", true);
        } catch (Exception e) {
            // No queremos que un fallo en notificación rompa la lógica principal
            e.printStackTrace();
        }
    }

    // Obtiene el nombre del usuario autenticado (o "Sistema" si no hay autenticación)
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

    // Sanitiza brevemente texto para mostrar en HTML (convertir saltos de línea y escapar)
    private String escapeHtmlShort(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\n", "<br>");
    }
}
