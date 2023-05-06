package fr.utc.sr03.chat.service.implementations;

import fr.utc.sr03.chat.dao.UserRepository;
import fr.utc.sr03.chat.model.User;
import fr.utc.sr03.chat.service.interfaces.UserServiceInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserServiceInt {

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public boolean addUser(User user) {
        List<User> users = userRepository.findAll();
        for(User u : users){
            if(u.equals(user)){
                return false;
            }
        }
        user.setPwd(passwordEncoder.encode(user.getPwd()));
        userRepository.save(user);
        return true;
    }

    @Override
    public List<User> findAllUsersNotAdmin() {
        return userRepository.findByAdmin(false);
    }

    @Override
    public void deleteUserById(Long id) {
        User user = userRepository.findById(id).get();
        userRepository.delete(user);
    }

    @Override
    public List<User> findAllInactiveUsers() {
        return userRepository.findByAdmin(false);
    }

    @Transactional
    @Override
    public void setStatusOfUser(long userId,boolean status) {
        userRepository.updateActive(userId,status);
    }

    @Transactional
    @Override
    public void setFailedAttemptsOfUser(long userId, int failedAttempts) {
        userRepository.updateFailedAttempts(userId,failedAttempts);
    }

    @Transactional
    @Override
    public void lockUserAndResetFailedAttempts(long userId) {
        userRepository.updateActive(userId,false);
        userRepository.updateFailedAttempts(userId,0);
    }

    @Override
    public Optional<User> findUserOrAdmin(String email, boolean isAdmin) {
        return userRepository.findByMailAndAdmin(email, isAdmin);
    }

}
