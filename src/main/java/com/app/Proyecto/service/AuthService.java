package com.app.Proyecto.service;

import com.app.Proyecto.model.User;
import com.app.Proyecto.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

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
