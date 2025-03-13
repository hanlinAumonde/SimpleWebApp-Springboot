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

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenService jwtTokenService;
    private final UserService userService;
    private final BlackListService blackListService;

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
        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            final String jwtToken = authHeader.substring(7);
            if(blackListService.isTokenInBlackList(jwtToken)) {
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
        }
        filterChain.doFilter(request, response);
    }
}
