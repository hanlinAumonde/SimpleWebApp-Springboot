package fr.utc.sr03.chat.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.utc.sr03.chat.dto.DTOMapper;
import fr.utc.sr03.chat.dto.UserDTO;
import fr.utc.sr03.chat.model.User;
import fr.utc.sr03.chat.service.implementations.ChatMessageService;
import fr.utc.sr03.chat.service.implementations.UserService;
import fr.utc.sr03.chat.service.utils.SpringContext;
import fr.utc.sr03.chat.service.utils.Events.ChangeChatroomMemberEvent;
import fr.utc.sr03.chat.service.utils.Events.RemoveChatroomEvent;
import static fr.utc.sr03.chat.service.utils.ConstantValues.MESSAGE_TEXT;
import static fr.utc.sr03.chat.service.utils.ConstantValues.MESSAGE_CONNECT;
import static fr.utc.sr03.chat.service.utils.ConstantValues.MESSAGE_DISCONNECT;
import static fr.utc.sr03.chat.service.utils.ConstantValues.MESSAGE_REMOVE_CHATROOM;
import static fr.utc.sr03.chat.service.utils.ConstantValues.MESSAGE_ADD_CHATROOM_MEMBER;
import static fr.utc.sr03.chat.service.utils.ConstantValues.MESSAGE_REMOVE_CHATROOM_MEMBER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cette classe permet de gérer les connexions websocket
 */
@ServerEndpoint(value = "/chatroom/{chatroomId}/user/{userId}") //le lien contient l'id de la chatroom et l'id de l'utilisateur
@Component
public class ChatServer {
    private final Logger logger = LoggerFactory.getLogger(ChatServer.class);

    //on gère les différentes chatrooms en utilisant une map contenant les entrées qui représentent les chatrooms
    //et chaque chatroom contient une map contenant les entrées qui représentent les utilisateurs connectés (session de user - userDTO)
    //map<chatroomID,ConcurrentHashMap<user, userSession>>
    private static final Map<Long, ConcurrentHashMap<Session, UserDTO>> chatrooms = new ConcurrentHashMap<>();

    //on utilise ObjectMapper pour construire les messages en JSON
    private static final ObjectMapper mapper = new ObjectMapper();

    //userInfo contient les informations de l'utilisateur qui ouvre la connexion WebSocket correspondante
    private UserDTO userInfo;

    //C'est pour obtenir le service UserService pour gérer les données dans la base de données
    private final UserService userService = SpringContext.getBean(UserService.class);
    
    //C'est pour obtenir le service ChatMessageService pour gérer les messages dans la base de données
    private final ChatMessageService chatMessageService = SpringContext.getBean(ChatMessageService.class);

    /**
     * Cette méthode permet de traiter l'ouverture d'une connexion websocket
     */
    @OnOpen
    public void ouvertureConnection(Session session, @PathParam("chatroomId") long chatroomId, @PathParam("userId") long userId) {
        try{
            Optional<User> user = userService.findUserById(userId);
            if(user.isPresent()){
                this.userInfo = DTOMapper.toUserDTO(user.get());
            }else{
                throw new Exception("User not found");
            }
            //on ajoute la session de l'utilisateur dans la chatroom correspondante, si la chatroom n'existe pas, on la crée
            if(chatrooms.containsKey(chatroomId)){
                chatrooms.get(chatroomId).put(session, userInfo);
            }else{
                ConcurrentHashMap<Session, UserDTO> newChatroom = new ConcurrentHashMap<>();
                newChatroom.put(session, userInfo);
                chatrooms.put(chatroomId, newChatroom);
            }
            //on envoie un message de connexion à tous les utilisateurs connectés dans la chatroom correspondante
            Date now = new Date();
            broadcastMessage(
                    setMessage(MESSAGE_CONNECT ,"",this.userInfo,now)
                    ,chatroomId
            );
            //Pour chaque utilisateur qui est déjà connecté dans le chatroom, on envoie un message de connexion à l'utilisateur qui ouvre la connexion
            for (Map.Entry<Session, UserDTO> entry : chatrooms.get(chatroomId).entrySet()) {
                if(!entry.getKey().equals(session)){
                    sendMsgToMe(
                            setMessage(MESSAGE_CONNECT,"",entry.getValue(),now)
                            ,chatroomId
                    );
                }
            }
            logger.info("Connection opened for user {} in chatroom {}", userInfo.getLastName()+" "+userInfo.getFirstName(), chatroomId);

        }catch (Exception e){
            //si on a une exception pendant l'ouverture de la connexion WebSocket, on ferme la connexion
            logger.error("Error while opening connection : ", e);
            try {
                session.close();
            } catch (IOException ioException) {
                logger.error("Error while closing session : ", ioException);
            }
        }
    }

