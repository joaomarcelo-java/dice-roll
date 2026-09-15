package com.diceroller.dto.request;

import jakarta.validation.constraints.*;

public record RegisterRequestDto(
        @NotBlank @Size(max = 32) String name,
        @NotBlank @Email @Size(max = 100) String email,

        @NotBlank
        @Size(min=8)
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$",
                message = "Senha deve ter no mínimo 8 caracteres, uma maiúscula, um número e um caractere especial")
        String password
        ) {}
