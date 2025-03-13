package com.devStudy.chat.service.implementations;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.devStudy.chat.service.interfaces.JwtTokenServiceInt;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.function.Function;
import javax.crypto.SecretKey;

import static com.devStudy.chat.service.utils.ConstantValues.TOKEN_FLAG_LOGIN;

@Service
public class JwtTokenService implements JwtTokenServiceInt {
	private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenService.class);
	
	@Value("${chatroomApp.jwt.secret}")
	private String secretKey;
	
	@Value("${chatroomApp.jwt.resetPwdTokenExpirationTime}")
	private Long resetPwdTokenExpirationTime;

	@Value("${chatroomApp.jwt.loginTokenExpirationTime}")
	private Long loginTokenExpirationTime;
    
    private SecretKey getSecretKey() {
    	byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    	return Keys.hmacShaKeyFor(keyBytes);
    }

	@Override
	public String generateJwtToken(String email, String tokenFlag) {
		Instant now = Instant.now();
		Instant expiration = now.plusMillis(Objects.equals(tokenFlag, TOKEN_FLAG_LOGIN) ? loginTokenExpirationTime : resetPwdTokenExpirationTime
				* 60 * 1000);

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
            return isExpired(token);
        } catch (SignatureException e) {
            LOGGER.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            LOGGER.error("Invalid JWT token: {}", e.getMessage());
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
			return isExpired(token) ? getSubject(token) : null;
		} catch (SignatureException e) {
            LOGGER.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            LOGGER.error("Invalid JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            LOGGER.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.error("JWT claims string is empty: {}", e.getMessage());
        }
		return null;
	}

	private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = Jwts.parser()
				.verifyWith(getSecretKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
		return claimsResolver.apply(claims);
	}

	@Override
	public Date getExpirationDate(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}

	private String getSubject(String token) {
		return getClaimFromToken(token, Claims::getSubject);
	}

	private boolean isExpired(String token) {
		return !getExpirationDate(token).before(new Date());
	}
}
