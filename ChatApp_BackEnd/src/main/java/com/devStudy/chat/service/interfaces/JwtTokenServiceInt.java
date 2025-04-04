package com.devStudy.chat.service.interfaces;

import java.util.Date;

public interface JwtTokenServiceInt {
	
    String generateJwtToken(String email, String tokenFlag);
        
    boolean validateToken(String token);
    
    String validateTokenAndGetEmail(String token);

    Date getExpirationDate(String token);
}
