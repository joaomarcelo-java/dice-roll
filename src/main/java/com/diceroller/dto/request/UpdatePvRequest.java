package com.diceroller.dto.request;

import jakarta.validation.constraints.Positive;

public record UpdatePvRequest(
        @Positive Integer dano,
        @Positive Integer cura) {
}
