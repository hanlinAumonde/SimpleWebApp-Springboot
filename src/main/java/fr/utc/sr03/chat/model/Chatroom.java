package fr.utc.sr03.chat.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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
    private LocalDateTime horaireCommence;

    @Column(name = "horaire_termine")
    private LocalDateTime horaireTermine;

    @Column(name = "is_active")
    private boolean active;
    
	/*
	 * @ManyToOne(fetch = FetchType.LAZY)
	 * 
	 * @JoinColumn(name = "creator_id", nullable = false) private User creator;
	 * 
	 * @ManyToMany(mappedBy = "joinedRooms") private Set<User> members = new
	 * HashSet<>();
	 */

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

    public LocalDateTime getHoraireCommence() {
        return horaireCommence;
    }

    public void setHoraireCommence(LocalDateTime horaireCommence) {
        this.horaireCommence = horaireCommence;
    }

    public LocalDateTime getHoraireTermine() {
        return horaireTermine;
    }

    public void setHoraireTermine(LocalDateTime horaireTermine) {
        this.horaireTermine = horaireTermine;
    }

    public boolean isActive() { return active; }

    public void setActive(boolean active) { this.active = active; }

    public boolean hasNotStarted() {
        return LocalDateTime.now().isBefore(this.horaireCommence);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Chatroom chatroom = (Chatroom) obj;

        return titre.equals(chatroom.titre) && description.equals(chatroom.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(titre, description);
    }

}
