package fr.utc.sr03.chat.controller;

import fr.utc.sr03.chat.model.ResetPasswordValidate;
import fr.utc.sr03.chat.model.User;
import fr.utc.sr03.chat.service.implementations.ResetPasswordValidateService;
import fr.utc.sr03.chat.service.implementations.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping(value="/reset-password")
public class ResetPasswordController {

    @Resource
    private UserService userService;

    @Resource
    private ResetPasswordValidateService resetPasswordValidateService;

    /**
     * Cette méthode permet d'obtenir la page de formulaire de saisie du mail pour réinitialiser le mot de passe
     */
    @GetMapping(value = "/forget-password")
    public String getForgetPasswordPage(Model model, @ModelAttribute(value = "msg")String msg){
        if(msg.startsWith("Succès")) {
            model.addAttribute("success", msg);
        }else{
            model.addAttribute("error", msg);
        }
        return "forgetPassword";
    }

    /**
     * Cette méthode permet de traiter le formulaire de saisie du mail pour réinitialiser le mot de passe
     * Elle envoie un mail de réinitialisation de mot de passe à l'adresse mail saisie si l'adresse mail est valide
     */
    @PostMapping(value = "/forget-password")
    public String postForgetPasswordPage(@RequestParam(value = "mail") String email, HttpServletRequest request, RedirectAttributes redirectAttributes){
        Optional<User> userFound = userService.findUserByEmail(email);
        if(userFound.isPresent()){
            User user = userFound.get();
            redirectAttributes.addFlashAttribute("msg", "Succès : un mail de réinitialisation de mot de passe a été envoyé à l'adresse " + email
                    + ", veuillez cliquer sur le lien contenu dans le mail pour réinitialiser votre mot de passe");
            userService.sendResetPasswordEmail(user, request);
        }else{
            redirectAttributes.addFlashAttribute("msg", "Error : aucun utilisateur n'est enregistré avec l'adresse " + email + ", veuillez réessayer");
        }
        return "redirect:/reset-password/forget-password";
    }

    /**
     * Cette méthode permet d'obtenir la page de formulaire de saisie du nouveau mot de passe
     * si le lien de réinitialisation de mot de passe est valide et non expiré
     */
    @GetMapping(value = "/reset-password-form")
    public String getResetPasswordFormPage(@RequestParam(value = "token",required = false) String token,Model model,@ModelAttribute(value = "msg")String msg,@ModelAttribute(value = "errorToken")String errorToken){
        if(msg.startsWith("Succès")) {
            model.addAttribute("success", msg);
            return "resetPassword";
        }
        Optional<ResetPasswordValidate> resetPasswordValidate = token != null ? resetPasswordValidateService.findValidateByToken(UUID.fromString(token)) : resetPasswordValidateService.findValidateByToken(UUID.fromString(errorToken));

        if (resetPasswordValidate.isPresent()){
            if(resetPasswordValidate.get().isExpired() || msg.startsWith("Error - expired")){
                model.addAttribute("error", "Error : le lien de réinitialisation de mot de passe a expiré, veuillez réessayer");
                resetPasswordValidateService.deleteValidate(resetPasswordValidate.get());
            }
        }else{
            model.addAttribute("error", "Error : le lien de réinitialisation de mot de passe est invalide, veuillez réessayer");
        }
        model.addAttribute("token", token);
        return "resetPassword";
    }

    /**
     * Cette méthode permet de traiter le formulaire de saisie du nouveau mot de passe
     * Elle réinitialise le mot de passe de l'utilisateur si le lien de réinitialisation de mot de passe est valide et non expiré
     */
    @PutMapping(value = "/reset-password-form")
    public String updateNewPassword(@RequestParam(value = "token")String token, @RequestParam(value = "new_password") String password, RedirectAttributes redirectAttributes){
        Optional<ResetPasswordValidate> resetPasswordValidate = resetPasswordValidateService.findValidateByToken(UUID.fromString(token));
        if (resetPasswordValidate.isPresent()){
            if(resetPasswordValidate.get().isExpired()){
                redirectAttributes.addFlashAttribute("msg", "Error - expired");
            }else{
                User user = resetPasswordValidate.get().getUser();
                userService.resetPassword(user, password);
                resetPasswordValidateService.deleteValidate(resetPasswordValidate.get());
                redirectAttributes.addFlashAttribute("msg", "Succès : votre mot de passe a été réinitialisé avec succès");
            }
        }
        redirectAttributes.addFlashAttribute("errorToken", token);
        return "redirect:/reset-password/reset-password-form";
    }
}
