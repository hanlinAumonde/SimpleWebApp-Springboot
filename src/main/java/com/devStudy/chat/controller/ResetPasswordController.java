package com.devStudy.chat.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.devStudy.chat.dto.ResponseDTO;
import com.devStudy.chat.service.implementations.ResetPasswordService;
import com.devStudy.chat.service.implementations.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/reset-password")
public class ResetPasswordController {
	private static final Logger LOGGER = LoggerFactory.getLogger(ResetPasswordController.class);

	@Resource
	private UserService userService;

	@Resource
	private ResetPasswordService resetPasswordService;

	/**
	 * Cette méthode permet de traiter le formulaire de saisie du mail pour
	 * réinitialiser le mot de passe Elle envoie un mail de réinitialisation de mot
	 * de passe à l'adresse mail saisie si l'adresse mail est valide
	 */
	@PostMapping(value = "/forget-password")
	public ResponseEntity<ResponseDTO> postForgetPasswordPage(@RequestParam(value = "email") String email,
			HttpServletRequest request) {
		return ResponseEntity.ok(resetPasswordService.sendResetPasswordEmail(email, request));
	}

	/*
	 * Cette méthode permet de verifier le token de réinitialisation de mot de passe
	 */
	@GetMapping(value = "/validate-token")
	public ResponseEntity<Boolean> validateToken(@RequestParam(value = "token") String token) {
	    return ResponseEntity.ok(resetPasswordService.validateToken(token));
	}
	
	/**
	 * Cette méthode permet de réinitialiser le mot de passe
	 */
	@PutMapping(value = "/reset-password")
	public ResponseEntity<Boolean> resetPassword(@RequestParam(value = "token") String token,
            @RequestParam(value = "password") String password) {
        return ResponseEntity.ok(resetPasswordService.resetPassword(token, password));		
	}
	
}
