package fr.utc.sr03.chat.controller;

import fr.utc.sr03.chat.model.User;
import fr.utc.sr03.chat.service.implementations.UserService;
import fr.utc.sr03.chat.service.utils.UserDTO;
import fr.utc.sr03.chat.service.utils.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
public class UserWebServiceController {

    private final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Resource
    private UserService userService;

    @GetMapping("/logged_user")
    public ResponseEntity<UserDTO> getLoggedUser(){
        logger.info("getLoggedUser");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            User user = (User) auth.getPrincipal(); // obtenir l'utilisateur connecté s'il existe
            return ResponseEntity.ok(UserMapper.toDTO(user));
        }
        return ResponseEntity.status(401).build();
    }


}
