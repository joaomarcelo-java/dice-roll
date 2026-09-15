package com.diceroller.dto.request;

import jakarta.validation.constraints.*;

import java.util.List;
import java.util.Map;

public record CreateNpcRequest(
        @NotBlank @Size(max=50) String name,
        @NotBlank @Max(value=1000) @Min(value = 0)Integer pv,
        @NotNull @Max(value=100) @Min(value=0)Integer ca,
        Map<String, Integer> atributos,
        List<AcaoNpc> acoes
) {
    public record AcaoNpc(
            @NotBlank @Size(max=50)String nome,
            String dado,
            Integer modificador
    ){}
}
