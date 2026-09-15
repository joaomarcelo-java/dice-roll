package com.diceroller.dto.config.table_config;

public record ConfiguracaoRolagem(
        String tipo,
        String dado,
        String campoSomado,
        boolean somarValorCampo
) {
}
