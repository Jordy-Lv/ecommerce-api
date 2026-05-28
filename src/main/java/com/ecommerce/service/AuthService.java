package com.ecommerce.service;

import com.ecommerce.dto.request.LoginRequest;
import com.ecommerce.dto.request.RegisterRequest;
import com.ecommerce.dto.response.AuthResponse;
import com.ecommerce.entity.User;
import com.ecommerce.enums.Role;
import com.ecommerce.exception.EmailAlreadyExistsException;
import com.ecommerce.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // Registra un usuario nuevo, por defecto todos son CUSTOMER
    // La contraseña se guarda encriptada con BCrypt
    public AuthResponse register(RegisterRequest request) {
        // Verifico que el email no este repetido
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("El email ya esta registrado: " + request.email());
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.CUSTOMER) // todos entran como cliente normal
                .build();

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved); // genero el token para que no tenga que loguearse

        return new AuthResponse(token, saved.getEmail(), saved.getRole().name());
    }

    // Loguea al usuario con email y contraseña, devuelve el token JWT
    public AuthResponse login(LoginRequest request) {
        // Busco el usuario por email, si no existe tiro error generico por seguridad
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));

        // Comparo la contraseña con la guardada
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Credenciales invalidas");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }
}
