package com.devStudy.chat.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "chatrooms")
public class Chatroom {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false) 
    private User creator;

    @ManyToMany(mappedBy = "joinedRooms", fetch = FetchType.LAZY) 
    private Set<User> members = new HashSet<>();


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
    
	public User getCreator() {
		return creator;
	}
	
	public void setCreator(User creator) {
		this.creator = creator;
	}
	
	public Set<User> getMembers() {
		return members;
	}
	
	public void setMembers(Set<User> members) {
		this.members = members;
	}

    public boolean hasNotStarted() {
        return LocalDateTime.now().isBefore(this.horaireCommence);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Chatroom chatroom = (Chatroom) obj;

        return id == chatroom.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
