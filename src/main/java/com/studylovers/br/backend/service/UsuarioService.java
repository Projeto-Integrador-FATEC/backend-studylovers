package com.studylovers.br.backend.service;

import com.studylovers.br.backend.model.User;
import com.studylovers.br.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {


    private final UserRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UserRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User autenticar(String email, String senhaDigitada) {
        Optional<User> usuarioOptional = usuarioRepository.findByEmail(email);

        if (usuarioOptional.isPresent()) {
            User usuario = usuarioOptional.get();

            if (usuario.getPassword().equals(senhaDigitada)) {
                return usuario;
            }
        }

        return null;
    }
}
