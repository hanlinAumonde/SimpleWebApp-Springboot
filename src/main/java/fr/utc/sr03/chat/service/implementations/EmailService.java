package fr.utc.sr03.chat.service.implementations;

import fr.utc.sr03.chat.service.interfaces.EmailServiceInt;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmailService implements EmailServiceInt {

    private final Logger logger = org.slf4j.LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        //message.setCc(cc);
        message.setSubject(subject);
        message.setText(text);
        try {
            emailSender.send(message);
            logger.info("Email envoyé à " + to + " avec succès.");
        }
        catch (Exception exception) {
            logger.error("Erreur lors de l'envoi de l'email à " + to + " : " + exception.getMessage());
        }
    }

    @Override
    public void sendHtmlMessage(String to, String subject, String text) {
        MimeMessage message = emailSender.createMimeMessage();

        try{
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            //helper.setCc(cc);
            helper.setSubject(subject);
            helper.setText(text, true);
            emailSender.send(message);
            logger.info("Email envoyé à " + to + " avec succès.");
        }catch(MessagingException msgException){
            logger.error("Erreur lors de l'envoi de l'email à " + to + " : " + msgException.getMessage());
        }

    }

    @Override
    public void sendMessageWithAttachment(String to, String subject, String text, String pathToAttachment, String attachmentName) {
        MimeMessage message = emailSender.createMimeMessage();

        try{
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            //helper.setCc(cc);
            helper.setSubject(subject);
            helper.setText(text);
            helper.addAttachment(attachmentName, new ClassPathResource(pathToAttachment));
            emailSender.send(message);
            logger.info("Email envoyé à " + to + " avec succès.");
        }catch(MessagingException msgException){
            logger.error("Erreur lors de l'envoi de l'email à " + to + " : " + msgException.getMessage());
        }
    }
}
