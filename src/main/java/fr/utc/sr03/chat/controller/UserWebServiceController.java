package fr.utc.sr03.chat.controller;

import fr.utc.sr03.chat.model.Chatroom;
import fr.utc.sr03.chat.model.User;
import fr.utc.sr03.chat.model.UserChatroomRelation;
import fr.utc.sr03.chat.service.implementations.ChatroomService;
import fr.utc.sr03.chat.service.implementations.UserChatroomRelationService;
import fr.utc.sr03.chat.service.implementations.UserService;
import fr.utc.sr03.chat.service.utils.ChatroomDTO;
import fr.utc.sr03.chat.service.utils.ChatroomRequestDTO;
import fr.utc.sr03.chat.service.utils.DTOMapper;
import fr.utc.sr03.chat.service.utils.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.websocket.SessionException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
public class UserWebServiceController {

    private final Logger logger = LoggerFactory.getLogger(UserWebServiceController.class);

    private static final int defaultPageSize = 5;

    @Resource
    private UserService userService;

    @Resource
    private ChatroomService chatroomService;

    @Resource
    private UserChatroomRelationService userChatroomRelationService;

    @GetMapping("/users/logged")
    public ResponseEntity<UserDTO> getLoggedUser(HttpServletRequest request){
        if (userService.checkUserLoginStatus()) {
            User user = userService.getLoggedUser();
            return ResponseEntity.ok(DTOMapper.toUserDTO(user));
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(!(auth instanceof AnonymousAuthenticationToken)){
            //si l'utilisateur est connecté mais a ete supprimé par l'admin, on doit mauellement supprimer le contexte de login
            //delete login context
            SecurityContextHolder.getContext().setAuthentication(null);
            //invalidate http session
            request.getSession().invalidate();
            logger.info("user {} a ete supprime par l'admin", auth.getName());
        }
        return ResponseEntity.status(401).body(new UserDTO());
    }

    @GetMapping("/user/users/other")
    public ResponseEntity<Page<UserDTO>> getOtherUsers(@RequestParam(defaultValue ="0")int page){
        if(userService.checkUserLoginStatus()){
            Page<User> users = userService.findAllOtherUsersNotAdminByPage(page, defaultPageSize, userService.getLoggedUser().getId());
            return ResponseEntity.ok(users.map(DTOMapper::toUserDTO));
        }
        return ResponseEntity.status(401).body(Page.empty());
    }

    @PostMapping("/user/chatrooms/")
    public ResponseEntity<ChatroomDTO> createChatroom(@RequestBody ChatroomRequestDTO chatroomRequestDTO){
        if(userService.checkUserLoginStatus()){
            User user = userService.getLoggedUser();
            Chatroom result = chatroomService.createChatroom(chatroomRequestDTO, user.getId());
            if(result.getId() != 0L){
                return ResponseEntity.ok(DTOMapper.toChatroomDTO(result));
            }else{
                return ResponseEntity.status(409).body(DTOMapper.toChatroomDTO(result));
            }
        }
        return ResponseEntity.status(401).body(new ChatroomDTO());
    }

    @GetMapping("/user/users/{userId}/chatrooms/owned")
    public ResponseEntity<Page<ChatroomDTO>> getChatroomsOwnedOfPage(@PathVariable long userId, @RequestParam(defaultValue = "0")int page){
        if(userService.checkUserLoginStatus() && userId == userService.getLoggedUser().getId()){
            Page<Chatroom> chatrooms = chatroomService.getChatroomsOwnedOrJoinedOfUserByPage(userId,true,page,defaultPageSize);
            return ResponseEntity.ok(chatrooms.map(DTOMapper::toChatroomDTO));
        }
        return ResponseEntity.status(401).body(Page.empty());
    }

    @GetMapping("/user/users/{userId}/chatrooms/joined")
    public ResponseEntity<Page<ChatroomDTO>> getChatroomsJoinedOfPage(@PathVariable long userId, @RequestParam(defaultValue = "0")int page){
        if(userService.checkUserLoginStatus() && userId == userService.getLoggedUser().getId()){
            Page<Chatroom> chatrooms = chatroomService.getChatroomsOwnedOrJoinedOfUserByPage(userId,false,page,defaultPageSize);
            return ResponseEntity.ok(chatrooms.map(DTOMapper::toChatroomDTO));
        }
        return ResponseEntity.status(401).body(Page.empty());
    }

    @GetMapping("/user/chatrooms/{chatroomId}/users/invited")
    public ResponseEntity<Page<UserDTO>> getUsersInvitedOfPage(@PathVariable long chatroomId, @RequestParam(defaultValue="0")int page){
        boolean checkOwner = chatroomService.checkUserIsOwnerOfChatroom(userService.getLoggedUser().getId(),chatroomId);
        if(userService.checkUserLoginStatus() && checkOwner){
            Page<User> users = userService.findUsersInvitedToChatroomByPage(chatroomId,page,defaultPageSize);
            return ResponseEntity.ok(users.map(DTOMapper::toUserDTO));
        }else if(!checkOwner){
            return ResponseEntity.status(403).body(Page.empty());
        }
        return ResponseEntity.status(401).body(Page.empty());
    }

    @GetMapping("/user/chatrooms/{chatroomId}/users/non-invited")
    public ResponseEntity<Page<UserDTO>> getUsersNotInvitedOfPage(@PathVariable long chatroomId, @RequestParam(defaultValue="0")int page){
        boolean checkOwner = chatroomService.checkUserIsOwnerOfChatroom(userService.getLoggedUser().getId(),chatroomId);
        if(userService.checkUserLoginStatus() && checkOwner){
            Page<User> users = userService.findUsersNotInvitedToChatroomByPage(chatroomId,page,defaultPageSize);
            return ResponseEntity.ok(users.map(DTOMapper::toUserDTO));
        }else if(!checkOwner){
            return ResponseEntity.status(403).body(Page.empty());
        }
        return ResponseEntity.status(401).body(Page.empty());
    }

    @GetMapping("/user/chatrooms/{chatroomId}/users/owner")
    public ResponseEntity<UserDTO> getOwner(@PathVariable long chatroomId){
        if(userService.checkUserLoginStatus()){
            Optional<UserChatroomRelation> relation = userChatroomRelationService.findOwnerOfChatroom(chatroomId);
            Optional<User> user = userService.findUserById(
                    relation.map(UserChatroomRelation::getUserId).orElse(0L)
            );
            return user.map(value -> ResponseEntity.ok(DTOMapper.toUserDTO(value)))
                                .orElseGet(() -> ResponseEntity.status(404).body(new UserDTO()));
        }
        return ResponseEntity.status(401).body(new UserDTO());
    }

    @DeleteMapping("/user/chatrooms/{chatroomId}")
    public ResponseEntity<Boolean> deleteChatroom(@PathVariable long chatroomId){
        if(userService.checkUserLoginStatus()){
            User user = userService.getLoggedUser();
            if(chatroomService.deleteChatRoom(chatroomId)){
                return ResponseEntity.ok(true);
            }else{
                return ResponseEntity.status(409).body(false);
            }
        }
        return ResponseEntity.status(401).body(false);
    }

    @PostMapping("/user/chatrooms/{chatroomId}/users/invited/")
    public ResponseEntity<Boolean> addUserInvited(@PathVariable long chatroomId, @RequestBody UserDTO userDTO){
        if(userService.checkUserLoginStatus()){
            try{
                userChatroomRelationService.addRelation(userDTO.getId() ,chatroomId,false);
                return ResponseEntity.ok(true);
            }catch (Exception e){
                return ResponseEntity.status(409).body(false);
            }
        }
        return ResponseEntity.status(401).body(false);
    }

    @DeleteMapping("/user/chatrooms/{chatroomId}/users/invited/{userId}")
    public ResponseEntity<Boolean> deleteUserInvited(@PathVariable long chatroomId, @PathVariable long userId){
        if(userService.checkUserLoginStatus()){
            if(chatroomService.deleteUserInvited(chatroomId, userId)){
                return ResponseEntity.ok(true);
            }else{
                return ResponseEntity.status(409).body(false);
            }
        }
        return ResponseEntity.status(401).body(false);
    }

    @GetMapping("/user/chatrooms/{chatroomId}")
    public ResponseEntity<ChatroomDTO> getChatroom(@PathVariable long chatroomId){
        if(userService.checkUserLoginStatus()){
            Optional<Chatroom> chatroom = chatroomService.findChatroom(chatroomId);
            return chatroom.map(value -> ResponseEntity.ok(DTOMapper.toChatroomDTO(value)))
                    .orElseGet(() -> ResponseEntity.status(404).body(new ChatroomDTO()));
        }
        return ResponseEntity.status(401).body(new ChatroomDTO());
    }

    @PutMapping("/user/chatrooms/{chatroomId}")
    public ResponseEntity<Boolean> updateChatroom(@PathVariable long chatroomId,@RequestBody ChatroomRequestDTO chatroomRequestDTO){
        boolean checkOwner = chatroomService.checkUserIsOwnerOfChatroom(chatroomId, userService.getLoggedUser().getId());
        if(userService.checkUserLoginStatus() && checkOwner){
            boolean res = chatroomService.updateChatroom(chatroomRequestDTO, chatroomId);
            if(res)
                return ResponseEntity.ok(true);
            else
                return ResponseEntity.status(409).body(false);
        }else if(!checkOwner){
            return ResponseEntity.status(403).body(false);
        }
        return ResponseEntity.status(401).body(false);
    }

    @GetMapping("/user/chatrooms/{chatroomId}/status")
    public ResponseEntity<Boolean> getChatroomStatus(@PathVariable long chatroomId){
        if(userService.checkUserLoginStatus()){
            Optional<Chatroom> chatroom = chatroomService.findChatroom(chatroomId);
            return chatroom.map(value -> ResponseEntity.ok(value.isActive()))
                    .orElseGet(() -> ResponseEntity.status(404).body(false));
        }
        return ResponseEntity.status(401).body(false);
    }

    @GetMapping("/user/chatrooms/{chatroomId}/users")
    public ResponseEntity<List<UserDTO>> getAllUsersInChatroom(@PathVariable long chatroomId){
        if(userService.checkUserLoginStatus()){
            List<User> users = chatroomService.getAllUsersInChatroom(chatroomId);
            if(users.size() > 1) {
                return ResponseEntity.ok(
                        users.stream().map(DTOMapper::toUserDTO).collect(Collectors.toList())
                );
            }else {
                return ResponseEntity.status(500).body(new ArrayList<>());
            }
        }
        return ResponseEntity.status(401).body(new ArrayList<>());
    }
}
