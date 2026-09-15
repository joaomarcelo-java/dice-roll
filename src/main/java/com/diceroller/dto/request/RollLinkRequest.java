package com.diceroller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RollLinkRequest(
        @NotBlank String categoria,
        @NotBlank String campo
) {
}
