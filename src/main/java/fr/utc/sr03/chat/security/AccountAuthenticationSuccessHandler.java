package fr.utc.sr03.chat.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.utc.sr03.chat.dto.DTOMapper;
import fr.utc.sr03.chat.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AccountAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final Logger logger = LoggerFactory.getLogger(AccountAuthenticationSuccessHandler.class);
	
    private static final ObjectMapper mapper = new ObjectMapper();
	/**
     * Redirect vers le chemin d'accueil
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        //response.sendRedirect("/accueil");
    	logger.info("login du user");
        logger.info("user's authorities : " + ((User)authentication.getPrincipal()).getAuthorities());
    	
		response.setContentType("application/json;charset=UTF-8");
		response.setStatus(HttpServletResponse.SC_OK);
		
		Map<String,Object> result = new HashMap<>();
		result.put("status", "success");
		result.put("message", "Login successful");
		//put user info in the result
		User user = (User)authentication.getPrincipal();
		result.put("UserInfo", DTOMapper.toUserDTO(user));
		String json = mapper.writeValueAsString(result);
		logger.info("json : " + json);
		
		response.getWriter().write(json); 
    }
}
