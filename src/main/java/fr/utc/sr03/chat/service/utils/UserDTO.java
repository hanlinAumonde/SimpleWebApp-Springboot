package fr.utc.sr03.chat.service.utils;

public class UserDTO {
    public long id;
    public String lastName;
    public String firstName;
    public String mail;

    //getters and setters
    public long getId() {
        return this.id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getLastName() {
        return this.lastName;
    }
    public void setLastName(String lastName) {
        this.lastName= lastName;
    }
    public String getFirstName() {
        return this.firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName= firstName;
    }
    public String getMail() {
        return this.mail;
    }
    public void setMail(String mail) {
        this.mail= mail;
    }

}
