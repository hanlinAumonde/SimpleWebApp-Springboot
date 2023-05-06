package fr.utc.sr03.chat.model;

import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "chatrooms")
public class Chatroom {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY) // strategy=GenerationType.IDENTITY => obligatoire pour auto increment mysql
    private long id;

    @Column(name = "titre")
    private String titre;

    @Column(name = "description")
    private String description;

    @Column(name = "horaire_commence")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date HoraireCommence;

    @Column(name = "horaire_termine")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date HoraireTermine;

    @Column(name = "is_active")
    private boolean active;

    public Chatroom(){}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getHoraireCommence() {
        return HoraireCommence;
    }

    public void setHoraireCommence(Date horaireCommence) {
        this.HoraireCommence = horaireCommence;
    }

    public Date getHoraireTermine() {
        return HoraireTermine;
    }

    public void setHoraireTermine(Date horaireTermine) {
        this.HoraireTermine = horaireTermine;
    }

    public boolean isActive() { return active; }

    public void setActive(boolean active) { this.active = active; }
}
