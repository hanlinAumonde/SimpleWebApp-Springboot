package fr.utc.sr03.chat.dao;

import fr.utc.sr03.chat.model.UserChatroomRelation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserChatroomRelationRepository extends JpaRepository<UserChatroomRelation,Long> {
    List<UserChatroomRelation> findByUserIdAndOwned(long userId, boolean isOwned);

    List<UserChatroomRelation> findByUserId(long userId);

    List<UserChatroomRelation> findByChatroomIdAndOwned(long chatroomId, boolean isOwned);

    List<UserChatroomRelation> findByChatroomId(long chatroomId);

    Optional<UserChatroomRelation> findByChatroomIdAndUserId(long chatroomId, long userId);
}
