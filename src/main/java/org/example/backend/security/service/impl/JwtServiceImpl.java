package org.example.backend.security.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.example.backend.security.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    @Override
    public String generateJwt(String id) {
        return Jwts.builder()
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .issuedAt(new Date())
                .signWith(signWithKey())
                .subject(id)
                .compact();
    }
    @Override
    public String generateRefreshJwt(String id) {
        return Jwts.builder()
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60*60*24))
                .issuedAt(new Date())
                .signWith(signWithKey())
                .subject(id)
                .compact();
    }
    @Override
    public SecretKey signWithKey(){
        final String SECRET_KEY="nimabolgandahamfaqatvafaqatolgailoveprogramming";
        byte[] decode = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(decode);
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
        String jwt = generateJwt(id);
        return ResponseEntity.ok(jwt);

    }
}
