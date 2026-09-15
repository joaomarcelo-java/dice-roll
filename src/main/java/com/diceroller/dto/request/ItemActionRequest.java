package com.diceroller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ItemActionRequest(
        @NotBlank String itemName,
        @Positive int quantidade
) {}
