package com.diceroller.repository;

import com.diceroller.domain.game.GameTable;
import com.diceroller.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GameTableRepository extends JpaRepository<GameTable, Long> {
    List<GameTable> findByMaster(User master);

    @Query("SELECT tm.table FROM TableMember tm WHERE tm.user = :user")
    List<GameTable> findByPlayer(@Param("user") User user);
}
