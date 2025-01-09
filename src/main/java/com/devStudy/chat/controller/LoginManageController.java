package com.devStudy.chat.controller;

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
	
	/**
     * Cette méthode permet d'obtenir le les informations de l'utilisateur connecté
     */
    @GetMapping("/check-login")
    public ResponseEntity<UserDTO> getLoggedUser(HttpServletRequest request){
        if (userService.checkUserLoginStatus()) {
            UserDTO user = userService.getLoggedUser();
            return ResponseEntity.ok(user);
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(!(auth instanceof AnonymousAuthenticationToken)){
            //si l'utilisateur est connecté mais a ete supprimé par l'admin, on doit mauellement supprimer le contexte de login
            //delete login context
            SecurityContextHolder.getContext().setAuthentication(null);
            //invalidate http session
            request.getSession().invalidate();
            LOGGER.info("user {} a ete supprime par l'admin", auth.getName());
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
		return ResponseEntity.ok(userService.sendResetPasswordEmail(email));
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
	
}
