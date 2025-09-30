package com.example.PromptShieldAPI.service;

import com.example.PromptShieldAPI.model.User;
import com.example.PromptShieldAPI.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Carrega detalhes do utilizador para autenticação Spring Security
     * Esta função é crítica para o sistema de autenticação
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilizador não encontrado com o email: " + email));

        // Verificar se a conta está deletada - impede login de contas eliminadas
        if (user.isDeleted()) {
            throw new UsernameNotFoundException("Conta deletada. Não é possível fazer login.");
        }

        // Nota: Não verificamos se está ativo aqui para permitir login
        // A verificação de conta ativa é feita depois no controller

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
