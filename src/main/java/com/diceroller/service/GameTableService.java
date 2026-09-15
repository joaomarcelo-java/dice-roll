package com.diceroller.service;

import com.diceroller.domain.game.GameTable;
import com.diceroller.domain.user.User;
import com.diceroller.dto.config.table_config.ConfigDto;
import com.diceroller.dto.config.table_config.ItemTemplate;
import com.diceroller.dto.request.CreateTableItemRequest;
import com.diceroller.dto.request.CreateTableRequestDto;
import com.diceroller.dto.response.MemberRole;
import com.diceroller.dto.response.TableResponseDto;
import com.diceroller.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class GameTableService {
    private final GameTableRepository gameTableRepository;
    private final TableMemberRepository tableMemberRepository;
    private final CharacterRepository characterRepository;
    private final NpcRepository npcRepository;
    private final InviteRepository inviteRepository;
    private final ObjectMapper objectMapper;


    public GameTableService(GameTableRepository gameTableRepository,
                            TableMemberRepository tableMemberRepository,
                            CharacterRepository characterRepository,
                            NpcRepository npcRepository,
                            InviteRepository inviteRepository,
                            ObjectMapper objectMapper) {
        this.gameTableRepository = gameTableRepository;
        this.tableMemberRepository = tableMemberRepository;
        this.objectMapper = objectMapper;
        this.characterRepository = characterRepository;
        this.npcRepository = npcRepository;
        this.inviteRepository = inviteRepository;
    }

    public TableResponseDto create(CreateTableRequestDto request, User master) {
        GameTable table = new GameTable(request.name(), request.description(), master);
        table = gameTableRepository.save(table);
        return TableResponseDto.from(table, MemberRole.MESTRE, 1L);
    }

    public List<TableResponseDto> findAllByUser(User user) {
        List<TableResponseDto> result = new ArrayList<>();

        for (GameTable t : gameTableRepository.findByMaster(user)) {
            result.add(TableResponseDto.from(t, MemberRole.MESTRE, countMembers(t)));
        }
        for (GameTable t : gameTableRepository.findByPlayer(user)) {
            if (t.getMaster().getId() != user.getId()) {
                result.add(TableResponseDto.from(t, MemberRole.PLAYER, countMembers(t)));
            }
        }
        return result;
    }

    public TableResponseDto findById(Long id, User user) {
        GameTable table = gameTableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa não encontrada"));

        boolean isMaster = table.getMaster().getId() == user.getId();
        boolean isMember = tableMemberRepository.findByTableAndUser(table, user).isPresent();
        MemberRole role = isMaster ? MemberRole.MESTRE : (isMember ? MemberRole.PLAYER : MemberRole.ESPECTADOR);

        return TableResponseDto.from(table, role, countMembers(table));
    }

    public TableResponseDto update(Long id, CreateTableRequestDto request, User user) {
        GameTable table = gameTableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa não encontrada!"));
        if (table.getMaster().getId() != user.getId()) {
            throw new RuntimeException("Apenas o Mestre pode editar a mesa.");
        }
        table.setName(request.name());
        table.setDescription(request.description());
        table = gameTableRepository.save(table);
        return TableResponseDto.from(table, MemberRole.MESTRE, countMembers(table));
    }

    @Transactional
    public void delete(Long id, User user) {
        GameTable table = gameTableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa não encontrada"));

        if (table.getMaster().getId() != user.getId()) {
            throw new RuntimeException("Apenas o Mestre pode deletar a mesa!");
        }

        npcRepository.deleteByTable(table);
        characterRepository.deleteByTable(table);
        tableMemberRepository.deleteByTable(table);
        inviteRepository.deleteByTable(table);
        gameTableRepository.delete(table);
    }

    public ConfigDto setConfig(Long tableId, ConfigDto config, User currentUser) {
        GameTable table = gameTableRepository.findById(tableId).orElseThrow(() -> new RuntimeException(("Mesa não encontrada.")));

        if (!(table.getMaster().getId() == currentUser.getId())) {
            throw new RuntimeException("Apenas o mestre pode definir a ficha da mesa!");
        }

        try {
            String json = objectMapper.writeValueAsString(config);
            table.setCharacterConfig(json);
        } catch (Exception e) {
            throw new RuntimeException("Erro na desserialização da ficha para JSON.");
        }
        gameTableRepository.save(table);
        return config;
    }

    public ConfigDto getConfig(Long tableId, User currentUser) {
        GameTable table = gameTableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Mesa não encontrada"));

        boolean isMaster = table.getMaster().getId() == currentUser.getId();
        boolean isMember = tableMemberRepository.findByTableAndUser(table, currentUser).isPresent();
        if (!isMaster && !isMember)
            throw new RuntimeException("Você não tem acesso à configuração desta mesa.");

        if (table.getCharacterConfig() == null)
            throw new RuntimeException("Configuração não definida");
        try {
            return objectMapper.readValue(table.getCharacterConfig(), ConfigDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao serializar a ficha para JSON.");
        }
    }

    public List<ItemTemplate> listItems(Long tableId, User currentUser) {
        GameTable table = gameTableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Mesa não encontrada."));

        if(table.getMaster().getId() != currentUser.getId()){
            throw new RuntimeException("Apenas o mestre pode listar os itens da mesa.");
        }

        try {
            if (table.getInventoryData() == null) return new ArrayList<>();
            return objectMapper.readValue(table.getInventoryData(), new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Erro ao listar itens da mesa.");
        }
    }


    public ItemTemplate addItem(Long tableId, CreateTableItemRequest request, User currentUser) {
        GameTable table = gameTableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Mesa não encontrada."));

        if (table.getMaster().getId() != currentUser.getId())
            throw new RuntimeException("Apenas o mestre pode gerenciar itens da mesa.");

        try {
            List<ItemTemplate> items = table.getInventoryData() != null
                    ? objectMapper.readValue(table.getInventoryData(), new TypeReference<>() {
            })
                    : new ArrayList<>();

            boolean jaExiste = items.stream().anyMatch(i -> i.nome().equals(request.nome()));
            if (jaExiste) {
                throw new RuntimeException("Já existe um item com esse nome");
            }

            // Gera id sequencial baseado nos itens existentes
            Long novoId = items.isEmpty()
                    ? 1L
                    : items.stream().mapToLong(ItemTemplate::id).max().orElse(0L) + 1;

            ItemTemplate novo = new ItemTemplate(
                    request.nome(),
                    request.tipo(),
                    request.causaDano(),
                    request.causaCura(),
                    request.dado(),
                    request.descricao(),
                    request.usos(),
                    novoId);

            items.add(novo);
            table.setInventoryData(objectMapper.writeValueAsString(items));
            gameTableRepository.save(table);
            return novo;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao adicionar item na mesa.");

        }
    }

    public void removeItem(Long tableId, Long itemId, User currentUser) {
        GameTable table = gameTableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Mesa não encontrada."));

        if (table.getMaster().getId() != currentUser.getId())
            throw new RuntimeException("Apenas o mestre pode gerenciar itens da mesa.");

        try {
            List<ItemTemplate> items = table.getInventoryData() != null
                    ? objectMapper.readValue(table.getInventoryData(), new TypeReference<>() {})
                    : new ArrayList<>();

            boolean removeu = items.removeIf(i -> itemId.equals(i.id()));
            if (!removeu)
                throw new RuntimeException("Item com id " + itemId + " não encontrado na mesa.");

            table.setInventoryData(items.isEmpty() ? null : objectMapper.writeValueAsString(items));
            gameTableRepository.save(table);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao remover item da mesa.");
        }
    }
    private long countMembers(GameTable table) {
        return tableMemberRepository.findByTable(table).size() + 1L;
    }
}
