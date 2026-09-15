package com.diceroller.dto.response;

import com.diceroller.domain.dice.DiceRoll;

import java.util.List;

public record RollLinkResponse(
        String categoria,
        String campo,
        String dado,
        List<DiceRoll> rolagens,
        CampoValor campoSomado,
        Integer valorCampo,
        int total
) {
    public record CampoValor(String categoria, String campo, int valor){}
}
