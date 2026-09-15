package com.diceroller.domain.game;

import com.diceroller.domain.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name="table_members")
public class TableMember {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name="table_id", nullable = false)
    private GameTable table;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime joinedAt;

    @PrePersist
    public void onCreate() {this.joinedAt = LocalDateTime.now();}

    public TableMember(){}

    public TableMember(GameTable table, User user) {
        this.table = table;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public GameTable getTable() {
        return table;
    }

    public User getUser() {
        return user;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setTable(GameTable table) {
        this.table = table;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
