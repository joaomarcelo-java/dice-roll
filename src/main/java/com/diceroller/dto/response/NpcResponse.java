package com.diceroller.dto.response;

import com.diceroller.domain.game.Npc;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record NpcResponse(
        Long id,
        String name,
        Integer pv,
        Integer ca,
        Map<String, Integer> atributos,
        List<Map<String, Object>> acoes,
        LocalDateTime createdAt
) {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static NpcResponse from(Npc npc) throws JsonProcessingException {
        Map<String, Integer> atr = mapper.readValue(npc.getAtributos(), new TypeReference<>() {});
        List<Map<String, Object>> ac = mapper.readValue(npc.getAcoes(), new TypeReference<>() {});
        return new NpcResponse(npc.getId(), npc.getName(), npc.getPv(), npc.getCa(), atr, ac, npc.getCreatedAt());
    }
}
