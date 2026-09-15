package com.diceroller.dto.response;

import com.diceroller.domain.game.GameTable;

import java.lang.reflect.Member;
import java.time.LocalDateTime;

public record TableResponseDto(Long id, String name, String description,
                               Long masterId, String masterName,
                               MemberRole role, Long memberCount,
                               LocalDateTime createdAt) {
    public static TableResponseDto from(GameTable table, MemberRole role, Long memberCount) {
        return new TableResponseDto(
                table.getId(), table.getName(), table.getDescription(),
                table.getMaster().getId(), table.getMaster().getName(),
                role, memberCount, table.getCreatedAt());
    }
}
