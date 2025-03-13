package com.devStudy.chat.controller;

import com.devStudy.chat.model.User;
import com.devStudy.chat.service.implementations.BlackListService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.devStudy.chat.dto.CreateCompteDTO;
import com.devStudy.chat.dto.UserDTO;
import com.devStudy.chat.service.implementations.JwtTokenService;
import com.devStudy.chat.service.implementations.UserService;

import java.util.Map;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/api/login")
public class LoginManageController {
	private static final Logger LOGGER = LoggerFactory.getLogger(LoginManageController.class);

	@Resource
	private UserService userService;

	@Resource
	private JwtTokenService jwtTokenService;

	@Resource
	private BlackListService blackListService;
	
	/**
     * Cette méthode permet d'obtenir le les informations de l'utilisateur connecté
     */
    @GetMapping("/check-login")
    public ResponseEntity<UserDTO> getLoggedUser(HttpServletRequest request){
		final String authHeader = request.getHeader("Authorization");
		if(authHeader != null && authHeader.startsWith("Bearer ")) {
			final String token = authHeader.substring(7);
			final String email = jwtTokenService.validateTokenAndGetEmail(token);
			if(email != null) {
				UserDTO user = userService.getLoggedUser(email);
				if(user.getId() == 0) {
					LOGGER.error("User not found, but token is valid");
					blackListService.addTokenToBlackList(token, jwtTokenService.getExpirationDate(token).getTime());
				}
				return ResponseEntity.ok(user);
			}
		}
		return ResponseEntity.ok(new UserDTO());
    }

	/**
	 * Cette méthode permet de traiter le formulaire de saisie du mail pour
	 * réinitialiser le mot de passe Elle envoie un mail de réinitialisation de mot
	 * de passe à l'adresse mail saisie si l'adresse mail est valide
	 */
	@PostMapping(value = "/forget-password")
	public ResponseEntity<Map<String, String>> postForgetPasswordPage(@RequestParam(value = "email") String email) {
		//return ResponseEntity.ok(userService.sendResetPasswordEmail(email));
		return ResponseEntity.ok(userService.sendResetPwdEmailRequestToMQ(email));
	}

	/*
	 * Cette méthode permet de verifier le token de réinitialisation de mot de passe
	 */
	@GetMapping(value = "/validate-token")
	public ResponseEntity<Boolean> validateToken(@RequestParam(value = "token") String token) {
	    return ResponseEntity.ok(jwtTokenService.validateToken(token));
	}
	
	/**
	 * Cette méthode permet de réinitialiser le mot de passe
	 */
	@PutMapping(value = "/reset-password")
	public ResponseEntity<Boolean> resetPassword(@RequestParam(value = "token") String token,
            @RequestParam(value = "password") String password) {
        return ResponseEntity.ok(userService.resetPassword(token, password));		
	}
	
	@PostMapping(value = "/compte/create")
	public ResponseEntity<CreateCompteDTO> createUserCompte(@RequestBody CreateCompteDTO createCompteDTO){
		return ResponseEntity.ok(userService.addUser(createCompteDTO));
	}

	@PostMapping(value = "/logout")
	public ResponseEntity<?> logout(HttpServletRequest request){
		final String authHeader = request.getHeader("Authorization");
		String resultMsg;
		if(authHeader != null && authHeader.startsWith("Bearer ")) {
			final String jwtToken = authHeader.substring(7);
			if(jwtTokenService.validateToken(jwtToken))
				blackListService.addTokenToBlackList(jwtToken, jwtTokenService.getExpirationDate(jwtToken).getTime());
			resultMsg = "Logout successful";
		}else{
			resultMsg = "Unnecessary logout, you are not logged in";
		}
		return ResponseEntity.ok(Map.ofEntries(
				Map.entry("message", resultMsg)
		));
	}
}
