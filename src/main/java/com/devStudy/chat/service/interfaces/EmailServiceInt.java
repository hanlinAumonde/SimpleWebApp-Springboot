package com.devStudy.chat.service.interfaces;

public interface EmailServiceInt {

    // envoyer un message simple à l'adresse email spécifiée
    void sendSimpleMessage(String to, String subject, String text);

    // envoyer un message HTML à l'adresse email spécifiée
    void sendHtmlMessage(String to, String subject, String text);

    // envoyer un message avec une pièce jointe à l'adresse email spécifiée
    void sendMessageWithAttachment(String to, String subject, String text, String pathToAttachment, String attachmentName);
}
