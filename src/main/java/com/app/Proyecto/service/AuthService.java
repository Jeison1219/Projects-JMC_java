package com.app.Proyecto.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.Proyecto.model.User;
import com.app.Proyecto.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    
    // ========= ALMACENAMIENTO TEMPORAL DE REGISTROS PENDIENTES =========
    // Guardar datos temporales hasta que se verifique el email
    private Map<String, RegistroPendiente> registrosPendientes = new HashMap<>();

    public static class RegistroPendiente {
        public String email;
        public String nombre;
        public String password;
        public String rol;
        public String ip;
        public String codigo;
        public LocalDateTime codigoExpiracion;
        public int intentosCodigo;
        public LocalDateTime ultimoIntento;

        public RegistroPendiente(String email, String nombre, String password, String rol, String ip, String codigo) {
            this.email = email;
            this.nombre = nombre;
            this.password = password;
            this.rol = rol;
            this.ip = ip;
            this.codigo = codigo;
            this.codigoExpiracion = LocalDateTime.now().plusMinutes(10);
            this.intentosCodigo = 0;
        }
    }

    // ========= VERIFICACIÓN DE EMAIL EN REGISTRO =========

    public boolean enviarCodigoRegistro(String email, String nombre, String password, String rol, String ip) {
        // Validar que no exista en BD
        Optional<User> optional = userRepository.findByEmail(email);
        if (optional.isPresent()) return false;

        // Validar que no haya un registro pendiente del mismo email
        if (registrosPendientes.containsKey(email)) {
            registrosPendientes.remove(email); // Limpiar intento anterior
        }

        // Generar código temporal
        String codigo = String.valueOf(new Random().nextInt(899999) + 100000); // 6 dígitos
        
        // GUARDAR SOLO EN MEMORIA (NO en BD)
        RegistroPendiente registroPendiente = new RegistroPendiente(email, nombre, password, rol, ip, codigo);
        registrosPendientes.put(email, registroPendiente);

        // Enviar email
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Código de verificación - Projects-JMC");
            message.setText("Tu código para verificar tu correo es: " + codigo + "\n\nEste código expira en 10 minutos.");
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            // Si falla el email, eliminar el registro pendiente
            registrosPendientes.remove(email);
            return false;
        }
    }

    public boolean verificarCodigoRegistro(String email, String codigoIngresado, String ipCliente) {
        // Buscar en registros pendientes, NO en BD
        if (!registrosPendientes.containsKey(email)) {
            return false;
        }

        RegistroPendiente registro = registrosPendientes.get(email);

        // Validar IP
        if (!registro.ip.equals(ipCliente)) {
            registro.intentosCodigo++;
            return false;
        }

        // Validar intentos fallidos
        if (registro.intentosCodigo >= 5 &&
            registro.ultimoIntento != null &&
            registro.ultimoIntento.isAfter(LocalDateTime.now().minusMinutes(10))) {
            return false;
        }

        // Verificar código y expiración
        if (registro.codigo.equals(codigoIngresado) &&
            registro.codigoExpiracion.isAfter(LocalDateTime.now())) {
            // ✅ AQUÍ CREAMOS EL USUARIO EN LA BD
            crearUsuarioDesdeRegistroPendiente(registro);
            // Limpiar el registro pendiente
            registrosPendientes.remove(email);
            return true;
        } else {
            registro.intentosCodigo++;
            registro.ultimoIntento = LocalDateTime.now();
            return false;
        }
    }

    // ========= CREAR USUARIO EN BD (SOLO DESPUÉS DE VERIFICAR) =========
    private void crearUsuarioDesdeRegistroPendiente(RegistroPendiente registro) {
        User user = new User();
        user.setEmail(registro.email);
        user.setName(registro.nombre);
        user.setPassword(encoder.encode(registro.password)); // Hashear la contraseña
        user.setRole(registro.rol);
        user.setEmailVerificado(true); // Ya verificado
        user.setCodigoVerificacion(null);
        user.setCodigoExpiracion(null);
        user.setCodigoUsado(false);
        user.setIntentosCodigo(0);
        user.setIpSolicitud(registro.ip);

        userRepository.save(user);
    }

    // ========= RECUPERACIÓN DE CONTRASEÑA =========

    public boolean enviarCodigo(String email, String ip) {
        Optional<User> optional = userRepository.findByEmail(email);
        if (optional.isEmpty()) return false;

        User user = optional.get();
        String codigo = String.valueOf(new Random().nextInt(899999) + 100000); // 6 dígitos

        user.setCodigoVerificacion(codigo);
        user.setCodigoExpiracion(LocalDateTime.now().plusMinutes(10));
        user.setCodigoUsado(false);
        user.setIntentosCodigo(0);
        user.setIpSolicitud(ip);

        userRepository.save(user);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Código de recuperación");
        message.setText("Tu código para recuperar la contraseña es: " + codigo);

        mailSender.send(message);

        return true;
    }

    public boolean verificarCodigo(String email, String codigoIngresado, String ipCliente) {
        Optional<User> optional = userRepository.findByEmail(email);
        if (optional.isEmpty()) return false;

        User user = optional.get();

        if (user.isCodigoUsado()) return false;
        if (!user.getIpSolicitud().equals(ipCliente)) return false;

        if (user.getIntentosCodigo() >= 5 &&
            user.getUltimoIntento().isAfter(LocalDateTime.now().minusMinutes(10))) {
            return false;
        }

        if (user.getCodigoVerificacion().equals(codigoIngresado) &&
            user.getCodigoExpiracion().isAfter(LocalDateTime.now())) {

            user.setCodigoUsado(true);
            user.setIntentosCodigo(0);
            userRepository.save(user);
            return true;

        } else {
            user.setIntentosCodigo(user.getIntentosCodigo() + 1);
            user.setUltimoIntento(LocalDateTime.now());
            userRepository.save(user);
            return false;
        }
    }

    @Transactional
    public void cambiarPassword(String email, String nuevaPassword) {
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setPassword(encoder.encode(nuevaPassword));
        user.setCodigoVerificacion(null);
        user.setCodigoExpiracion(null);
        user.setCodigoUsado(false);
        userRepository.save(user);
    }
}
