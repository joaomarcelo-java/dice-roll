package com.diceroller.domain.game;


import com.diceroller.domain.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name="characters", uniqueConstraints = @UniqueConstraint(columnNames = {"table_id", "player_id"}))
public class Character {
    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String characterName;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private GameTable table;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private User player;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String data;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String evolutionData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String inventoryData;

    private LocalDateTime createdAt;

    public Character(){}

    public Character(String characterName, String data, User player, GameTable table) {
        this.characterName = characterName;
        this.data = data;
        this.player = player;
        this.table = table;
    }

    @PrePersist
    public void onCreate(){ this.createdAt = LocalDateTime.now(); }

    public Long getId() {
        return id;
    }

    public String getCharacterName() {
        return characterName;
    }

    public GameTable getTable() {
        return table;
    }

    public User getPlayer() {
        return player;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getData() {
        return data;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getEvolutionData() {
        return evolutionData;
    }

    public void setEvolutionData(String evolutionData) {
        this.evolutionData = evolutionData;
    }

    public String getInventoryData() {
        return inventoryData;
    }

    public void setInventoryData(String inventoryData) {
        this.inventoryData = inventoryData;
    }
}
