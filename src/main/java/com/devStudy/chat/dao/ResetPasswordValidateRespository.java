package com.devStudy.chat.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devStudy.chat.model.ResetPasswordValidate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResetPasswordValidateRespository extends JpaRepository<ResetPasswordValidate, Long> {

    Optional<ResetPasswordValidate> findByToken(UUID token);

    List<ResetPasswordValidate> findByUserId(long userId);
}
