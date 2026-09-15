package com.diceroller.domain.game;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
public class Npc {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch=LAZY)
    @JoinColumn(name="table_id", nullable = false)
    private GameTable table;

    @Column(nullable = false)
    private String name;

    private Integer pv;
    private Integer ca;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String atributos;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private String acoes;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate(){
        this.createdAt = LocalDateTime.now();
    }

    public Npc(){}

    public Npc(GameTable table, String name, Integer pv, Integer ca, String atributos, String acoes) {
        this.table = table;
        this.name = name;
        this.pv = pv;
        this.ca = ca;
        this.atributos = atributos;
        this.acoes = acoes;
    }

    public Long getId() {
        return id;
    }

    public GameTable getTable() {
        return table;
    }

    public void setTable(GameTable table) {
        this.table = table;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPv() {
        return pv;
    }

    public void setPv(Integer pv) {
        this.pv = pv;
    }

    public Integer getCa() {
        return ca;
    }

    public void setCa(Integer ca) {
        this.ca = ca;
    }

    public String getAtributos() {
        return atributos;
    }

    public void setAtributos(String atributos) {
        this.atributos = atributos;
    }

    public String getAcoes() {
        return acoes;
    }

    public void setAcoes(String acoes) {
        this.acoes = acoes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
