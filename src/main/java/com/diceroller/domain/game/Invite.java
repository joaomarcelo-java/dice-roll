package com.diceroller.domain.game;

import com.diceroller.domain.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name="invites")
public class Invite {
    @Id @GeneratedValue()
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private GameTable table;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User from;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "target_id", nullable = false)
    private User target;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InviteStatus status;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate(){this.createdAt = LocalDateTime.now();}

    public Invite(){}

    public Invite(GameTable table, User from, User target, InviteStatus status) {
        this.table = table;
        this.from = from;
        this.target = target;
        this.status = status;
    }

    public GameTable getTable() {
        return table;
    }

    public void setTable(GameTable table) {
        this.table = table;
    }

    public User getFrom() {
        return from;
    }

    public void setFrom(User from) {
        this.from = from;
    }

    public User getTarget() {
        return target;
    }

    public void setTarget(User target) {
        this.target = target;
    }

    public InviteStatus getStatus() {
        return this.status;
    }

    public void setStatus(InviteStatus status) {
        this.status = status;
    }

    public Long getId(){
        return this.id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
