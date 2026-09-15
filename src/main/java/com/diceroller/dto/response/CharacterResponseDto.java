package com.diceroller.dto.response;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import com.diceroller.domain.game.Character;

public record CharacterResponseDto(
        Long id,
        String characterName,
        Long playerId,
        String playerName,
        Long tableId,
        Map<String, Map<String, Object>> data,
        Map<String, InventoryItem> inventory,
        Map<String, Integer> evolutionData,
        LocalDateTime createdAt
) {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static CharacterResponseDto from(Character character) {
        try {
            Map<String, Map<String, Object>> dataMap = mapper.readValue(character.getData(), new TypeReference<>() {});
            Map<String, InventoryItem> inventoryMap = character.getInventoryData() != null
                    ? mapper.readValue(character.getInventoryData(), new TypeReference<>() {})
                    : new LinkedHashMap<>();
            Map<String, Integer> evolutionMap = character.getEvolutionData() != null
                    ? mapper.readValue(character.getEvolutionData(), new TypeReference<>() {})
                    : new LinkedHashMap<>();
            return new CharacterResponseDto(
                    character.getId(), character.getCharacterName(),
                    character.getPlayer().getId(), character.getPlayer().getName(),
                    character.getTable().getId(), dataMap, inventoryMap, evolutionMap,
                    character.getCreatedAt());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao desserializar o JSON na resposta.");
        }
    }

    public static CharacterResponseDto withoutInventory(Character character) {
        try {
            Map<String, Map<String, Object>> dataMap = mapper.readValue(character.getData(), new TypeReference<>() {});
            Map<String, Integer> evolutionMap = character.getEvolutionData() != null
                    ? mapper.readValue(character.getEvolutionData(), new TypeReference<>() {})
                    : new LinkedHashMap<>();
            return new CharacterResponseDto(
                    character.getId(), character.getCharacterName(),
                    character.getPlayer().getId(), character.getPlayer().getName(),
                    character.getTable().getId(), dataMap, new LinkedHashMap<>(), evolutionMap,
                    character.getCreatedAt());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao desserializar o JSON na resposta.");
        }
    }

    public static CharacterResponseDto fromData(Character character, Map<String, Map<String, Object>> data) {
        try {
            Map<String, InventoryItem> inventoryMap = character.getInventoryData() != null
                    ? mapper.readValue(character.getInventoryData(), new TypeReference<>() {})
                    : new LinkedHashMap<>();
            Map<String, Integer> evolutionMap = character.getEvolutionData() != null
                    ? mapper.readValue(character.getEvolutionData(), new TypeReference<>() {})
                    : new LinkedHashMap<>();
            return new CharacterResponseDto(
                    character.getId(), character.getCharacterName(),
                    character.getPlayer().getId(), character.getPlayer().getName(),
                    character.getTable().getId(), data, inventoryMap, evolutionMap,
                    character.getCreatedAt());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao desserializar o JSON na resposta.");
        }
    }
}