package fr.utc.sr03.chat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping(value="/errorPage")
public class ErrorPageController {
    @GetMapping(value="/{code}")
    public String getErrorPage(@PathVariable int code, HttpServletRequest req, Model model){
        HttpSession session = req.getSession();
        String errorInfo = (String)session.getAttribute("errorInfo");
        model.addAttribute("errorCode",code);
        model.addAttribute("errorInfo", errorInfo);
        return "errorPage";
    }
}
