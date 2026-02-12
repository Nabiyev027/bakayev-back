package org.example.backend.security.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.backend.security.service.JwtService;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Filter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // 1. Ochiq yo'llarni tekshirmaslik (ixtiyoriy, lekin yaxshi praktika)
        String path = request.getRequestURI();
        if (path.contains("/auth/login") || path.contains("/auth/refresh")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Jws<Claims> claimsJws = jwtService.extractJwt(token);
                Claims payload = claimsJws.getPayload();
                String username = payload.getSubject();

                List<String> roles = payload.get("authorities", List.class);
                List<SimpleGrantedAuthority> authorities = roles != null ? roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList()) : List.of();

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (ExpiredJwtException e) {
                // ⭐ MUHIM: Token muddati tugaganda 401 qaytarish
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                response.setContentType("application/json");
                response.getWriter().write("{\"message\": \"Token expired\", \"status\": 401}");
                return; // Jarayonni shu yerda to'xtatamiz!
            } catch (Exception e) {
                // Boshqa xatolar (nauto'g'ri token va h.k.)
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}