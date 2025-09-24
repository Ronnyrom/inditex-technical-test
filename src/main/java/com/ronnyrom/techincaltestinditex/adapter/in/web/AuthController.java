package com.ronnyrom.techincaltestinditex.adapter.in.web;

import com.ronnyrom.techincaltestinditex.application.port.in.AuthUseCase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthUseCase authUseCase;

    public AuthController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> credentials) {
        String token = authUseCase.login(credentials.get("username"), credentials.get("password"));

        return Map.of("token", token);
    }
}