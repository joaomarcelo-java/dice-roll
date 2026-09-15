package com.diceroller.dto.response;

public record InventoryItem(
        String nome,
        String tipo,
        Boolean causaDano,
        Boolean causaCura,
        String dado,
        String descricao,
        Integer quantidade,
        Integer usos
) {}
