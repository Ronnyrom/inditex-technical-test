package com.ronnyrom.techincaltestinditex.application.port.out;

public interface TokenGenerator {
    String generateToken(String subject);
}