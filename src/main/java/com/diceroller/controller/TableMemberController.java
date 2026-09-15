package com.diceroller.controller;

import com.diceroller.domain.user.User;
import com.diceroller.dto.request.AddMemberRequest;
import com.diceroller.dto.response.MemberResponseDto;
import com.diceroller.service.TableMemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables/{id}")
public class TableMemberController {
    private final TableMemberService tableMemberService;

    public TableMemberController(TableMemberService tableMemberService){
        this.tableMemberService = tableMemberService;
    }

    @PostMapping("/members")
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponseDto addMember(@PathVariable Long id,
                                       @RequestBody @Valid AddMemberRequest request,
                                       @AuthenticationPrincipal User currentUser
                                    )
    {
        return tableMemberService.addMember(id, request, currentUser);
    }

    @GetMapping("/members")
    @ResponseStatus(HttpStatus.OK)
    public List<MemberResponseDto> findAllFromTable(@PathVariable Long id,
                                                    @AuthenticationPrincipal User currentUser)
    {
        return tableMemberService.findAllByTable(id, currentUser);
    }

    @DeleteMapping("/members/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leave(@PathVariable Long id,
                      @AuthenticationPrincipal User currentUser)
    {
        tableMemberService.leave(id, currentUser);
    }

    @DeleteMapping("/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void kick(@PathVariable Long id,
                     @PathVariable Long userId,
                     @AuthenticationPrincipal User currentUser){
        tableMemberService.kick(id, userId, currentUser);
    }
}
