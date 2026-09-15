package com.diceroller.dto.response;

import com.diceroller.domain.user.User;

public record AuthResponseDto(
        String token,
        Long userId,
        String nome,
        String email
) {
    public static AuthResponseDto of(User user, String token){
        return new AuthResponseDto(token, user.getId(), user.getName(), user.getEmail());
    }
}
