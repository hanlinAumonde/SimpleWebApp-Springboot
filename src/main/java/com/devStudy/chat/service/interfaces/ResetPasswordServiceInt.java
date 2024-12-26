package com.devStudy.chat.service.interfaces;

import javax.servlet.http.HttpServletRequest;

import com.devStudy.chat.model.ResetPasswordValidate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ResetPasswordServiceInt {
	
	Map<String, String> sendResetPasswordEmail(String email, HttpServletRequest request);
	
    Optional<ResetPasswordValidate> findValidateByToken(String token);
    
    boolean validateToken(String token);

    List<ResetPasswordValidate> findValidatesByUserId(long userId);

    void deleteValidate(ResetPasswordValidate resetPasswordValidate);
    
    boolean resetPassword(String token, String password);
}
