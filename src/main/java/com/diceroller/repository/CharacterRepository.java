package com.diceroller.repository;

import com.diceroller.domain.game.GameTable;
import com.diceroller.domain.user.User;
import com.diceroller.domain.game.Character;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import com.diceroller.domain.game.GameTable;
import com.diceroller.domain.user.User;

public interface CharacterRepository extends JpaRepository<Character, Long> {
    List<Character> findByTable(GameTable table);
    Optional<Character> findByTableAndPlayer(GameTable table, User player);
    void deleteByTable(GameTable table);
    void deleteByTableAndPlayer(GameTable table, User player);
}