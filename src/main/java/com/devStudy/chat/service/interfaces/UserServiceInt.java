package com.devStudy.chat.service.interfaces;

import org.springframework.data.domain.Page;

import com.devStudy.chat.dto.CreateCompteDTO;
import com.devStudy.chat.dto.UserDTO;
import com.devStudy.chat.model.User;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public interface UserServiceInt {

    UserDTO getLoggedUser();
    Page<User> findAllUsersByPage(int page);

    CreateCompteDTO addUser(CreateCompteDTO user);

    Page<User> findAllUsersNotAdminByPage(int page);

    Page<UserDTO> findAllOtherUsersNotAdminByPage(int page, long userId);

    Page<UserDTO> findUsersInvitedToChatroomByPage(long chatroomId, int page);

    Page<UserDTO> findUsersNotInvitedToChatroomByPage(long chatroomId, int page);

    void deleteUserById(Long id);

    Page<User> findAllInactiveUsersByPage(int page);

    void setStatusOfUser(long userId,boolean status);

    void setFailedAttemptsOfUser(long userId, int failedAttempts);

    void lockUserAndResetFailedAttempts(long userId);

    Optional<User> findUserByEmail(String email);

    Optional<User> findUserById(long userId);

    Optional<User> findUserOrAdmin(String email, boolean isAdmin);

    void sendResetPasswordEmail(User user, HttpServletRequest request);

    void resetPassword(User user, String password);

    boolean checkUserLoginStatus();
}