package com.diceroller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record EvolveRequest(
        @NotBlank String categoria,
        @Positive int pontos
) {}
