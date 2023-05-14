package fr.utc.sr03.chat.controller;

import fr.utc.sr03.chat.model.Chatroom;
import fr.utc.sr03.chat.model.User;
import fr.utc.sr03.chat.model.UserChatroomRelation;
import fr.utc.sr03.chat.service.implementations.ChatroomService;
import fr.utc.sr03.chat.service.implementations.UserChatroomRelationService;
import fr.utc.sr03.chat.service.implementations.UserService;
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
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping
public class LoginController {
    private final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Resource
    private UserService userService;

    @Resource
    private ChatroomService chatroomService;

    @Resource
    private UserChatroomRelationService userChatroomRelationService;

    /**
     * Cette méthode permet d'obtenir le formulaire de login
     */
    @GetMapping("/login")
    public String getLogin(Model model, @RequestParam(value = "error", required = false) String error, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            User user = (User) auth.getPrincipal(); // obtenir l'utilisateur connecté s'il existe
            if (user.isAdmin()) {
                return "redirect:/admin/adminAccueil"; // si le user est un admin, rediriger vers la page d'accueil de l'admin
            } else {
                return "userPage"; // si le user est un user, rediriger vers la page d'accueil du user
            }
        }

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
    public String getAcceuil(Model Page, @AuthenticationPrincipal User user){
        if(user.isAdmin()){
            logger.info("login du admin");
            logger.info("user's authorities : " + user.getAuthorities());
            return "redirect:/admin/adminAccueil";
        }else{
            logger.info("login du user");
            logger.info("user's authorities : " + user.getAuthorities());
            /*
            long userId = user.getId();
            List<Chatroom> ChatroomsOwned = new ArrayList<>();
            List<Chatroom> ChatroomsInvited = new ArrayList<>();

            List<UserChatroomRelation> ChatroomsOwnedByUser = userChatroomRelationService.findChatroomsOwnedOrInviting(userId,true);
            for(UserChatroomRelation owned : ChatroomsOwnedByUser){
                Chatroom ChatroomTemp = chatroomService.findChatroom(owned.getChatroomId()).get();
                Date currentDate = new Date();
                if(ChatroomTemp.getHoraireTermine().getTime() > currentDate.getTime()){
                    ChatroomsOwned.add(ChatroomTemp);
                }
            }
            List<UserChatroomRelation> ChatroomsInviteUser = userChatroomRelationService.findChatroomsOwnedOrInviting(userId,false);
            for(UserChatroomRelation invited : ChatroomsInviteUser){
                Chatroom ChatroomTemp = chatroomService.findChatroom(invited.getChatroomId()).get();
                Date currentDate = new Date();
                if(ChatroomTemp.getHoraireTermine().getTime() > currentDate.getTime()){
                    ChatroomsInvited.add(ChatroomTemp);
                }
            }

            Page.addAttribute("users",userService.findAllUsers());
            Page.addAttribute("ChatroomsOwnedByUser",ChatroomsOwned);
            Page.addAttribute("ChatroomsInviteUser",ChatroomsInvited);
            */
            return "userPage";
        }
    }

}
