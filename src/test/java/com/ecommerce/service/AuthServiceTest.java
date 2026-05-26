package com.ecommerce.service;

import com.ecommerce.dto.request.LoginRequest;
import com.ecommerce.dto.request.RegisterRequest;
import com.ecommerce.dto.response.AuthResponse;
import com.ecommerce.entity.User;
import com.ecommerce.enums.Role;
import com.ecommerce.exception.EmailAlreadyExistsException;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_success() {
        // given
        RegisterRequest request = new RegisterRequest("Juan", "juan@store.com", "password123");

        when(userRepository.existsByEmail("juan@store.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("token123");

        // when
        AuthResponse response = authService.register(request);

        // then
        assertEquals("token123", response.token());
        assertEquals("juan@store.com", response.email());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals("hashed", saved.getPassword());
        assertEquals(Role.CUSTOMER, saved.getRole());
    }

    @Test
    void register_duplicateEmail() {
        // given
        RegisterRequest request = new RegisterRequest("Juan", "juan@store.com", "password123");
        when(userRepository.existsByEmail("juan@store.com")).thenReturn(true);

        // when / then
        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));
    }

    @Test
    void login_success() {
        // given
        LoginRequest request = new LoginRequest("juan@store.com", "password123");
        User user = User.builder()
                .id(1L)
                .email("juan@store.com")
                .password("hashed")
                .role(Role.CUSTOMER)
                .build();

        when(userRepository.findByEmail("juan@store.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("token123");

        // when
        AuthResponse response = authService.login(request);

        // then
        assertEquals("token123", response.token());
        assertEquals("CUSTOMER", response.role());
    }

    @Test
    void login_wrongPassword() {
        // given
        LoginRequest request = new LoginRequest("juan@store.com", "wrong");
        User user = User.builder()
                .id(1L)
                .email("juan@store.com")
                .password("hashed")
                .role(Role.CUSTOMER)
                .build();

        when(userRepository.findByEmail("juan@store.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        // when / then
        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }
}
