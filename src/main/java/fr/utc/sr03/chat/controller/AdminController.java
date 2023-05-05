package fr.utc.sr03.chat.controller;

import fr.utc.sr03.chat.dao.ChatRoomRepository;
import fr.utc.sr03.chat.dao.UserChatroomRelationRepository;
import fr.utc.sr03.chat.dao.UserRepository;
import fr.utc.sr03.chat.model.User;
import fr.utc.sr03.chat.model.UserChatroomRelation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping
public class AdminController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private UserChatroomRelationRepository userChatroomRelationRepository;

    @GetMapping("/admin/adminAccueil")
    public String getAdminAccueil(Model adminPage, @AuthenticationPrincipal User admin) {
        List<User> users = userRepository.findAll();
        adminPage.addAttribute("admin",admin);
        adminPage.addAttribute("users",users);
        return "adminPage";
    }

    @GetMapping("/admin/adminAjoutUser")
    public String getAddUserForm(Model model, @ModelAttribute(value = "msg")String msg , @AuthenticationPrincipal User admin) {
        model.addAttribute("admin",admin);
        model.addAttribute("user", new User());
        if(msg.equals("User already exists"))
            model.addAttribute("error",msg);
        else if(msg.equals("User added"))
            model.addAttribute("success",msg);

        return "adminAjoutUserPage";
    }

    @PostMapping("/admin/adminAjoutUser")
    public String addUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {

        List<User> allUsers = userRepository.findAll();
        for(User u : allUsers){
            if(u.equals(user)){
                redirectAttributes.addFlashAttribute("msg", "User already exists");
                return "redirect:/admin/adminAjoutUser";
            }
        }

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("msg", "User added");
        return "redirect:/admin/adminAjoutUser";
    }

    @GetMapping("/admin/adminSuppressionUser")
    public String getDeleteUserForm(Model model, @AuthenticationPrincipal User admin) {
        model.addAttribute("admin",admin);
        List<User> users = userRepository.findByAdmin(false);
        model.addAttribute("users",users);
        return "adminSuppressionUserPage";
    }

    @DeleteMapping("/admin/adminSuppressionUser")
    public String deleteUser(@RequestParam("userId") long userId) {
        User user = userRepository.findById(userId).get();
        userRepository.delete(user);

        List<UserChatroomRelation> relations = userChatroomRelationRepository.findByUserId(userId);
        for(UserChatroomRelation relation : relations){
            if(relation.isOwned()) {
                chatRoomRepository.findById(relation.getChatRoomId()).ifPresent(chatRoom -> chatRoomRepository.delete(chatRoom));
            }
            userChatroomRelationRepository.delete(relation);
        }

        return "redirect:/admin/adminSuppressionUser";
    }

    @GetMapping("/admin/adminUserActivation")
    public String getActivationUserForm(Model model, @AuthenticationPrincipal User admin) {
        model.addAttribute("admin",admin);
        List<User> usersDesactive = userRepository.findByActive(false);
        model.addAttribute("users",usersDesactive);
        return "adminUserActivationPage";
    }

    @PutMapping("/admin/adminUserActivation")
    public String activateUser(@RequestParam("userId") long userId) {
        User user = userRepository.findById(userId).get();
        user.setActive(true);
        userRepository.save(user);
        /*
         * activer les salons de l'utilisateur
         *
        List<UserChatroomRelation> relations = userChatroomRelationRepository.findByUserId(userId);
        for(UserChatroomRelation relation : relations){
            chatRoomRepository.findById(relation.getChatRoomId()).ifPresent(chatRoom -> {
                chatRoom.setActive(true);
                chatRoomRepository.save(chatRoom);
            });
        }
         */
        return "redirect:/admin/adminUserActivation";
    }
}

