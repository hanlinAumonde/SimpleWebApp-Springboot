package fr.utc.sr03.chat.service.utils;

import java.util.List;

public class ChatroomDTO {
    public long id;
    public String titre;
    public String description;
    //getters and setters
    public long getId() {
        return this.id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getTitre() {
        return this.titre;
    }
    public void setTitre(String titre) {
        this.titre= titre;
    }
    public String getDescription() {
        return this.description;
    }
    public void setDescription(String description) {
        this.description= description;
    }
}
