package com.devStudy.chat.security;

import com.devStudy.chat.model.User;
import com.devStudy.chat.service.implementations.BlackListService;
import com.devStudy.chat.service.implementations.JwtTokenService;
import com.devStudy.chat.service.implementations.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenService jwtTokenService;
    private final UserService userService;
    private final BlackListService blackListService;

    private static final Pattern TOKEN_PATTERN = Pattern.compile("token=([A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+)");

    @Autowired
    public JwtAuthenticationFilter(JwtTokenService jwtTokenService, UserService userService, BlackListService blackListService) {
        this.jwtTokenService = jwtTokenService;
        this.userService = userService;
        this.blackListService = blackListService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        String jwtToken = null;
        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.substring(7);
        }else if(authHeader == null && request.getQueryString() != null) {
            //Temporairement utiliser url paramètre pour obtenir le token
            //TODO: changer le façon de passer le token
            logger.info(request.getQueryString());
            Matcher matcher = TOKEN_PATTERN.matcher(request.getQueryString());
            if (matcher.matches()) {
                jwtToken = matcher.group(1);
            }
        }

        if(jwtToken == null || blackListService.isTokenInBlackList(jwtToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String email = jwtTokenService.validateTokenAndGetEmail(jwtToken);
        if(email != null && SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userDetails = userService.loadUserByUsername(email);
            if(userDetails == null){
                LOGGER.error("User not found, but token is valid");
                blackListService.addTokenToBlackList(jwtToken, jwtTokenService.getExpirationDate(jwtToken).getTime());
                filterChain.doFilter(request, response);
                return;
            }
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetails(request));
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            request.setAttribute("userId", ((User)userDetails).getId());
        }
        filterChain.doFilter(request, response);
    }
}
