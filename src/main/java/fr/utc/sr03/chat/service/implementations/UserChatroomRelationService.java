package fr.utc.sr03.chat.service.implementations;

import fr.utc.sr03.chat.dao.UserChatroomRelationRepository;
import fr.utc.sr03.chat.model.UserChatroomRelation;
import fr.utc.sr03.chat.service.interfaces.UserChatroomRelationServiceInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserChatroomRelationService implements UserChatroomRelationServiceInt {

    @Autowired
    private UserChatroomRelationRepository userChatroomRelationRepository;

    @Override
    public List<UserChatroomRelation> findRelationsOfUser(long userId) {
        return userChatroomRelationRepository.findByUserId(userId);
    }

    @Override
    public void addRelation(long userId, long chatroomId , boolean isOwned) {
        UserChatroomRelation relation = new UserChatroomRelation();
        relation.setUserId(userId);
        relation.setChatroomId(chatroomId);
        relation.setOwned(isOwned);
        userChatroomRelationRepository.save(relation);
    }

    @Override
    public List<UserChatroomRelation> findUsersInvitedToChatroom(long chatroomId) {
        return userChatroomRelationRepository.findByChatroomIdAndOwned(chatroomId,false);
    }

    @Override
    public Optional<UserChatroomRelation> findOwnerOfChatroom(long chatroomId) {
        return userChatroomRelationRepository.findByChatroomIdAndOwned(chatroomId,true).stream().findFirst();
    }

    @Override
    public void deleteRelation(UserChatroomRelation relation) {
        userChatroomRelationRepository.delete(relation);
    }

}
