package com.diceroller.controller;

import com.diceroller.dto.config.table_config.ConfigDto;
import com.diceroller.service.PremadeTemplateService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.diceroller.domain.user.User;

@RestController
public class PremadeTemplateController {
    private final PremadeTemplateService templateService;

    public PremadeTemplateController(PremadeTemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping("/api/templates/premade")
    public List<Map<String, String>> listarTemplates() {
        return templateService.listarTemplates();
    }

    @PostMapping("/api/tables/{tableId}/template/from-premade/{templateName}")
    @ResponseStatus(HttpStatus.CREATED)
    public ConfigDto aplicarTemplate(@PathVariable Long tableId,
                                      @PathVariable String templateName,
                                      @AuthenticationPrincipal User currentUser) {
        return templateService.aplicarTemplate(tableId, templateName, currentUser.getId());
    }
}
