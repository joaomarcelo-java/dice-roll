package com.diceroller.controller;

import com.diceroller.domain.user.User;
import com.diceroller.dto.request.CreateCharacterRequestDto;
import com.diceroller.dto.request.EvolveRequest;
import com.diceroller.dto.request.ItemActionRequest;
import com.diceroller.dto.request.RollLinkRequest;
import com.diceroller.dto.response.CharacterResponseDto;
import com.diceroller.dto.response.RollLinkResponse;
import com.diceroller.service.CharacterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables/{tableId}/characters")
public class CharacterController {
    private final CharacterService characterService;

    public CharacterController(CharacterService characterService){
        this.characterService = characterService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CharacterResponseDto create(@PathVariable Long tableId,
                                       @RequestBody @Valid CreateCharacterRequestDto request,
                                       @AuthenticationPrincipal User currentUser){
        return characterService.create(tableId, request, currentUser);
    }

    @GetMapping
    public List<CharacterResponseDto> findAll(@PathVariable Long tableId,
                                              @AuthenticationPrincipal User currentUser){
        return characterService.findAllByTable(tableId, currentUser);
    }
    @GetMapping("/{charId}")
    public CharacterResponseDto findById(@PathVariable Long tableId,
                                         @PathVariable Long charId,
                                         @AuthenticationPrincipal User currentUser){
        return characterService.findById(tableId, charId, currentUser);
    }

    @PutMapping("/{charId}")
    public CharacterResponseDto update(@PathVariable Long tableId,
                                       @PathVariable Long charId,
                                       @RequestBody @Valid CreateCharacterRequestDto request,
                                       @AuthenticationPrincipal User currentUser){
        return characterService.update(tableId, charId, request, currentUser);
    }

    @DeleteMapping("/{charId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long tableId,
                       @PathVariable Long charId,
                       @AuthenticationPrincipal User currentUser){
        characterService.delete(tableId, charId, currentUser);
    }

    @PostMapping("/{charId}/evolve")
    @ResponseStatus(HttpStatus.OK)
    public void evolve(@PathVariable Long tableId,
                       @PathVariable Long charId,
                       @RequestBody @Valid EvolveRequest request,
                       @AuthenticationPrincipal User currentUser){
        characterService.evolve(tableId, charId, request, currentUser);
    }

    @PostMapping("/{charId}/items")
    @ResponseStatus(HttpStatus.OK)
    public void addItem(@PathVariable Long tableId,
                        @PathVariable Long charId,
                        @RequestBody @Valid ItemActionRequest request,
                        @AuthenticationPrincipal User currentUser) {
        characterService.addItem(tableId, charId, request, currentUser);
    }

    @DeleteMapping("/{charId}/items")
    @ResponseStatus(HttpStatus.OK)
    public void removeItem(@PathVariable Long tableId,
                           @PathVariable Long charId,
                           @RequestBody @Valid ItemActionRequest request,
                           @AuthenticationPrincipal User currentUser) {
        characterService.removeItem(tableId, charId, request, currentUser);
    }

    @PostMapping("/{charId}/roll-stats")
    public CharacterResponseDto rollStats(@PathVariable Long tableId,
                                          @PathVariable Long charId,
                                          @AuthenticationPrincipal User currentUser) {
        return characterService.rollStats(tableId, charId, currentUser);
    }

    @PostMapping("/{charId}/roll")
    public RollLinkResponse rollLinked(@PathVariable Long tableId,
                                       @PathVariable Long charId,
                                       @RequestBody @Valid RollLinkRequest request,
                                       @AuthenticationPrincipal User currentUser) {
        return characterService.rollLinked(tableId, charId, request, currentUser);
    }
}
