package com.skyways.controller;

import com.skyways.dto.AuthResponseDTO;
import com.skyways.dto.LoginDTO;
import com.skyways.dto.UserDTO;
import com.skyways.entity.User;
import com.skyways.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private static final Logger logger =
        LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok(
            "SkyWays Airlines API is running!");
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody UserDTO userDTO) {
        logger.info("Register request for: {}", 
            userDTO.getUsername());
        String result = authService.registerUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(result);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginDTO loginDTO) {
        logger.info("Login request for: {}", 
            loginDTO.getUsername());
        AuthResponseDTO response = authService.loginUser(loginDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }
}