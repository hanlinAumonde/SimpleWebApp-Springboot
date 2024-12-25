package com.devStudy.chat.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devStudy.chat.model.UserChatroomRelation;

import java.util.List;
import java.util.Optional;

public interface UserChatroomRelationRepository extends JpaRepository<UserChatroomRelation,Long> {

    List<UserChatroomRelation> findByUserId(long userId);

    List<UserChatroomRelation> findByChatroomIdAndOwned(long chatroomId, boolean isOwned);

    List<UserChatroomRelation> findByChatroomId(long chatroomId);

    Optional<UserChatroomRelation> findByChatroomIdAndUserId(long chatroomId, long userId);

    Optional<UserChatroomRelation> findByUserIdAndChatroomIdAndOwned(long userId, long chatroomId, boolean b);
}
