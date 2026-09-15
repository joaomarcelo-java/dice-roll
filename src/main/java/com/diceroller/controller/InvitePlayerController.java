package com.diceroller.controller;

import com.diceroller.domain.game.Invite;
import com.diceroller.domain.user.User;
import com.diceroller.dto.request.UpdateInviteRequestDto;
import com.diceroller.dto.response.InviteResponse;
import com.diceroller.service.InviteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invites")
public class InvitePlayerController {
    private final InviteService inviteService;

    public InvitePlayerController(InviteService inviteService) {
        this.inviteService = inviteService;
    }

    @GetMapping("/pending")
    public List<InviteResponse> getPendingInvites(@AuthenticationPrincipal User currentUser){
        return inviteService.findPending(currentUser);
    }

    @PatchMapping("/{inviteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateInvite(@PathVariable Long inviteId,
                             @RequestBody @Valid UpdateInviteRequestDto request,
                             @AuthenticationPrincipal User currentUser){
        inviteService.updateInvite(inviteId, request, currentUser);
    }
}
