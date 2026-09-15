package com.diceroller.domain.user;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name="reset_token")
    private String resetToken;

    @Column(name="reset_token_exp")
    private LocalDateTime resetTokenExp;


    private LocalDateTime createdAt;



    @PrePersist
    public void onCreate(){
         this.createdAt = LocalDateTime.now();
    }

    public User(){}

    public User(String name, String email, String password){
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public LocalDateTime getResetTokenExp() { return resetTokenExp; }
    public void setResetTokenExp(LocalDateTime resetTokenExp) {
        this.resetTokenExp = resetTokenExp;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
