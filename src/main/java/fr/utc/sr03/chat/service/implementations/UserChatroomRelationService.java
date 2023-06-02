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

    /**
     * Cette méthode permet de trouver les relations entre un utilisateur et les chatrooms
     */
    @Override
    public List<UserChatroomRelation> findRelationsOfUser(long userId) {
        return userChatroomRelationRepository.findByUserId(userId);
    }

    /**
     * Cette méthode permet d'ajouter une relation entre un utilisateur et un chatroom
     */
    @Override
    public void addRelation(long userId, long chatroomId , boolean isOwned) {
        UserChatroomRelation relation = new UserChatroomRelation();
        relation.setUserId(userId);
        relation.setChatroomId(chatroomId);
        relation.setOwned(isOwned);
        userChatroomRelationRepository.save(relation);
    }

    /**
     * Cette méthode permet de trouver le relation contenant l'utilisateur propriétaire du chatroom
     */
    @Override
    public Optional<UserChatroomRelation> findOwnerOfChatroom(long chatroomId) {
        //findByChatroomIdAndOzned retourne une liste de relations, on prend le premier élément de la liste,car il n'y a qu'un seul propriétaire
        return userChatroomRelationRepository.findByChatroomIdAndOwned(chatroomId,true).stream().findFirst();
    }

    /**
     * Cette méthode permet de supprimer une relation entre un utilisateur et un chatroom
     */
    @Override
    public void deleteRelation(UserChatroomRelation relation) {
        userChatroomRelationRepository.delete(relation);
    }

}
