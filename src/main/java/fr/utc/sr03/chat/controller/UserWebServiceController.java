package fr.utc.sr03.chat.controller;

import fr.utc.sr03.chat.model.Chatroom;
import fr.utc.sr03.chat.model.User;
import fr.utc.sr03.chat.service.implementations.ChatroomService;
import fr.utc.sr03.chat.service.implementations.UserChatroomRelationService;
import fr.utc.sr03.chat.service.implementations.UserService;
import fr.utc.sr03.chat.service.utils.ChatroomDTO;
import fr.utc.sr03.chat.service.utils.ChatroomRequestDTO;
import fr.utc.sr03.chat.service.utils.DTOMapper;
import fr.utc.sr03.chat.service.utils.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
public class UserWebServiceController {

    private final Logger logger = LoggerFactory.getLogger(UserWebServiceController.class);

    @Resource
    private UserService userService;

    @Resource
    private ChatroomService chatroomService;

    @Resource
    private UserChatroomRelationService userChatroomRelationService;

    @GetMapping("/logged_user")
    public ResponseEntity<UserDTO> getLoggedUser(){
        if (userService.checkUserLoginStatus()) {
            User user = userService.getLoggedUser();
            return ResponseEntity.ok(DTOMapper.toUserDTO(user));
        }
        return ResponseEntity.status(401).body(new UserDTO());
    }

    @GetMapping("/user/OtherUsers")
    public ResponseEntity<List<UserDTO>> getOtherUsers(){
        if(userService.checkUserLoginStatus()){
            List<User> users = userService.findAllUsersNotAdmin();
            User user = userService.getLoggedUser();
            users.remove(user);
            return ResponseEntity.ok(
                    users.stream().map(DTOMapper::toUserDTO).collect(Collectors.toList())
            );
        }
        return ResponseEntity.status(401).body(new ArrayList<>());
    }

    @PostMapping("/user/planifierChatroom")
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

    @GetMapping("/user/chatroomsOwned")
    public ResponseEntity<List<ChatroomDTO>> getChatroomsOwned(){
        if(userService.checkUserLoginStatus()){
            User user = userService.getLoggedUser();
            List<Chatroom> chatrooms = chatroomService.getChatroomsOwnedOrJoinedByUser(user.getId(),true);
            return ResponseEntity.ok(
                    chatrooms.stream().map(DTOMapper::toChatroomDTO).collect(Collectors.toList())
            );
        }
        return ResponseEntity.status(401).body(new ArrayList<>());
    }

    @GetMapping("/user/chatroomsJoined")
    public ResponseEntity<List<ChatroomDTO>> getChatroomsJoined(){
        if(userService.checkUserLoginStatus()){
            User user = userService.getLoggedUser();
            List<Chatroom> chatrooms = chatroomService.getChatroomsOwnedOrJoinedByUser(user.getId(),false);
            return ResponseEntity.ok(
                    chatrooms.stream().map(DTOMapper::toChatroomDTO).collect(Collectors.toList())
            );
        }
        return ResponseEntity.status(401).body(new ArrayList<>());
    }

    @GetMapping("/user/chatroom/{chatroomId}/usersInvited")
    public ResponseEntity<List<UserDTO>> getUsersInvited(@PathVariable long chatroomId){
        if(userService.checkUserLoginStatus()){
            List<User> users = chatroomService.getUsersInvitedToChatroom(chatroomId);
            return ResponseEntity.ok(
                    users.stream().map(DTOMapper::toUserDTO).collect(Collectors.toList())
            );
        }
        return ResponseEntity.status(401).body(new ArrayList<>());
    }

    @GetMapping("/user/chatroom/{chatroomId}/usersNotInvited")
    public ResponseEntity<List<UserDTO>> getUsersNotInvited(@PathVariable long chatroomId){
        if(userService.checkUserLoginStatus()){
            List<User> users = chatroomService.getUsersNotInvitedToChatroom(chatroomId);
            return ResponseEntity.ok(
                    users.stream().map(DTOMapper::toUserDTO).collect(Collectors.toList())
            );
        }
        return ResponseEntity.status(401).body(new ArrayList<>());
    }

    @GetMapping("/user/chatroom/{chatroomId}/owner")
    public ResponseEntity<UserDTO> getOwner(@PathVariable long chatroomId){
        if(userService.checkUserLoginStatus()){
            Optional<User> user = userService.findUserById(
                    userChatroomRelationService.findOwnerOfChatroom(chatroomId).isPresent()?
                            userChatroomRelationService.findOwnerOfChatroom(chatroomId).get().getUserId() : 0L
            );
            return user.map(value -> ResponseEntity.ok(DTOMapper.toUserDTO(value)))
                                .orElseGet(() -> ResponseEntity.status(404).body(new UserDTO()));
        }
        return ResponseEntity.status(401).body(new UserDTO());
    }

    @DeleteMapping("/user/chatroom/{chatroomId}")
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

    @PostMapping("/user/chatroom/{chatroomId}/addUserInvited")
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

    @DeleteMapping("/user/chatroom/{chatroomId}/userInvited/{userId}")
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

    @GetMapping("/user/chatroom/{chatroomId}")
    public ResponseEntity<ChatroomDTO> getChatroom(@PathVariable long chatroomId){
        if(userService.checkUserLoginStatus()){
            Optional<Chatroom> chatroom = chatroomService.findChatroom(chatroomId);
            return chatroom.map(value -> ResponseEntity.ok(DTOMapper.toChatroomDTO(value)))
                    .orElseGet(() -> ResponseEntity.status(404).body(new ChatroomDTO()));
        }
        return ResponseEntity.status(401).body(new ChatroomDTO());
    }

    @PutMapping("/user/chatroom/{chatroomId}")
    public ResponseEntity<Boolean> updateChatroom(@PathVariable long chatroomId,@RequestBody ChatroomRequestDTO chatroomRequestDTO){
        if(userService.checkUserLoginStatus()){
            boolean res = chatroomService.updateChatroom(chatroomRequestDTO, chatroomId);
            if(res)
                return ResponseEntity.ok(true);
            else
                return ResponseEntity.status(409).body(false);
        }
        return ResponseEntity.status(401).body(false);
    }
}
