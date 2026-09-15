package com.diceroller.domain.game;

import com.diceroller.domain.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "game_tables")
public class GameTable {
    @Id @GeneratedValue()
    private long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "master_id", nullable = false)
    private User master;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String characterConfig;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String inventoryData;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate(){ this.createdAt = LocalDateTime.now(); }

    public GameTable(){}

    public GameTable(String name, String description, User master){
        this.name = name;
        this.description = description;
        this.master = master;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public User getMaster() {
        return master;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCharacterConfig() {
        return characterConfig;
    }

    public void setCharacterConfig(String characterConfig) {
        this.characterConfig = characterConfig;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInventoryData() {
        return inventoryData;
    }

    public void setInventoryData(String inventoryData) {
        this.inventoryData = inventoryData;
    }
}
