package com.devStudy.chat.service.implementations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.devStudy.chat.service.interfaces.JwtTokenServiceInt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;

@Service
public class JwtTokenService implements JwtTokenServiceInt {
	private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenService.class);
	
	@Value("${chatroomApp.jwt.secret}")
	private String secretKey;
	
	@Value("${chatroomApp.jwt.expirationTime}")
	private Long expirationTime;
    
    private SecretKey getSecretKey() {
    	byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    	return Keys.hmacShaKeyFor(keyBytes);
    }

	@Override
	public String generateJwtToken(String email) {
		Instant now = Instant.now();
		Instant expiration = now.plusMillis(expirationTime * 60 * 1000);

        return Jwts.builder()
                .subject(email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getSecretKey())
                .compact();
	}

	@Override
	public boolean validateToken(String token) {
		try {
            Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (SignatureException e) {
            LOGGER.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            LOGGER.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            LOGGER.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            LOGGER.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
	}
	
	@Override
	public String validateTokenAndGetEmail(String token) {
		try {
			return Jwts.parser()
					.verifyWith(getSecretKey())
	                .build()
	                .parseSignedClaims(token)
	                .getPayload()
	                .getSubject();
		} catch (SignatureException e) {
            LOGGER.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            LOGGER.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            LOGGER.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            LOGGER.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.error("JWT claims string is empty: {}", e.getMessage());
        }
		return "";
	}
}
