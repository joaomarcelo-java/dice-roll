package com.diceroller.controller;

import com.diceroller.domain.user.User;
import com.diceroller.dto.config.table_config.ConfigDto;
import com.diceroller.dto.config.table_config.ItemTemplate;
import com.diceroller.dto.request.CreateTableItemRequest;
import com.diceroller.dto.request.CreateTableRequestDto;
import com.diceroller.dto.response.TableResponseDto;
import com.diceroller.service.GameTableService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
public class GameTableController {
    private final GameTableService gameTableService;

    public GameTableController (GameTableService gameTableService){
        this.gameTableService = gameTableService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TableResponseDto create(@RequestBody @Valid CreateTableRequestDto request,
                                   @AuthenticationPrincipal User currentUser){
        return gameTableService.create(request, currentUser);
    }

    @GetMapping
    public List<TableResponseDto> findAll(@AuthenticationPrincipal User user){
        return gameTableService.findAllByUser(user);
    }

    @GetMapping("/{id}")
    public TableResponseDto findById(@PathVariable Long id,
                                     @AuthenticationPrincipal User currentUser){
        return gameTableService.findById(id, currentUser);
    }

    @PutMapping("/{id}")
    public TableResponseDto update(@PathVariable Long id,
                                   @RequestBody @Valid CreateTableRequestDto request,
                                   @AuthenticationPrincipal User currentUser){
        return gameTableService.update(id, request, currentUser);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id,
                       @AuthenticationPrincipal User user){
        gameTableService.delete(id, user);
    }

    @PutMapping("/{id}/config")
    public ConfigDto setConfig(@PathVariable Long id,
                                         @RequestBody @Valid ConfigDto request,
                                         @AuthenticationPrincipal User user){
        return gameTableService.setConfig(id, request, user);
    }

    @GetMapping("/{id}/config")
    public ConfigDto getConfig(@PathVariable Long id,
                               @AuthenticationPrincipal User currentUser){
        return gameTableService.getConfig(id, currentUser);
    }

    @PostMapping("/{id}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public ItemTemplate addItem(@PathVariable Long id,
                                 @RequestBody @Valid CreateTableItemRequest request,
                                 @AuthenticationPrincipal User currentUser) {
        return gameTableService.addItem(id, request, currentUser);
    }

    @GetMapping("/{id}/items")
    public List<ItemTemplate> listItems(@PathVariable Long id,
                                        @AuthenticationPrincipal User currentUser) {
        return gameTableService.listItems(id, currentUser);
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(@PathVariable Long id,
                           @PathVariable Long itemId,
                           @AuthenticationPrincipal User currentUser) {
        gameTableService.removeItem(id, itemId, currentUser);
    }
}
