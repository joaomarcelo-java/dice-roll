package com.diceroller.service;

import com.diceroller.domain.game.GameTable;
import com.diceroller.domain.game.Npc;
import com.diceroller.domain.user.User;
import com.diceroller.dto.request.CreateNpcRequest;
import com.diceroller.dto.request.UpdatePvRequest;
import com.diceroller.dto.response.NpcResponse;
import com.diceroller.repository.GameTableRepository;
import com.diceroller.repository.NpcRepository;
import com.diceroller.repository.TableMemberRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class NpcService {
    private final NpcRepository npcRepository;
    private final GameTableRepository gameTableRepository;
    private final ObjectMapper objectMapper;
    private final TableMemberRepository tableMemberRepository;

    public NpcService(NpcRepository npcRepository, GameTableRepository gameTableRepository, ObjectMapper objectMapper,  TableMemberRepository tableMemberRepository) {
        this.npcRepository = npcRepository;
        this.gameTableRepository = gameTableRepository;
        this.objectMapper = objectMapper;
        this.tableMemberRepository = tableMemberRepository;
    }

    public NpcResponse create(Long tableId, CreateNpcRequest request, User currentUser) {
        GameTable table = gameTableRepository.findById(tableId).orElseThrow(() -> new RuntimeException("Mesa não encontrada."));
        if(table.getMaster().getId() != currentUser.getId()){
            throw new RuntimeException("Apenas o mestre pode criar NPC's dentro da mesa");
        }

        try {
            String atributosJson = objectMapper.writeValueAsString(request.atributos());
            String acoesJson = objectMapper.writeValueAsString(request.acoes());
            Npc npc = new Npc(table, request.name(), request.pv(), request.ca(), atributosJson, acoesJson);
            npcRepository.save(npc);
            return NpcResponse.from(npc);

        } catch (Exception e){
            throw new RuntimeException("Erro ao serializar dados JSON na criação do npc.");
        }
    }

    public List<NpcResponse> findAllByTable(Long tableId, User currentUser) {
        GameTable table = gameTableRepository.findById(tableId).orElseThrow(() -> new RuntimeException("Mesa não encontrada."));
        boolean isMaster = table.getMaster().getId() == currentUser.getId();
        boolean isMember = tableMemberRepository.findByTableAndUser(table, currentUser).isPresent();

        if(!isMaster && !isMember){
            throw new RuntimeException("Apenas membros da mesa podem listar os NPC's da mesa.");
        }

        return npcRepository.findByTable(table).stream().map(npc -> {
            try{
                return NpcResponse.from(npc);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Erro ao desserializar lista de npcs.");
            }
        }).toList();
    }

    public NpcResponse findById(Long tableId, Long npcId, User currentUser){
        GameTable table = gameTableRepository.findById(tableId).orElseThrow(() -> new RuntimeException("Mesa não encontrada."));
        Npc npc = npcRepository.findById(npcId).orElseThrow(() -> new RuntimeException("NPC não encontrado."));
        if(!(Objects.equals(npc.getTable().getId(), table.getId()))){
            throw new RuntimeException("NPC não encontrado.");
        }

        boolean isMaster = table.getMaster().getId() == currentUser.getId();
        boolean isMember = tableMemberRepository.findByTableAndUser(table, currentUser).isPresent();

        if(!isMaster && !isMember){
            throw new RuntimeException("Apenas membros ou o mestre da mesa podem ver este NPC");
        }

        try{
            return NpcResponse.from(npc);
        }catch (Exception e){
            throw new RuntimeException("Erro ao desserializar lista de npcs.");
        }
    }

    public NpcResponse update(Long tableId, Long npcId, CreateNpcRequest request, User currentUser) {
        GameTable table = gameTableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Mesa não encontrada"));

        if (table.getMaster().getId() != currentUser.getId())
            throw new RuntimeException("Apenas o mestre pode editar NPCs");

        Npc npc = npcRepository.findById(npcId)
                .orElseThrow(() -> new RuntimeException("NPC não encontrado"));

        if (!Objects.equals(npc.getTable().getId(), tableId))
            throw new RuntimeException("NPC não encontrado");

        try {
            npc.setName(request.name());
            npc.setPv(request.pv());
            npc.setCa(request.ca());
            npc.setAtributos(objectMapper.writeValueAsString(request.atributos()));
            npc.setAcoes(objectMapper.writeValueAsString(request.acoes()));

            npcRepository.save(npc);
            return NpcResponse.from(npc);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao serializar dados do NPC.");
        }
    }

    public void delete(Long tableId, Long npcId, User currentUser){
        GameTable table = gameTableRepository.findById(tableId).orElseThrow(()-> new RuntimeException("Mesa não encontrada."));
        Npc npc = npcRepository.findById(npcId).orElseThrow(() -> new RuntimeException("NPC não encontrado."));

        if(!(Objects.equals(npc.getTable().getId(), table.getId()))){
            throw new RuntimeException("NPC não encontrado.");
        }

        boolean isMaster = table.getMaster().getId() == currentUser.getId();

        if(!isMaster){
            throw new RuntimeException("Apenas o mestre pode deletar NPC's da mesa.");
        }

        npcRepository.delete(npc);
    }

    public NpcResponse updatePv(Long tableId, Long npcId, UpdatePvRequest request, User currentUser){
        GameTable table = gameTableRepository.findById(tableId).orElseThrow(()-> new RuntimeException("Mesa não encontrada."));
        Npc npc = npcRepository.findById(npcId).orElseThrow(() -> new RuntimeException("NPC não encontrado."));

        if(!(Objects.equals(npc.getTable().getId(), table.getId()))){
            throw new RuntimeException("NPC não encontrado.");
        }

        boolean isMaster = table.getMaster().getId() == currentUser.getId();

        if(!isMaster){
            throw new RuntimeException("Apenas o mestre pode editar NPC's da mesa.");
        }

        if(request.dano() != null){
            npc.setPv(npc.getPv() - request.dano());
        }

        if(request.cura() != null){
            npc.setPv(npc.getPv() + request.cura());
        }

        npcRepository.save(npc);
        try{
            return NpcResponse.from(npc);
        }catch (Exception e){
            throw new RuntimeException("Erro ao desserializar lista de npcs.");
        }
    }
}