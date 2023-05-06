package fr.utc.sr03.chat.controller;


import fr.utc.sr03.chat.model.User;
import fr.utc.sr03.chat.model.UserChatroomRelation;
import fr.utc.sr03.chat.service.implementations.ChatroomService;
import fr.utc.sr03.chat.service.implementations.UserChatroomRelationService;
import fr.utc.sr03.chat.service.implementations.UserService;
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
    @Resource
    private UserService userService;

    @Resource
    private ChatroomService chatroomService;

    @Resource
    private UserChatroomRelationService userChatroomRelationService;

    @GetMapping("/adminAccueil")
    public String getAdminAccueil(Model adminPage, @AuthenticationPrincipal User admin) {
        List<User> users = userService.findAllUsers();
        adminPage.addAttribute("admin",admin);
        adminPage.addAttribute("users",users);
        return "adminPage";
    }

    @GetMapping("/adminAjoutUser")
    public String getAddUserForm(Model model, @ModelAttribute(value = "msg")String msg , @AuthenticationPrincipal User admin) {
        model.addAttribute("admin",admin);
        model.addAttribute("user", new User());
        if(msg.equals("User already exists"))
            model.addAttribute("error",msg);
        else if(msg.equals("User added"))
            model.addAttribute("success",msg);

        return "adminAjoutUserPage";
    }

    @PostMapping("/adminAjoutUser")
    public String addUserToBD(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        boolean userAdded = userService.addUser(user);
        if (!userAdded){
            redirectAttributes.addFlashAttribute("msg", "User already exists");
        } else {
            redirectAttributes.addFlashAttribute("msg","User added");
        }
        return "redirect:/admin/adminAjoutUser";
    }

    @GetMapping("/adminSuppressionUser")
    public String getDeleteUserForm(Model model, @AuthenticationPrincipal User admin) {
        model.addAttribute("admin",admin);
        List<User> users = userService.findAllUsersNotAdmin();
        model.addAttribute("users",users);
        return "adminSuppressionUserPage";
    }

    @DeleteMapping("/adminSuppressionUser")
    public String deleteUser(@RequestParam("userId") long userId) {
        userService.deleteUserById(userId);
        List<UserChatroomRelation> relations = userChatroomRelationService.findRelationsOfUser(userId);
        for(UserChatroomRelation relation : relations){
            if(relation.isOwned()) {
                chatroomService.deleteChatRoom(relation.getChatroomId());
            }
            userChatroomRelationService.deleteRelation(relation);
        }
        return "redirect:/admin/adminSuppressionUser";
    }

    @GetMapping("/adminUserActivation")
    public String getActivationUserForm(Model model, @AuthenticationPrincipal User admin) {
        model.addAttribute("admin",admin);
        List<User> usersDesactive = userService.findAllInactiveUsers();
        model.addAttribute("users",usersDesactive);
        return "adminUserActivationPage";
    }

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
}

