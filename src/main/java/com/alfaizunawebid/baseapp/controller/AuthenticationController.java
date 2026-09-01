package com.alfaizunawebid.baseapp.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alfaizunawebid.baseapp.dto.AuthenticationRequest;
import com.alfaizunawebid.baseapp.dto.AuthenticationResponse;
import com.alfaizunawebid.baseapp.dto.RefreshTokenRequest;
import com.alfaizunawebid.baseapp.dto.RegisterRequest;
import com.alfaizunawebid.baseapp.service.AuthenticationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    /**
     * Register user baru
     * POST /api/v1/auth/register
     * Body: { "name": "John", "email": "john@example.com", "password": "secret123" }
     */
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
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
            @RequestBody AuthenticationRequest request
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
            @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(authenticationService.refreshToken(request));
    }

    /**
     * Logout user dengan me-revoke refresh token
     * POST /api/v1/auth/logout
     * Body: { "refreshToken": "uuid-token" }
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestBody RefreshTokenRequest request
    ) {
        authenticationService.logout(request);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
