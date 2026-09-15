package com.diceroller.dto.response;

import com.diceroller.domain.game.Invite;
import com.diceroller.domain.game.InviteStatus;

import java.time.LocalDateTime;

public record InviteResponse(
        Long id,
        String tableName,
        String fromName,
        InviteStatus status
) {
    public static InviteResponse from(Invite invite) {
        return new InviteResponse(
                invite.getId(),
                invite.getTable().getName(),
                invite.getFrom().getName(),
                invite.getStatus()
        );
    }
}
