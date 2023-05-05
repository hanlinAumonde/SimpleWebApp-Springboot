package fr.utc.sr03.chat.controller;

import fr.utc.sr03.chat.dao.ChatRoomRepository;
import fr.utc.sr03.chat.dao.UserChatroomRelationRepository;
import fr.utc.sr03.chat.dao.UserRepository;
import fr.utc.sr03.chat.model.ChatRoom;
import fr.utc.sr03.chat.model.User;
import fr.utc.sr03.chat.model.UserChatroomRelation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping
public class LoginController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private UserChatroomRelationRepository userChatroomRelationRepository;

    @GetMapping("/login")
    public String getLogin(Model model) {
        model.addAttribute("user", new User());
        return "loginPage";
    }

    @GetMapping("/accueil")
    public String getAcceuil(Model Page, @AuthenticationPrincipal User user){
        if(user.isAdmin()){
            System.out.println("login du admin");
            System.out.println("user's authorities : " + user.getAuthorities());
            return "redirect:/admin/adminAccueil";
        }else{
            System.out.println("login du user");
            long userId = user.getId();
            List<ChatRoom> ChatroomsOwned = new ArrayList<>();
            List<ChatRoom> ChatroomsInvited = new ArrayList<>();

            List<UserChatroomRelation> ChatroomsOwnedByUser = userChatroomRelationRepository.findByUserIdAndOwned(userId,true);
            for(UserChatroomRelation owned : ChatroomsOwnedByUser){
                ChatRoom ChatroomTemp = chatRoomRepository.findById(owned.getChatRoomId()).get();
                Date currentDate = new Date();
                if(ChatroomTemp.getHoraireTermine().getTime() > currentDate.getTime()){
                    ChatroomsOwned.add(ChatroomTemp);
                }
            }
            List<UserChatroomRelation> ChatroomsInviteUser = userChatroomRelationRepository.findByUserIdAndOwned(userId,false);
            for(UserChatroomRelation invited : ChatroomsInviteUser){
                ChatRoom ChatroomTemp = chatRoomRepository.findById(invited.getChatRoomId()).get();
                Date currentDate = new Date();
                if(ChatroomTemp.getHoraireTermine().getTime() > currentDate.getTime()){
                    ChatroomsInvited.add(ChatroomTemp);
                }
            }

            Page.addAttribute("users",userRepository.findAll());
            Page.addAttribute("ChatroomsOwnedByUser",ChatroomsOwned);
            Page.addAttribute("ChatroomsInviteUser",ChatroomsInvited);
            return "userPage";
        }
    }

}
