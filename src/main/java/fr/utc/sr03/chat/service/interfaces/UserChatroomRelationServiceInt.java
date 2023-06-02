package fr.utc.sr03.chat.service.interfaces;

import fr.utc.sr03.chat.model.Chatroom;
import fr.utc.sr03.chat.model.User;
import fr.utc.sr03.chat.model.UserChatroomRelation;

import java.util.List;
import java.util.Optional;

public interface UserChatroomRelationServiceInt{
    List<UserChatroomRelation> findRelationsOfUser(long userId);

    void addRelation(long userId, long chatroomId , boolean isOwned);

    Optional<UserChatroomRelation> findOwnerOfChatroom(long chatroomId);

    void deleteRelation(UserChatroomRelation relation);
}
