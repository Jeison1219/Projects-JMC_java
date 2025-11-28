package com.app.Proyecto.service;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.internet.MimeMessage;

@Service
public class NotificacionService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.from:Projects_JMC_Notifications <no-reply@tudominio.com>}")
    private String defaultFrom;

    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

    public NotificacionService(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    // envío texto plano (compatibilidad)
    @Async
    public void enviarCorreo(String destinatario, String asunto, String mensaje) {
        try {
            org.springframework.mail.SimpleMailMessage email = new org.springframework.mail.SimpleMailMessage();
            email.setTo(destinatario);
            email.setSubject(asunto);
            email.setText(mensaje);
            email.setFrom(defaultFrom);
            mailSender.send(email);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // envío HTML directo
    @Async
    public void enviarCorreoHtml(String destinatario, String asunto, String htmlBody) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setFrom(defaultFrom);
            helper.setText(htmlBody, true);
            mailSender.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Enviar correo usando plantilla Thymeleaf.
     * @param to destinatario
     * @param subject asunto
     * @param variables mapa con variables para la plantilla
     * @param templateName nombre de plantilla Thymeleaf (ej: "email/tarea-asignada" o "email/proyecto-asignado")
     * @param inlineLogo true para incrustar logo desde static/images/logo.png
     */
    @Async
    public void enviarCorreoConTemplate(String to, String subject, Map<String, Object> variables,
                                        String templateName, boolean inlineLogo) {
        try {
            Context context = new Context();
            if (variables != null) context.setVariables(variables);

            context.setVariable("appName", variables != null && variables.get("appName") != null ? variables.get("appName") : "Proyecto");
            context.setVariable("year", LocalDate.now().getYear());
            context.setVariable("appUrl", appUrl);

            // Procesar la plantilla indicada
            String html = templateEngine.process(templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom(defaultFrom);
            helper.setText(html, true);

            if (inlineLogo) {
                try {
                    ClassPathResource logo = new ClassPathResource("static/images/logo.png");
                    if (logo.exists()) {
                        helper.addInline("logoImage", logo);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Overload: templateName + inlineLogo = true
    public void enviarCorreoConTemplate(String to, String subject, Map<String, Object> variables, String templateName) {
        enviarCorreoConTemplate(to, subject, variables, templateName, true);
    }

    // Overload: mantiene compatibilidad si llamas sin template -> usa tarea-asignada por defecto
    public void enviarCorreoConTemplate(String to, String subject, Map<String, Object> variables) {
        enviarCorreoConTemplate(to, subject, variables, "email/tarea-asignada", true);
    }
}
