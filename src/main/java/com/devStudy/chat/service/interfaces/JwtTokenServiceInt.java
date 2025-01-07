package com.devStudy.chat.service.interfaces;

public interface JwtTokenServiceInt {
	
    String generateJwtToken(String email);
        
    boolean validateToken(String token);
    
    String validateTokenAndGetEmail(String token);
    
}
