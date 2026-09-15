package com.diceroller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequestDto(String token,
                                      @NotBlank
                                      @Size(min=8)
                                      @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$",
                                              message = "Senha deve ter no mínimo 8 caracteres, uma maiúscula, um número e um caractere especial")
                                      String senha) {
}
