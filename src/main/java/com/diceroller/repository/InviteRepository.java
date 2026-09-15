package com.diceroller.repository;


import com.diceroller.domain.game.GameTable;
import com.diceroller.domain.game.Invite;
import com.diceroller.domain.game.InviteStatus;
import com.diceroller.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InviteRepository extends JpaRepository<Invite, Long> {
    List<Invite> findByTargetAndStatus(User target, InviteStatus status);
    Optional<Invite> findByTableAndTargetAndStatus(GameTable table, User target, InviteStatus status);
    Optional<Invite> findByIdAndTarget(Long inviteId, User target);
    void deleteByTable(GameTable table);
}
