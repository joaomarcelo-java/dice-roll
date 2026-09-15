package com.diceroller.controller.invite;

import com.diceroller.domain.user.User;
import com.diceroller.dto.request.CreateInviteRequest;
import com.diceroller.dto.response.InviteResponse;
import com.diceroller.service.InviteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tables/{tableId}/invites")
public class InviteController {
    private final InviteService inviteService;

    public InviteController(InviteService inviteService) {
        this.inviteService = inviteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InviteResponse send(@PathVariable Long tableId,
                               @RequestBody @Valid CreateInviteRequest request,
                               @AuthenticationPrincipal User currentUser){
        return inviteService.send(tableId,request,currentUser);
    }
}
