package com.diceroller.dto.response;

import com.diceroller.domain.game.TableMember;
import com.diceroller.domain.user.User;

import java.time.LocalDateTime;

public record MemberResponseDto(
        Long userId,
        String name,
        MemberRole role,
        LocalDateTime joinedAt
) {
    public static MemberResponseDto fromMaster(User user){
        return new MemberResponseDto(
                user.getId(),
                user.getName(),
                MemberRole.MESTRE,
                null
        );
    }

    public static MemberResponseDto fromPlayer(TableMember user){
        return new MemberResponseDto(
                user.getUser().getId(),
                user.getUser().getName(),
                MemberRole.PLAYER,
                user.getJoinedAt()
        );
    }
}
