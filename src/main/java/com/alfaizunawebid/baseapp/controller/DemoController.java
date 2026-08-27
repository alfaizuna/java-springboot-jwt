package com.alfaizunawebid.baseapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alfaizunawebid.baseapp.model.User;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/demo")
public class DemoController {

    /**
     * Endpoint yang bisa diakses semua user yang sudah login
     * GET /api/v1/demo/hello
     */
    @GetMapping("/hello")
    public ResponseEntity<Map<String, Object>> hello() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        return ResponseEntity.ok(Map.of(
                "message", "Hello, " + currentUser.getName() + "!",
                "email", currentUser.getEmail(),
                "role", currentUser.getRole().name(),
                "authorities", auth.getAuthorities().toString()
        ));
    }

    /**
     * Endpoint khusus ADMIN
     * GET /api/v1/demo/admin
     */
    @GetMapping("/admin")
    public ResponseEntity<Map<String, String>> adminOnly() {
        return ResponseEntity.ok(Map.of(
                "message", "Welcome, Admin! This is a restricted area."
        ));
    }
}
