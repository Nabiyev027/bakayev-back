package org.example.backend.security.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.example.backend.security.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final UserDetailsService userDetailsService;

    private static final String SECRET_KEY = "nimabolgandahamfaqatvafaqatolgailoveprogramming";

    @Override
    public String generateJwt(String id, Authentication authentication) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 1000 * 60 * 60 * 24); // 24 soat

        List<String> authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(id)
                .issuedAt(now)
                .expiration(expiryDate)
                .claim("authorities", authorities)
                .signWith(signWithKey())
                .compact();
    }

    @Override
    public String generateRefreshJwt(String id, Authentication authentication) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 1000 * 60 * 60 * 24); // 24 soat

        return Jwts.builder()
                .subject(id)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signWithKey())
                .compact();
    }

    @Override
    public SecretKey signWithKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    @Override
    public Jws<Claims> extractJwt(String jwt) {
        return Jwts.parser()
                .verifyWith(signWithKey())
                .build()
                .parseSignedClaims(jwt);
    }

    @Override
    public ResponseEntity<?> refreshToken(String refreshToken) {
        String id = extractJwt(refreshToken).getPayload().getSubject();

        UserDetails userDetails = userDetailsService.loadUserByUsername(id);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        String jwt = generateJwt(id, authentication);

        return ResponseEntity.ok(jwt);
    }
}
