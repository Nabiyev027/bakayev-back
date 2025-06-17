package org.example.backend.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import javax.crypto.SecretKey;

public interface JwtService {
    String generateJwt(String id, Authentication authenticate);
    String generateRefreshJwt(String id,Authentication authenticate);
    SecretKey signWithKey();
    Jws<Claims> extractJwt(String jwt);
    ResponseEntity<?> refreshToken(String refreshToken);
}
