package com.diceroller.service;

import com.diceroller.domain.game.GameTable;
import com.diceroller.dto.config.table_config.ConfigDto;
import com.diceroller.repository.GameTableRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PremadeTemplateService {
    private final GameTableRepository gameTableRepository;
    private final ObjectMapper objectMapper;
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    public PremadeTemplateService(GameTableRepository gameTableRepository, ObjectMapper objectMapper) {
        this.gameTableRepository = gameTableRepository;
        this.objectMapper = objectMapper;
    }

    public List<Map<String, String>> listarTemplates() {
        List<Map<String, String>> templates = new ArrayList<>();

        try {
            Resource[] resources = resolver.getResources("classpath:templates/*.json");
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (filename != null) {
                    String name = filename.replace(".json", "");
                    String displayName = switch (name) {
                        case "fantasia-medieval" -> "Fantasia Medieval";
                        case "cyberpunk" -> "Cyberpunk";
                        case "horror" -> "Horror/Investigativo";
                        default -> name;
                    };
                    templates.add(Map.of("id", name, "nome", displayName));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao listar templates.");
        }

        return templates;
    }

    public ConfigDto aplicarTemplate(Long tableId, String templateName, Long userId) {
        GameTable table = gameTableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Mesa não encontrada."));

        if (table.getMaster().getId() != userId) {
            throw new RuntimeException("Apenas o mestre pode aplicar templates.");
        }

        try {
            Resource resource = resolver.getResource("classpath:templates/" + templateName + ".json");
            if (!resource.exists()) {
                throw new RuntimeException("Template '" + templateName + "' não encontrado.");
            }

            String json = new String(resource.getInputStream().readAllBytes());
            objectMapper.readTree(json);

            table.setCharacterConfig(json);
            gameTableRepository.save(table);

            return objectMapper.readValue(json, ConfigDto.class);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao aplicar template.");
        }
    }
}
