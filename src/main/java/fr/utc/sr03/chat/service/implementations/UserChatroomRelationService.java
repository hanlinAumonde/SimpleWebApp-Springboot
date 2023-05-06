package fr.utc.sr03.chat.service.implementations;

import fr.utc.sr03.chat.dao.UserChatroomRelationRepository;
import fr.utc.sr03.chat.model.UserChatroomRelation;
import fr.utc.sr03.chat.service.interfaces.UserChatroomRelationServiceInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserChatroomRelationService implements UserChatroomRelationServiceInt {

    @Autowired
    private UserChatroomRelationRepository userChatroomRelationRepository;

    @Override
    public List<UserChatroomRelation> findRelationsOfUser(long userId) {
        return userChatroomRelationRepository.findByUserId(userId);
    }

    @Override
    public void deleteRelation(UserChatroomRelation relation) {
        userChatroomRelationRepository.delete(relation);
    }

    @Override
    public List<UserChatroomRelation> findChatroomsOwnedOrInviting(long userId, boolean isOwned) {
        return userChatroomRelationRepository.findByUserIdAndOwned(userId, isOwned);
    }
}
