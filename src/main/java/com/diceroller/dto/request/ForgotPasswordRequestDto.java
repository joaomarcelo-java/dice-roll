package com.diceroller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ForgotPasswordRequestDto(@NotBlank @Email @Size(max = 100) String email) {
}
