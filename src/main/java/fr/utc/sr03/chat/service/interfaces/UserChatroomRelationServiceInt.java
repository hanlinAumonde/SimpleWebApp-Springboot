package fr.utc.sr03.chat.service.interfaces;

import fr.utc.sr03.chat.model.UserChatroomRelation;

import java.util.List;

public interface UserChatroomRelationServiceInt{
    List<UserChatroomRelation> findRelationsOfUser(long userId);

    List<UserChatroomRelation> findChatroomsOwnedOrInviting(long userId, boolean isOwned);

    void deleteRelation(UserChatroomRelation relation);
}
