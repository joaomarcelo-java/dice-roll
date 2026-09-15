package com.diceroller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTableItemRequest(
        @NotBlank @Size(max = 24) String nome,
        @NotBlank @Size(max = 16) String tipo,
        Boolean causaDano,
        Boolean causaCura,
        @Size(max = 4) String dado,
        @Size(max = 256) String descricao,
        Integer usos
) {}
