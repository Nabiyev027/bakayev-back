package org.example.backend.security.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.backend.entity.User;
import org.example.backend.repository.UserRepo;
import org.example.backend.security.service.JwtService;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Configuration
@RequiredArgsConstructor
public class Filter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepo userRepo;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("key");
        System.out.println("Authorization: " + authorization);
        if(authorization!=null){
            Jws<Claims> claimsJws = jwtService.extractJwt(authorization);
            Claims user = claimsJws.getPayload();
            String id = user.getSubject();

            User user1 = userRepo.findById(UUID.fromString(id)).orElseThrow();
            UsernamePasswordAuthenticationToken usn = new UsernamePasswordAuthenticationToken(
                    user1.getUsername(),
                    user1.getPassword(),
                    user1.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(usn);
        }
        filterChain.doFilter(request,response);
    }
}
