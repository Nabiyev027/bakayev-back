package org.example.backend.security;




import lombok.RequiredArgsConstructor;
import org.example.backend.entity.User;
import org.example.backend.repository.UserRepo;
import org.example.backend.security.filter.Filter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final Filter filter;

    @Bean
public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers("/auth/login", "/auth/refresh").permitAll()
                                .requestMatchers("/file/get").permitAll()
                                .requestMatchers("/uploads/**").permitAll()
                                .requestMatchers("/reference/post").permitAll()
                                .requestMatchers("/reference").permitAll()
                                .requestMatchers("/filial/get").permitAll()
                                .requestMatchers("/headerSection").permitAll()
                                .requestMatchers("/comment").permitAll()
                                .requestMatchers("/differenceSection/get").permitAll()
                                .anyRequest().authenticated()
                ).addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    public BCryptPasswordEncoder encoder(){
        return new BCryptPasswordEncoder();
    }

}
