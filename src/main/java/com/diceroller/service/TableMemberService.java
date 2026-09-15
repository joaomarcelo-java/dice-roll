package com.diceroller.service;

import com.diceroller.domain.game.GameTable;
import com.diceroller.domain.game.TableMember;
import com.diceroller.domain.user.User;
import com.diceroller.dto.request.AddMemberRequest;
import com.diceroller.dto.response.MemberResponseDto;
import com.diceroller.repository.GameTableRepository;
import com.diceroller.repository.TableMemberRepository;
import com.diceroller.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.diceroller.repository.CharacterRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TableMemberService {
    private final TableMemberRepository tableMemberRepository;
    private final UserRepository userRepository;
    private final GameTableRepository gameTableRepository;
    private final CharacterRepository characterRepository;

    public TableMemberService(TableMemberRepository tableMemberRepository,
                              UserRepository userRepository,
                              GameTableRepository gameTableRepository,
                              CharacterRepository characterRepository)
    {
        this.gameTableRepository = gameTableRepository;
        this.tableMemberRepository = tableMemberRepository;
        this.userRepository = userRepository;
        this.characterRepository = characterRepository;
    }

    public MemberResponseDto addMember(Long tableId, AddMemberRequest request, User currentUser){

        GameTable gameTable = gameTableRepository.findById(tableId).orElseThrow(() -> new RuntimeException("Mesa não encotrada"));

        if(!(gameTable.getMaster().getId() == currentUser.getId())){
            throw new RuntimeException("Apenas mestres podem adicionar jogadores na mesa!");
        }

        User target = userRepository.findFirstByName(request.name());

        if(target == null){
            throw new RuntimeException("Usuário não encontrado.");
        }

        if(target.getId() == gameTable.getMaster().getId()){
            throw new RuntimeException("O mestre ja está na mesa!");
        }

        if(tableMemberRepository.findByTableAndUser(gameTable, target).isPresent()){
            throw new RuntimeException("O usuário já faz parte da mesa");
        }

        TableMember member = new TableMember(gameTable, target);

        tableMemberRepository.save(member);
        return MemberResponseDto.fromPlayer(member);
    }

    public List<MemberResponseDto> findAllByTable(Long tableId, User currentUser){
        GameTable gameTable = gameTableRepository.findById(tableId).orElseThrow(() -> new RuntimeException("Mesa não encotrada"));

        List<MemberResponseDto> memberResponses = new ArrayList<>();
        memberResponses.add(MemberResponseDto.fromMaster(gameTable.getMaster()));

        List<TableMember> players = tableMemberRepository.findByTable(gameTable);
        for(TableMember p: players){
            memberResponses.add(MemberResponseDto.fromPlayer(p));
        }

        return memberResponses;
    }

    @Transactional
    public void leave(Long tableId, User currentUser){
        GameTable gameTable = gameTableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Mesa não encotrada"));

        if (gameTable.getMaster().getId() == currentUser.getId())
            throw new RuntimeException("O mestre não pode sair da mesa!");

        TableMember tableMember = tableMemberRepository
                .findByTableAndUser(gameTable, currentUser)
                .orElseThrow(() -> new RuntimeException("O jogador não pertence a essa mesa."));

        characterRepository.deleteByTableAndPlayer(gameTable, currentUser);
        tableMemberRepository.delete(tableMember);
    }

    @Transactional
    public void kick(Long tableId, Long userId, User currentUser){
        GameTable gameTable = gameTableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Mesa não encontrada"));

        if (gameTable.getMaster().getId() != currentUser.getId())
            throw new RuntimeException("Apenas o mestre pode kickar players da mesa!");

        User target = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (gameTable.getMaster().getId() == target.getId())
            throw new RuntimeException("O mestre não pode ser kickado da mesa!");

        TableMember member = tableMemberRepository
                .findByTableAndUser(gameTable, target)
                .orElseThrow(() -> new RuntimeException("Mesa ou jogador não encontrados!"));

        characterRepository.deleteByTableAndPlayer(gameTable, target);
        tableMemberRepository.delete(member);
    }

}
