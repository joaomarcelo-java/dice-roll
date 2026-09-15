package com.diceroller.service;

import com.diceroller.domain.game.GameTable;
import com.diceroller.domain.game.Invite;
import com.diceroller.domain.game.InviteStatus;
import com.diceroller.domain.game.TableMember;
import com.diceroller.domain.user.User;
import com.diceroller.dto.request.CreateInviteRequest;
import com.diceroller.dto.request.InviteAction;
import com.diceroller.dto.request.UpdateInviteRequestDto;
import com.diceroller.dto.response.InviteResponse;
import com.diceroller.repository.GameTableRepository;
import com.diceroller.repository.InviteRepository;
import com.diceroller.repository.TableMemberRepository;
import com.diceroller.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class InviteService {
    private final InviteRepository inviteRepository;
    private final UserRepository userRepository;
    private final GameTableRepository gameTableRepository;
    private final TableMemberRepository tableMemberRepository;

    public InviteService(InviteRepository inviteRepository, UserRepository userRepository, GameTableRepository gameTableRepository, TableMemberRepository tableMemberRepository) {
        this.inviteRepository = inviteRepository;
        this.userRepository = userRepository;
        this.gameTableRepository = gameTableRepository;
        this.tableMemberRepository = tableMemberRepository;
    }

    public InviteResponse send(Long tableId, CreateInviteRequest request, User currentUser) {
        GameTable table = gameTableRepository.findById(tableId).orElseThrow(() -> new RuntimeException("Mesa não encontrada."));
        if (table.getMaster().getId() != currentUser.getId()) {
            throw new RuntimeException("Apenas o mestre da mesa pode enviar convites.");
        }

        User target = userRepository.findFirstByName(request.name());

        if (target == null) {
            throw new RuntimeException("Usuário alvo não encontrado.");
        }

        if (table.getMaster().getId() == target.getId()) {
            throw new RuntimeException("Não é possível enviar convites ao mestre da mesa.");
        }

        if (tableMemberRepository.findByTableAndUser(table, target).isPresent()) {
            throw new RuntimeException("O usuário ja faz parte dessa mesa.");
        }

        if (inviteRepository.findByTableAndTargetAndStatus(table, target, InviteStatus.PENDENTE).isPresent()) {
            throw new RuntimeException("O usuário ja possui um convite pendente.");
        }

        Invite invite = new Invite(table, currentUser, target, InviteStatus.PENDENTE);
        inviteRepository.save(invite);
        return InviteResponse.from(invite);
    }

    public List<InviteResponse> findPending(User currentUser) {
        return inviteRepository.findByTargetAndStatus(currentUser, InviteStatus.PENDENTE).
                stream().map(InviteResponse::from).
                toList();
    }

    public void updateInvite(Long inviteId, UpdateInviteRequestDto request, User currentUser) {
        Invite invite = inviteRepository.findByIdAndTarget(inviteId, currentUser)
                .orElseThrow(() -> new RuntimeException("Esse convite não pertence a esse usuário"));

        if (!Objects.equals(invite.getStatus(), InviteStatus.PENDENTE)) {
            inviteRepository.delete(invite);
            throw new RuntimeException("Esse convite não esta mais disponível.");
        }

        if (request.status() == InviteAction.ACEITO) {
            TableMember member = new TableMember(invite.getTable(), currentUser);
            tableMemberRepository.save(member);
        } else if (request.status() != InviteAction.RECUSADO) {
            throw new RuntimeException("Status inválido. Use ACEITO ou RECUSADO.");
        }

        inviteRepository.delete(invite);
    }
}
