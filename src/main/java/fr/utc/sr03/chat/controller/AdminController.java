package fr.utc.sr03.chat.controller;


import fr.utc.sr03.chat.model.ResetPasswordValidate;
import fr.utc.sr03.chat.model.User;
import fr.utc.sr03.chat.model.UserChatroomRelation;
import fr.utc.sr03.chat.service.implementations.ChatroomService;
import fr.utc.sr03.chat.service.implementations.ResetPasswordValidateService;
import fr.utc.sr03.chat.service.implementations.UserChatroomRelationService;
import fr.utc.sr03.chat.service.implementations.UserService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import java.util.List;

@Controller
@RequestMapping(value="/admin")
public class AdminController {

    //le nombre de chatrooms par page défault
    private static final int defaultPageSize = 5;
    @Resource
    private UserService userService;

    @Resource
    private ChatroomService chatroomService;

    @Resource
    private UserChatroomRelationService userChatroomRelationService;

    @Resource
    private ResetPasswordValidateService resetPasswordValidateService;

    //Tous les anootations @AuthenticationPrincipal sont pour récupérer les informations de l'utilisateur connecté
    //Tous les opération directement sur la base de données sont dans les services correspondants,autrement dit on peut pas accéder à la couche DAO directement dans les controllers

    /**
     * C'est la page d'accueil de l'admin, elle affiche tous les utilisateurs de la base de données
     */
    @GetMapping("/adminAccueil")
    public String getAdminAccueil(Model adminPage, @RequestParam(defaultValue = "0") int page ,@AuthenticationPrincipal User admin) {
        //List<User> users = userService.findAllUsers();
        Page<User> users = userService.findAllUsersByPage(page,defaultPageSize);
        adminPage.addAttribute("admin",admin);
        adminPage.addAttribute("users",users);
        return "adminPage";
    }

    /**
     * Cette méthode permet d'obtenir le formulaire d'ajout d'un utilisateur
     */
    @GetMapping("/adminAjoutUser")
    public String getAddUserForm(Model model, @ModelAttribute(value = "msg")String msg , @AuthenticationPrincipal User admin) {
        model.addAttribute("admin",admin);
        model.addAttribute("user", new User());
        if(msg.equals("Compte déjà existant"))
            model.addAttribute("error",msg);
        else if(msg.equals("Compte crée !"))
            model.addAttribute("success",msg);

        return "adminAjoutUserPage";
    }

    /**
     * Cette méthode permet d'ajouter un utilisateur à la base de données
     */
    @PostMapping("/adminAjoutUser")
    public String addUserToBD(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        boolean userAdded = userService.addUser(user);
        if (!userAdded){
            redirectAttributes.addFlashAttribute("msg", "Compte déjà existant");
        } else {
            redirectAttributes.addFlashAttribute("msg","Compte crée !");
        }
        return "redirect:/admin/adminAjoutUser";
    }

    /**
     * Cette méthode permet d'oobtenir le formulaire de suppression d'un utilisateur
     */
    @GetMapping("/adminSuppressionUser")
    public String getDeleteUserForm(Model model,@RequestParam(defaultValue = "0")int page ,@AuthenticationPrincipal User admin) {
        model.addAttribute("admin",admin);
        //List<User> users = userService.findAllUsersNotAdmin();
        Page<User> users = userService.findAllUsersNotAdminByPage(page,defaultPageSize);
        model.addAttribute("users",users);
        return "adminSuppressionUserPage";
    }

    /**
     * Cette méthode permet de supprimer un utilisateur de la base de données
     * Elle supprime aussi tous ses relations et ses salons
     */
    @DeleteMapping("/adminSuppressionUser")
    public String deleteUser(@RequestParam("userId") long userId) {
        List<UserChatroomRelation> relations = userChatroomRelationService.findRelationsOfUser(userId);
        for(UserChatroomRelation relation : relations){
            userChatroomRelationService.deleteRelation(relation);
            if(relation.isOwned()) {
                chatroomService.deleteChatRoom(relation.getChatroomId());
            }
        }
        userService.deleteUserById(userId);
        return "redirect:/admin/adminSuppressionUser";
    }

    /**
     * Cette méthode permet d'obtenir le liste des utilisateurs désactivés
     */
    @GetMapping("/adminUserActivation")
    public String getActivationUserForm(Model model,@RequestParam(defaultValue = "0")int page ,@AuthenticationPrincipal User admin) {
        model.addAttribute("admin",admin);
        //List<User> usersDesactive = userService.findAllInactiveUsers();
        Page<User> usersDesactive = userService.findAllInactiveUsersByPage(page,defaultPageSize);
        model.addAttribute("users",usersDesactive);
        return "adminUserActivationPage";
    }

    /**
     * Cette méthode permet d'activer un utilisateur
     * Elle active aussi tous ses salons
     */
    @PutMapping("/adminUserActivation")
    public String activateUser(@RequestParam("userId") long userId) {
        userService.setStatusOfUser(userId,true);
        /*
         * activer les salons de l'utilisateur
         */
        List<UserChatroomRelation> relations = userChatroomRelationService.findRelationsOfUser(userId);
        for(UserChatroomRelation relation : relations){
            chatroomService.setStatusOfChatroom(relation.getChatroomId(),true);
        }

        return "redirect:/admin/adminUserActivation";
    }

    @PutMapping("/adminSuppressionUser")
    public String blockUser(@RequestParam("userId") long userId) {
        userService.setStatusOfUser(userId,false);
        /*
         * activer les salons de l'utilisateur
         */
        List<UserChatroomRelation> relations = userChatroomRelationService.findRelationsOfUser(userId);
        for(UserChatroomRelation relation : relations){
            chatroomService.setStatusOfChatroom(relation.getChatroomId(),false);
        }

        return "redirect:/admin/adminSuppressionUser";
    }
}

