package com.diceroller.controller;


import com.diceroller.domain.user.User;
import com.diceroller.dto.request.CreateNpcRequest;
import com.diceroller.dto.request.UpdatePvRequest;
import com.diceroller.dto.response.NpcResponse;
import com.diceroller.service.NpcService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables/{tableId}/npcs")
public class NpcController {
    private final NpcService npcService;

    public NpcController(NpcService npcService) {
        this.npcService = npcService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NpcResponse create(@PathVariable Long tableId,
                              @RequestBody CreateNpcRequest request,
                              @AuthenticationPrincipal User currentUser){
        return npcService.create(tableId,request,currentUser);
    }

    @GetMapping
    public List<NpcResponse> findAllByTable(@PathVariable Long tableId,
                                            @AuthenticationPrincipal User currentUser){
        return npcService.findAllByTable(tableId,currentUser);
    }

    @GetMapping("/{npcId}")
    public NpcResponse findById(@PathVariable Long tableId,
                                @PathVariable Long npcId,
                                @AuthenticationPrincipal User currentUser){
        return  npcService.findById(tableId,npcId,currentUser);
    }

    @PutMapping("/{npcId}")
    public NpcResponse update(@PathVariable Long tableId,
                              @PathVariable Long npcId,
                              @RequestBody CreateNpcRequest request,
                              @AuthenticationPrincipal User currentUser){
        return  npcService.update(tableId,npcId,request,currentUser);
    }

    @DeleteMapping("/{npcId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long tableId,
                       @PathVariable Long npcId,
                       @AuthenticationPrincipal User currentUser){
        npcService.delete(tableId,npcId,currentUser);
    }

    @PatchMapping("/{npcId}/pv")
    public NpcResponse updatePv(@PathVariable Long tableId,
                                @PathVariable Long npcId,
                                @RequestBody UpdatePvRequest request,
                                @AuthenticationPrincipal User currentUser){
        return npcService.updatePv(tableId,npcId,request,currentUser);
    }
}
