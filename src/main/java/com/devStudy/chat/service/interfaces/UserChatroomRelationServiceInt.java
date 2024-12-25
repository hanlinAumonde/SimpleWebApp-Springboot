package com.devStudy.chat.service.interfaces;

import java.util.List;
import java.util.Optional;

import com.devStudy.chat.model.Chatroom;
import com.devStudy.chat.model.User;
import com.devStudy.chat.model.UserChatroomRelation;

public interface UserChatroomRelationServiceInt{
    List<UserChatroomRelation> findRelationsOfUser(long userId);

    void addRelation(long userId, long chatroomId , boolean isOwned);

    Optional<UserChatroomRelation> findOwnerOfChatroom(long chatroomId);

    void deleteRelation(UserChatroomRelation relation);
}
