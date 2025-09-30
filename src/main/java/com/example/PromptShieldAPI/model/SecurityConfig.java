package com.example.PromptShieldAPI.model;

import com.example.PromptShieldAPI.service.CustomUserDetailsService;
import com.example.PromptShieldAPI.service.CustomAuthenticationFailureHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {
    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationFailureHandler authenticationFailureHandler;

    /**
     * Configuração principal de segurança da aplicação
     * Esta configuração é crítica pois define quem pode aceder a quê
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeHttpRequests()
                // URLs públicas - qualquer pessoa pode aceder
                .requestMatchers("/", "/auth/register", "/auth/login", "/swagger-ui/**", "/v3/**", "/files/**", "/css/**", "/api/auth/test-email", "/api/auth/register", "/api/auth/login", "/admin/llm-status", "/admin/llm-status-simple", "/admin/llm-user-prefs", "/admin/llm-maintenance-status").permitAll()
                // URLs de administração - apenas admins
                .requestMatchers("/admin/**", "/auth/delete/**").hasRole("ADMIN")
                // URLs que requerem autenticação mas não admin
                .requestMatchers("/check-account-status", "/account-disabled", "/account-deleted", "/chat/trash").authenticated()
                // Todas as outras URLs requerem autenticação
                .anyRequest().authenticated()
                .and()
                // Configuração do formulário de login
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/check-account-status", true)
                        .failureHandler(authenticationFailureHandler)
                        .permitAll()
                )
                // Configuração do logout
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/auth/login")
                        .permitAll()
                )
                // Tratamento de exceções para requisições AJAX
                .exceptionHandling(exception -> exception
                    .defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        request -> "XMLHttpRequest".equals(request.getHeader("X-Requested-With")) ||
                                   (request.getHeader("Accept") != null && request.getHeader("Accept").contains("application/json"))
                    )
                );
        return http.build();
    }


    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class).userDetailsService(userDetailsService).passwordEncoder(passwordEncoder()).and().build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}