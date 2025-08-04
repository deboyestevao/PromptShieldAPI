package com.example.PromptShieldAPI.service;

import com.example.PromptShieldAPI.dto.LoginRequest;
import com.example.PromptShieldAPI.dto.RegisterRequest;
import com.example.PromptShieldAPI.model.User;
import com.example.PromptShieldAPI.model.UserPreferences;
import com.example.PromptShieldAPI.repository.UserRepository;
import com.example.PromptShieldAPI.repository.UserPreferencesRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserPreferencesRepository userPreferencesRepository;
    // Removido: private final JavaMailSender mailSender;

    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        try {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
            Authentication authentication = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Atualizar last_login_at e marcar como online
            String email = request.getEmail();
            User user = userRepository.findByEmail(email).orElse(null);
            
            if (user != null) {
                user.setLastLoginAt(java.time.LocalDateTime.now());
                user.setLastActive(java.time.LocalDateTime.now());
                userRepository.save(user);
                
                // Armazenar username na sessão
                session.setAttribute("username", user.getUsername());
            }

            // Buscar o username do utilizador
            String username = user != null ? user.getUsername() : "User";
            
            return ResponseEntity.ok("Welcome, " + username + "! Login successful.");

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials.");
        }
    }

    @Transactional
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // Validar se as passwords coincidem
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("As senhas não coincidem.");
        }

        // Gerar username: inicial do primeiro nome + último nome, tudo minúsculo
        String baseUsername = (request.getFirstName().substring(0, 1) + request.getLastName()).toLowerCase().replaceAll("\\s+", "");
        String username = baseUsername;
        int count = 1;
        while (userRepository.findByUsername(username).isPresent()) {
            username = baseUsername + count;
            count++;
        }
        // Verificar se já existe utilizador com o mesmo email
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Já existe um utilizador com este email.");
        }

        // Criar utilizador imediatamente
        User user = new User();
        user.setUsername(username);
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setActive(true);
        user.setRole("USER");
        userRepository.save(user);

        // Criar UserPreferences por padrão
        UserPreferences userPreferences = new UserPreferences();
        userPreferences.setUser(user);
        userPreferences.setOpenaiPreferred(true);
        userPreferences.setOllamaPreferred(false);
        userPreferencesRepository.save(userPreferences);

        String mensagem = "Registo efetuado com sucesso. O seu username é: " + username;
        if (!username.equals(baseUsername)) {
            mensagem += " (O username base já existia, por isso foi atribuído um username alternativo.)";
        }
        return ResponseEntity.ok(mensagem);
    }

    // Remover métodos sendVerificationCode e finalizeRegister

    @Transactional
    public void delete(@PathVariable Long id) {
        try {
            userRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
        }
    }

    @Transactional
    public void updateLastLogin(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setLastLoginAt(java.time.LocalDateTime.now());
            user.setLastActive(java.time.LocalDateTime.now());
            userRepository.save(user);
        });
    }
}
