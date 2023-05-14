package fr.utc.sr03.chat.controller;

import fr.utc.sr03.chat.model.User;
import fr.utc.sr03.chat.service.implementations.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Optional;

@RestController
public class WebServiceController {

    @Resource
    private UserService userService;
    @GetMapping("/user")
    public User getUser(@RequestParam(value = "email") String email){
        Optional<User> user = userService.findUserByEmail(email);
        return user.orElseGet(User::new);
    }
}
