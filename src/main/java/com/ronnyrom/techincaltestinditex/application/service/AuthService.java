package com.ronnyrom.techincaltestinditex.application.service;

import com.ronnyrom.techincaltestinditex.application.port.in.AuthUseCase;
import com.ronnyrom.techincaltestinditex.application.port.out.TokenGenerator;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements AuthUseCase {
    private final TokenGenerator tokenGenerator;

    public AuthService(TokenGenerator tokenGenerator) {
        this.tokenGenerator = tokenGenerator;
    }

    @Override
    public String login(String username, String password) {
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException("Bad Username or Password");
        }
        return tokenGenerator.generateToken(username);
    }
}
