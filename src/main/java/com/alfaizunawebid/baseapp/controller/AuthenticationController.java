package com.alfaizunawebid.baseapp.controller;

import java.util.Base64;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alfaizunawebid.baseapp.dto.AuthenticationRequest;
import com.alfaizunawebid.baseapp.dto.AuthenticationResponse;
import com.alfaizunawebid.baseapp.dto.RefreshTokenRequest;
import com.alfaizunawebid.baseapp.dto.RegisterRequest;
import com.alfaizunawebid.baseapp.service.AuthenticationService;
import com.alfaizunawebid.baseapp.service.JwtService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;

    /**
     * Register user baru
     * POST /api/v1/auth/register
     * Body: { "name": "John", "email": "john@example.com", "password": "secret123" }
     */
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    /**
     * Login user yang sudah terdaftar
     * POST /api/v1/auth/login
     * Body: { "email": "john@example.com", "password": "secret123" }
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    /**
     * Refresh access token menggunakan refresh token
     * POST /api/v1/auth/refresh-token
     * Body: { "refreshToken": "uuid-token" }
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(authenticationService.refreshToken(request));
    }

    /**
     * Logout user:
     * - Blacklist Access Token ke Redis (jika header Authorization diberikan)
     * - Revoke Refresh Token di DB (jika request body refreshToken diberikan)
     * POST /api/v1/auth/logout
     * Header: Authorization: Bearer <accessToken>
     * Body: { "refreshToken": "uuid-token" }
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) RefreshTokenRequest request
    ) {
        authenticationService.logout(authHeader, request);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    /**
     * Mengambil RSA Public Key (PEM format) untuk resource server / verifikasi client
     * GET /api/v1/auth/public-key
     */
    @GetMapping("/public-key")
    public ResponseEntity<Map<String, String>> getPublicKey() {
        String base64Key = Base64.getEncoder().encodeToString(jwtService.getPublicKey().getEncoded());
        String pem = "-----BEGIN PUBLIC KEY-----\n" +
                base64Key.replaceAll("(.{64})", "$1\n") +
                "\n-----END PUBLIC KEY-----";
        return ResponseEntity.ok(Map.of("publicKey", pem));
    }
}
