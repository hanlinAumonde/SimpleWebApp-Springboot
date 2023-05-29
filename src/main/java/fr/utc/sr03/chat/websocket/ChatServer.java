package fr.utc.sr03.chat.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.utc.sr03.chat.model.User;
import fr.utc.sr03.chat.service.implementations.UserService;
import fr.utc.sr03.chat.service.utils.SpringContext;
import fr.utc.sr03.chat.service.utils.UserDTO;
import fr.utc.sr03.chat.service.utils.DTOMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/chatroom/{chatroomId}/user/{userId}")
@Component
public class ChatServer {
    private final Logger logger = LoggerFactory.getLogger(ChatServer.class);

    //map<chatroomID,ConcurrentHashMap<user, userSession>>
    private static final Map<Long, ConcurrentHashMap<Session, UserDTO>> chatrooms = new ConcurrentHashMap<>();

    private static final ObjectMapper mapper = new ObjectMapper();

    private UserDTO userInfo;

    private final UserService userService = SpringContext.getBean(UserService.class);

    @OnOpen
    public void ouvertureConnection(Session session, @PathParam("chatroomId") long chatroomId, @PathParam("userId") long userId) {
        try{
            Optional<User> user = userService.findUserById(userId);
            if(user.isPresent()){
                this.userInfo = DTOMapper.toUserDTO(user.get());
            }else{
                throw new Exception("User not found");
            }
            if(chatrooms.containsKey(chatroomId)){
                chatrooms.get(chatroomId).put(session, userInfo);
            }else{
                ConcurrentHashMap<Session, UserDTO> newChatroom = new ConcurrentHashMap<>();
                newChatroom.put(session, userInfo);
                chatrooms.put(chatroomId, newChatroom);
            }
            broadcastMessage(
                    setMessage(1,"",this.userInfo)
                    ,chatroomId
            );
            for (Map.Entry<Session, UserDTO> entry : chatrooms.get(chatroomId).entrySet()) {
                if(!entry.getKey().equals(session)){
                    sendMsgToMe(
                            setMessage(1,"",entry.getValue())
                            ,chatroomId
                    );
                }
            }
            logger.info("Connection opened for user {} in chatroom {}", userInfo.getLastName()+" "+userInfo.getFirstName(), chatroomId);

        }catch (Exception e){
            logger.error("Error while opening connection : ", e);
            try {
                session.close();
            } catch (IOException ioException) {
                logger.error("Error while closing session : ", ioException);
            }
        }
    }

    @OnClose
    public void fermetureConnection(Session session, @PathParam("chatroomId") long chatroomId){
        try{
            broadcastMessageExceptSender(
                    setMessage(2,"",this.userInfo)
                    ,chatroomId
            );
            if(chatrooms.containsKey(chatroomId)){
                chatrooms.get(chatroomId).remove(session);
                logger.info("Connection closed for user {} in chatroom {}", userInfo.getLastName()+" "+userInfo.getFirstName(), chatroomId);
                if(chatrooms.get(chatroomId).size() == 0){
                    chatrooms.remove(chatroomId);
                    logger.info("Chatroom {} is now empty and has been removed", chatroomId);
                }
            }
        }catch (Exception e){
            logger.error("Error while closing connection : ", e);
        }
    }

    @OnMessage
    public void receptionMessage(String message, @PathParam("chatroomId") long chatroomId){
        try{
            broadcastMessage(
                    setMessage(0,message,this.userInfo)
                    ,chatroomId
            );
        }catch (Exception e){
            logger.error("Error while receiving message : ", e);
        }
    }

    @OnError
    public void onError(Throwable error) {
        logger.error("Error occurred");
        error.printStackTrace();
    }

    private String setMessage(int messageType, String message, UserDTO userInfo) {
        //format : {user: {id: 1, username: "user1 user1"}, messageType: 0, message: "hello" , timestamp : "18:00"}
        try {
            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

            ObjectNode finalNode = mapper.createObjectNode();

            ObjectNode userNode = mapper.createObjectNode();
            userNode.put("id", userInfo.getId());
            userNode.put("username", userInfo.getLastName() + " " + userInfo.getFirstName());

            finalNode.set("user", userNode);
            finalNode.put("messageType", messageType);
            finalNode.put("message", message);
            finalNode.put("timestamp", sdf.format(now));

            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(finalNode);
        } catch (JsonProcessingException e) {
            logger.error("Error while creating message : ", e);
            return "";
        }
    }

    private void broadcastMessage(String message, long chatroomId){
        if(chatrooms.containsKey(chatroomId)){
            chatrooms.get(chatroomId).forEach((session, user) -> {
                //session.getBasicRemote().sendText(message);
                if(!message.equals(""))
                    session.getAsyncRemote().sendText(message);
            });
        }
    }

    private void broadcastMessageExceptSender(String message, long chatroomId){
        if(chatrooms.containsKey(chatroomId)){
            chatrooms.get(chatroomId).forEach((session, user) -> {
                if(!(user.getId() == this.userInfo.getId()) && !message.equals(""))
                    session.getAsyncRemote().sendText(message);
            });
        }
    }

    private void sendMsgToMe(String message, long chatroomId){
        if(chatrooms.containsKey(chatroomId)){
            chatrooms.get(chatroomId).forEach((session, user) -> {
                if(user.getId() == this.userInfo.getId() && !message.equals(""))
                    session.getAsyncRemote().sendText(message);
            });
        }
    }

}
