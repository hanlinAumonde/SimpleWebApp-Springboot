package fr.utc.sr03.chat.controller;

import fr.utc.sr03.chat.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping
public class LoginController {
    private final Logger logger = LoggerFactory.getLogger(LoginController.class);

    private static final String remoteFrontEndUrl = "http://localhost:3000";

    /**
     * Cette méthode permet d'obtenir le formulaire de login
     */
    @GetMapping("/login")
    public String getLogin(Model model, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            User user = (User) auth.getPrincipal(); // obtenir l'utilisateur connecté s'il existe
            if (user.isAdmin()) {
                return "redirect:/admin/adminAccueil"; // si le user est un admin, rediriger vers la page d'accueil de l'admin
            } else {
                return "redirect:" + remoteFrontEndUrl; // si le user est un user, rediriger vers la page d'accueil du user
            }
        }
        //Obtenir le message d'erreur
        model.addAttribute("user", new User());
        HttpSession session = request.getSession();
        String errorMsg = (String) session.getAttribute("error");

        if(errorMsg != null) {
            model.addAttribute("error", errorMsg);
            session.removeAttribute("error");
        }
        return "loginPage";
    }

    /**
     * Cette méthode permet d'odentifier si l'utilisateur est un admin ou un user,
     * et puis rediriger vers la page d'accueil correspondante
     */
    @GetMapping("/accueil")
    public String getAcceuil(@AuthenticationPrincipal User user){
        if(user.isAdmin()){
            logger.info("login du admin");
            logger.info("user's authorities : " + user.getAuthorities());
            return "redirect:/admin/adminAccueil";
        }else{
            logger.info("login du user");
            logger.info("user's authorities : " + user.getAuthorities());
            return "redirect:" + remoteFrontEndUrl;
        }
    }

}
