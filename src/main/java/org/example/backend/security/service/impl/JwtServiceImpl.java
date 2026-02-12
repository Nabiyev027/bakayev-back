package org.example.backend.security.service.impl;

import io.jsonwebtoken.*;

import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.example.backend.security.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final UserDetailsService userDetailsService;

    @Value("${jwt.secret}")
    private String secret;


    @Override
    public SecretKey signWithKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }


    @Override
    public Jws<Claims> extractJwt(String jwt) {
        return Jwts.parser()
                .verifyWith(signWithKey()) // Yangi versiya uchun
                .build()
                .parseSignedClaims(jwt);
    }


    @Override
    public String generateJwt(String id, Authentication authentication) {
        if (id == null) throw new IllegalArgumentException("User ID cannot be null for JWT generation");

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 1000 * 60 * 60 * 24); // 1 kun

        List<String> authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(authentication.getName())
                .issuedAt(now)
                .expiration(expiryDate)
                .claim("userId", id)
                .claim("authorities", authorities)
                .signWith(signWithKey(), Jwts.SIG.HS256) // Yangi versiya sintaksisi
                .compact();
    }

    @Override
    public String generateRefreshJwt(String id, Authentication authentication) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 1000L * 60 * 60 * 24 * 7);

        return Jwts.builder()
                .subject(authentication.getName())
                .claim("userId", id) // Refresh tokenga ham ID ni qo'shdik!
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signWithKey(), Jwts.SIG.HS256) // Yangi versiya sintaksisi
                .compact();
    }


    @Override
    public ResponseEntity<?> refreshToken(String refreshToken) {
        try {
            Claims claims = extractJwt(refreshToken).getPayload();

            String username = claims.getSubject();
            String userId = (String) claims.get("userId");

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(username);

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            // 🔥 YANGI TOKENLAR
            String newAccessToken = generateJwt(userId, authentication);
            String newRefreshToken = generateRefreshJwt(userId, authentication);

            return ResponseEntity.ok(
                    Map.of(
                            "accessToken", newAccessToken,
                            "refreshToken", newRefreshToken
                    )
            );

        } catch (ExpiredJwtException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Refresh token muddati tugagan");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Refresh token yaroqsiz");
        }
    }

}

