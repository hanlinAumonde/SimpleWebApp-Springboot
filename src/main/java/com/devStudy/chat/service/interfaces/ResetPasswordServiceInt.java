package com.devStudy.chat.service.interfaces;

import javax.servlet.http.HttpServletRequest;
import javax.swing.text.html.Option;

import org.springframework.web.bind.annotation.ResponseBody;

import com.devStudy.chat.dto.ResponseDTO;
import com.devStudy.chat.model.ResetPasswordValidate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResetPasswordServiceInt {
	ResponseDTO sendResetPasswordEmail(String email, HttpServletRequest request);
	
    Optional<ResetPasswordValidate> findValidateByToken(String token);
    
    boolean validateToken(String token);

    List<ResetPasswordValidate> findValidatesByUserId(long userId);

    void deleteValidate(ResetPasswordValidate resetPasswordValidate);
    
    boolean resetPassword(String token, String password);
}