    /**
     * Cette méthode permet de traiter la fermeture d'une connexion websocket
     */
    @OnClose
    public void fermetureConnection(Session session, @PathParam("chatroomId") long chatroomId){
        try{
            //on envoie un message de déconnexion à tous les utilisateurs connectés dans la chatroom correspondante
            broadcastMessageExceptSender(
                    setMessage(MESSAGE_DISCONNECT,"",this.userInfo,new Date())
                    ,chatroomId
            );
            //on supprime la session de l'utilisateur de la chatroom correspondante, si la chatroom est vide, on la supprime
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

    /**
     * Cette méthode permet de traiter les messages envoyés par l'utilisateur
     */
    @OnMessage
    public void receptionMessage(String message, @PathParam("chatroomId") long chatroomId){
        try{
        	Date now = new Date();
        	chatMessageService.saveMsgIntoCollection(chatroomId, userInfo, message, now);
            //on envoie le message à tous les utilisateurs connectés dans la chatroom correspondante
            broadcastMessage(
                    setMessage(MESSAGE_TEXT,message,this.userInfo,now)
                    ,chatroomId
            );
        }catch (Exception e){
            logger.error("Error while receiving message : ", e);
        }
    }

    /**
     * Cette méthode permet de traiter les erreurs
     */
    @OnError
    public void onError(Throwable error) {
        logger.error("Error occurred");
        error.printStackTrace();
    }
    
    /*
     * cette methode surveille l'evenenment de suppression des chatrooms
     */
    //@EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void removeEventListener(RemoveChatroomEvent event) {
    	try {
    		long chatroomId = event.getEventMsg();
	    	broadcastMessage(
	    			setMessage(MESSAGE_REMOVE_CHATROOM,"This chatroom has been removed!",new UserDTO(),new Date()),
	    			chatroomId
	    	);
	    	if(chatrooms.containsKey(chatroomId)) chatrooms.remove(chatroomId);
    	}catch(Exception ex) {
    		logger.error("Error while deleting users in chatroom " + event.getEventMsg());
            ex.printStackTrace();
    	}
    }
    
	/*
	 * cette methode surveille l'evenenment de changement des membres des chatrooms
	 */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void changeChatroomMemberEventListener(ChangeChatroomMemberEvent event) {
    	try {
    		long chatroomId = event.getChatroomId();
    		for(UserDTO user : event.getAddedMembers()) {
    			broadcastMessage(
    					setMessage(MESSAGE_ADD_CHATROOM_MEMBER,"A new user has joined the chatroom!", user, new Date()),
    					chatroomId);
    		}
			for(UserDTO user : event.getRemovedMembers()) {
				broadcastMessage(
						setMessage(MESSAGE_REMOVE_CHATROOM_MEMBER, "A user has left the chatroom!", user, new Date()),
						chatroomId);
			}
    	}catch(Exception ex) {
    		logger.error("Error while updating users in chatroom " + event.getChatroomId());
            ex.printStackTrace();
        }
    }

    /**
     * Cette méthode permet de construire le message à envoyer en Json
     */
    private String setMessage(int messageType, String message, UserDTO userInfo, Date now) {
        //format : {user: {id: 1, username: "user1 user1"}, messageType: 0, message: "hello" , timestamp : "18:00"}
        try {
            //Date now = new Date();
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

    /**
     * Cette méthode permet d'envoyer un message à tous les utilisateurs connectés dans la chatroom correspondante (inclus l'utilisateur qui envoie le message)
     */
    private static void broadcastMessage(String message, long chatroomId){
        if(chatrooms.containsKey(chatroomId)){
            chatrooms.get(chatroomId).forEach((session, user) -> {
                //session.getBasicRemote().sendText(message);
                if(!message.equals(""))
                    session.getAsyncRemote().sendText(message);
            });
        }
    }

    /**
     * Cette méthode permet d'envoyer un message à tous les utilisateurs connectés dans la chatroom correspondante (exclu l'utilisateur qui envoie le message)
     */
    private void broadcastMessageExceptSender(String message, long chatroomId){
        if(chatrooms.containsKey(chatroomId)){
            chatrooms.get(chatroomId).forEach((session, user) -> {
                if(!(user.getId() == this.userInfo.getId()) && !message.equals(""))
                    session.getAsyncRemote().sendText(message);
            });
        }
    }

    /**
     * Cette méthode permet d'envoyer un message seulement à l'utilisateur qui envoie le message
     */
    private void sendMsgToMe(String message, long chatroomId){
        if(chatrooms.containsKey(chatroomId)){
            chatrooms.get(chatroomId).forEach((session, user) -> {
                if(user.getId() == this.userInfo.getId() && !message.equals(""))
                    session.getAsyncRemote().sendText(message);
            });
        }
    }

}
