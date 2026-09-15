package com.diceroller.dto.response;

import com.diceroller.domain.user.User;

import java.time.LocalDateTime;

public record MeResponseDto(
        Long id,
        String name,
        String email,
        LocalDateTime createdAt
) {
    public static MeResponseDto from(User user) {
        return new MeResponseDto(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
    }
}