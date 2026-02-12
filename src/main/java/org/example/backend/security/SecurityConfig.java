package org.example.backend.security;

import lombok.RequiredArgsConstructor;
import org.example.backend.security.filter.Filter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final Filter filter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // OPTIONS so'rovlariga har doim ruxsat berish (CORS uchun muhim)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/login", "/auth/refresh").permitAll()
                        .requestMatchers("/file/get", "/file/getVideo","/uploads/**").permitAll()
                        .requestMatchers("/filial/**").permitAll()
                        .requestMatchers("/reference/**","/reference/post", "/filial/get").permitAll()
                        .requestMatchers("/headerSection/**", "/differenceSection/home","/footerSection/get", "/aboutSection/**").permitAll()
                        .requestMatchers("/aboutSection/**","/comment/**","/courseSection/**","/teacherSection/getInfo/**,","/teacherSection/**").permitAll()
                        .requestMatchers("/studentSection/**","/lesson/**").permitAll()
                        .requestMatchers("/cardSkill/**","/courseCard/**","/payment/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    // CORS sozlamalari: Frontend (React/Vue) bilan muammosiz bog'lanish uchun
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*")); // Hamma joydan ruxsat (ishlab chiqarishda o'zgartiring)
        configuration.setAllowedOrigins(List.of("http://localhost:3000","http://localhost:3001","http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type", "lang", "key"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // Login jarayonida parolni tekshirish uchun kerak
    @Bean
    public BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    // Controllerda authentication qilish uchun kerak
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}