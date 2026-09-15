package com.diceroller.repository;

import com.diceroller.domain.game.GameTable;
import com.diceroller.domain.game.Npc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import com.diceroller.domain.game.GameTable;

public interface NpcRepository extends JpaRepository<Npc, Long> {
    List<Npc> findByTable(GameTable table);
    void deleteByTable(GameTable table);
}
