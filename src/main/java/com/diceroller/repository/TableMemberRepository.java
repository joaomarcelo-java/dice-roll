package com.diceroller.repository;

import com.diceroller.domain.game.GameTable;
import com.diceroller.domain.game.TableMember;
import com.diceroller.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TableMemberRepository extends JpaRepository<TableMember, Long> {
    List<TableMember> findByTable(GameTable table);
    Optional<TableMember> findByTableAndUser(GameTable table, User user);
    void deleteByTableAndUser(GameTable table, User user);
    void deleteByTable(GameTable table);
}
