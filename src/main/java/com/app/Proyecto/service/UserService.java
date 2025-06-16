package com.app.Proyecto.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.Proyecto.model.User;
import com.app.Proyecto.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public void register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public boolean login(String username, String rawPassword) {
        User user = userRepository.findByUsername(username);
        return user != null && passwordEncoder.matches(rawPassword, user.getPassword());
    }
}
