package com.devStudy.chat.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AccountAuthenticationFailureHandler implements AuthenticationFailureHandler {
    
	private static final ObjectMapper mapper = new ObjectMapper();
	/**
     * Redirect vers la page de login, avec un message d'erreur
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        /*
    	String error = exception.getMessage();
        request.getSession().setAttribute("error", error);
        response.sendRedirect("/login");
        */
    	response.setContentType("application/json;charset=UTF-8");
    	response.setStatus(HttpServletResponse.SC_OK);
    	
    	Map<String, Object> result = new HashMap<>();
        result.put("status", "error");
        result.put("msg", "Failed login : " + exception.getMessage());
        
        response.getWriter().write(mapper.writeValueAsString(result));
    }
}