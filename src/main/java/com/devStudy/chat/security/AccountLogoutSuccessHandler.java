package com.devStudy.chat.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AccountLogoutSuccessHandler implements LogoutSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(AccountLogoutSuccessHandler.class);

    private static final ObjectMapper mapper = new ObjectMapper();
    /**
     * Redirect vers la page de login
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        if (authentication != null && authentication.getDetails() != null) {
            logger.info("User {} logged out successfully.", authentication.getName());
        }
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        //response.sendRedirect("/login");
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("msg", "logout successfully !");
        
        response.getWriter().write(mapper.writeValueAsString(result));
    }
}