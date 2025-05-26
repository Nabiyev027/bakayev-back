package org.example.backend.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.http.ResponseEntity;

import javax.crypto.SecretKey;

public interface JwtService {
    String generateJwt(String id);
    String generateRefreshJwt(String id);
    SecretKey signWithKey();
    Jws<Claims> extractJwt(String jwt);
    ResponseEntity<?> refreshToken(String refreshToken);
}
