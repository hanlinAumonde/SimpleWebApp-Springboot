package com.devStudy.chat.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.devStudy.chat.service.utils.ConstantValues.MINUTES_EXPIRATION_TOKEN;

@Entity
@Table(name = "reset_password_validate")
public class ResetPasswordValidate {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "token")
    private UUID token;

    @ManyToOne/*(targetEntity = User.class, fetch = FetchType.EAGER)*/(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    public ResetPasswordValidate() {
    }

    public ResetPasswordValidate(UUID token, User user) {
        this.token = token;
        this.user = user;
        this.expiryDate = LocalDateTime.now().plusMinutes(MINUTES_EXPIRATION_TOKEN);
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public UUID getToken() {
        return this.token;
    }

    public void setToken(UUID token) {
        this.token = token;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getExpiryDate() {
        return this.expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }
}