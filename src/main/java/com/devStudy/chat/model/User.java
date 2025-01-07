package com.devStudy.chat.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User implements UserDetails {
    private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy=GenerationType.IDENTITY) // strategy=GenerationType.IDENTITY => obligatoire pour auto increment mysql
    private long id;

    @Column(name = "firstname")
    private String firstName;

    @Column(name = "lastname")
    private String lastName;

    @Column(name = "mail")
    private String mail;

    @Column(name = "pwd")
    private String pwd;

    @Column(name = "is_admin")
    private boolean admin = false;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "failed_attempts")
    private int failedAttempts = 0;    
	
	@OneToMany(mappedBy="creator", cascade = CascadeType.ALL, fetch = FetchType.LAZY ,orphanRemoval = true) 
	private Set<Chatroom> createdRooms = new HashSet<>();
	 
	@ManyToMany
	@JoinTable(name = "user_chatroom_relationship", 
			   joinColumns = @JoinColumn(name="user_id"), 
			   inverseJoinColumns = @JoinColumn(name="chatroom_id")) 
	private Set<Chatroom> joinedRooms = new HashSet<>();
	 
    
    public User(){}

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
        this.lastName = lastName;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMail() {
        return this.mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPwd() {
        return this.pwd;
    }

    public void setPwd(String password) {
        this.pwd = password;
    }

    public boolean isAdmin() {
        return this.admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isActive() { return this.active; }

    public void setActive(boolean active) { this.active = active; }

    public int getFailedAttempts() {
        return this.failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }
	
	public Set<Chatroom> getCreatedRooms() {
		return createdRooms;
	}
	
    public Set<Chatroom> getJoinedRooms() {
    	return joinedRooms;
    }
    
    //Les méthodes ci-dessous sont obligatoires pour l'interface UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        String role = admin ? "ROLE_ADMIN" : "ROLE_USER";
        authorities.add(new SimpleGrantedAuthority(role));
        return authorities;
    }

    @Override
    public String getUsername() {
        return this.mail;
    }

    @Override
    public String getPassword() {
        return this.pwd;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return mail.equals(user.mail);
    }

    @Override
    public int hashCode() {
        return mail.hashCode();
    }
}
