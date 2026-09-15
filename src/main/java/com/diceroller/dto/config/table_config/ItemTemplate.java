package com.diceroller.dto.config.table_config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ItemTemplate(@NotBlank @Size(max = 24) String nome,
                           @NotBlank @Size(max = 16) String tipo,
                           Boolean causaDano,
                           Boolean causaCura,
                           @Size(max = 4) String dado,
                           @Size(max = 256) String descricao,
                           Integer usos,
                           Long id) {

    public ItemTemplate{
        if((Boolean.TRUE.equals(causaCura) || Boolean.TRUE.equals(causaDano)) && (dado == null || dado.isBlank())){
            throw new RuntimeException("Se o item causa dano, ou causa cura, obrigatoriamente ele deve ter um dado.");
        }
    }
}
