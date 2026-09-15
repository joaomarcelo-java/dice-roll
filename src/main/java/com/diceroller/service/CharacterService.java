package com.diceroller.service;

import com.diceroller.domain.game.Character;
import com.diceroller.domain.game.GameTable;
import com.diceroller.domain.user.User;
import com.diceroller.domain.dice.DiceResult;
import com.diceroller.domain.dice.DiceRoll;
import com.diceroller.dto.config.table_config.*;
import com.diceroller.dto.request.CreateCharacterRequestDto;
import com.diceroller.dto.request.EvolveRequest;
import com.diceroller.dto.request.ItemActionRequest;
import com.diceroller.dto.request.RollLinkRequest;
import com.diceroller.dto.response.CharacterResponseDto;
import com.diceroller.dto.response.InventoryItem;
import com.diceroller.dto.response.RollLinkResponse;
import com.diceroller.repository.CharacterRepository;
import com.diceroller.repository.GameTableRepository;
import com.diceroller.repository.TableMemberRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

@Service
public class CharacterService {
    private final CharacterRepository characterRepository;
    private final GameTableRepository gameTableRepository;
    private final TableMemberRepository tableMemberRepository;
    private final ObjectMapper objectMapper;
    private final DiceRollerService diceRollerService;
    private final Random random = new Random();

    public CharacterService(CharacterRepository characterRepository, GameTableRepository gameTableRepository, TableMemberRepository tableMemberRepository, ObjectMapper objectMapper, DiceRollerService diceRollerService) {
        this.characterRepository = characterRepository;
        this.gameTableRepository = gameTableRepository;
        this.tableMemberRepository = tableMemberRepository;
        this.objectMapper = objectMapper;
        this.diceRollerService = diceRollerService;
    }

    public CharacterResponseDto create(Long tableId, CreateCharacterRequestDto request, User currentUser) {
        GameTable table = gameTableRepository.findById(tableId).orElseThrow(() -> new RuntimeException("Mesa não encontrada."));

        if (table.getCharacterConfig() == null) {
            throw new RuntimeException("A mesa ainda não possui uma configuração de ficha.");
        }

        boolean isMaster = table.getMaster().getId() == currentUser.getId();
        boolean isMember = tableMemberRepository.findByTableAndUser(table, currentUser).isPresent();

        if (!isMaster && !isMember) {
            throw new RuntimeException("Você não é membro desta mesa.");
        }
        if (characterRepository.findByTableAndPlayer(table, currentUser).isPresent()) {
            throw new RuntimeException("Você já possui um personagem nessa mesa.");
        }

        try {
            ConfigDto config = objectMapper.readValue(table.getCharacterConfig(), ConfigDto.class);

            Map<String, Map<String, Object>> dataFinal = new LinkedHashMap<>();

            for (CategoriaConfig cat : config.categorias()) {
                Map<String, Object> dadosCategoria = new LinkedHashMap<>();
                Map<String, Object> catEnviada = request.data().get(cat.nome());

                validarCategoria(cat, catEnviada, config);

                boolean ehDadoCriacao = cat.metodo() == CategoriaMetodo.dado
                        && cat.dadoCriacao() != null
                        && !cat.dadoCriacao().isBlank();

                if (cat.metodo() == CategoriaMetodo.pontos) {
                    processarCategoriaPontos(cat, catEnviada, dadosCategoria, config, request, new HashMap<>());
                } else if (ehDadoCriacao) {
                    processarCategoriaDado(cat, dadosCategoria, config, request);
                } else {
                    processarCategoriaLivre(cat, catEnviada, dadosCategoria, config, request);
                }

                dataFinal.put(cat.nome(), dadosCategoria);
            }

            String dataJson = objectMapper.writeValueAsString(dataFinal);

            Character character = new Character(request.characterName(), dataJson, currentUser, table);
            character = characterRepository.save(character);

            return CharacterResponseDto.from(character);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar os dados do personagem.");
        }
    }

    private void processarCategoriaPontos(CategoriaConfig cat, Map<String, Object> catEnviada, Map<String, Object> dadosCategoria, ConfigDto config, CreateCharacterRequestDto request, Map<String, Integer> evolutionData) {
        if (catEnviada == null) return;
        int soma = 0;
        for (Map.Entry<String, Object> entry : catEnviada.entrySet()) {
            int valor = ((Number) entry.getValue()).intValue();
            if (valor < 0) {
                throw new RuntimeException(entry.getKey() + " não pode ser negativo.");
            }
            soma += valor;
        }

        int limite = cat.pontosDisponiveis() != null ? cat.pontosDisponiveis() : Integer.MAX_VALUE;
        if (evolutionData != null && evolutionData.containsKey(cat.nome())) {
            limite += evolutionData.get(cat.nome());
        }

        if (soma > limite) {
            throw new RuntimeException("A soma dos pontos em " + cat.nome() + " excede o limite de " + limite + ".");
        }

        for (Map.Entry<String, Object> entry : catEnviada.entrySet()) {
            int valorBase = ((Number) entry.getValue()).intValue();
            int modificador = calcularModificadorRegras(entry.getKey(), config, request);
            int valorFinal = valorBase + modificador;
            dadosCategoria.put(entry.getKey(), valorFinal);
        }
    }

    private void processarCategoriaDado(CategoriaConfig cat, Map<String, Object> dadosCategoria, ConfigDto config, CreateCharacterRequestDto request) {
        for (var campo : cat.campos()) {
            if (campo.tipo() == CampoTipo.numero) {
                int valorRolado = rolarDado(cat.dadoCriacao());
                int modificador = calcularModificadorRegras(campo.nome(), config, request);
                dadosCategoria.put(campo.nome(), valorRolado + modificador);
            }
        }
    }

    private void processarCategoriaLivre(CategoriaConfig cat, Map<String, Object> catEnviada, Map<String, Object> dadosCategoria, ConfigDto config, CreateCharacterRequestDto request) {
        if (catEnviada == null) return;

        for (Map.Entry<String, Object> entry : catEnviada.entrySet()) {
            Object valor = entry.getValue();

            if (valor instanceof Number num) {
                int valorInt = num.intValue();
                if (valorInt < 0) {
                    throw new RuntimeException(entry.getKey() + " não pode ser negativo.");
                }
                int modificador = calcularModificadorRegras(entry.getKey(), config, request);
                dadosCategoria.put(entry.getKey(), valorInt + modificador);
            } else {
                dadosCategoria.put(entry.getKey(), valor);
            }
        }
    }

    private void validarCategoria(CategoriaConfig cat, Map<String, Object> catEnviada, ConfigDto config) {
        if (catEnviada == null) return;

        switch (cat.tipo()) {
            case CategoriaTipo.identificacao -> {
                for (var entry : catEnviada.entrySet()) {
                    CampoConfig campo = cat.campos().stream()
                            .filter(c -> c.nome().equals(entry.getKey()))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException(
                                    "Campo '" + entry.getKey() + "' não existe em " + cat.nome()));

                    if (campo.tipo() == CampoTipo.texto) {
                        if (entry.getValue() instanceof Number) {
                            throw new RuntimeException(entry.getKey() + " deve ser texto em " + cat.nome());
                        }
                        if (campo.opcoes() != null && !campo.opcoes().contains(entry.getValue())) {
                            throw new RuntimeException(entry.getKey() + " em " + cat.nome()
                                    + " deve ser: " + campo.opcoes());
                        }
                    } else if (campo.tipo() == CampoTipo.numero) {
                        if (!(entry.getValue() instanceof Number)) {
                            throw new RuntimeException(entry.getKey() + " deve ser um número em " + cat.nome());
                        }
                    }
                }
            }
            case CategoriaTipo.atributos, CategoriaTipo.pericias, CategoriaTipo.combate -> {
                for (var entry : catEnviada.entrySet()) {
                    CampoConfig campo = cat.campos().stream()
                            .filter(c -> c.nome().equals(entry.getKey()))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException(
                                    "Campo '" + entry.getKey() + "' não existe em " + cat.nome()));

                    if (!(entry.getValue() instanceof Number num)) {
                        throw new RuntimeException(entry.getKey() + " em " + cat.nome() + " deve ser um número.");
                    }
                    int valor = num.intValue();
                    if (valor < 0) {
                        throw new RuntimeException(entry.getKey() + " em " + cat.nome() + " não pode ser negativo.");
                    }
                    if (campo.min() != null && valor < campo.min()) {
                        throw new RuntimeException(entry.getKey() + " em " + cat.nome() + " mínimo é " + campo.min() + ".");
                    }
                    if (campo.max() != null && valor > campo.max()) {
                        throw new RuntimeException(entry.getKey() + " em " + cat.nome() + " máximo é " + campo.max() + ".");
                    }

                    if (!(entry.getValue() instanceof Number)) {
                        throw new RuntimeException(entry.getKey() + " em " + cat.nome()
                                + " deve ser um número.");
                    }
                    if (((Number) entry.getValue()).intValue() < 0) {
                        throw new RuntimeException(entry.getKey() + " em " + cat.nome() + " não pode ser negativo.");
                    }
                }
            }
            case CategoriaTipo.custom -> {
            }
            default -> throw new RuntimeException("Tipo de categoria desconhecido: " + cat.tipo());
        }
    }

    private int calcularModificadorRegras(String campoAlvo, ConfigDto config, CreateCharacterRequestDto request) {
        int total = 0;
        for (RegraConfig regra : config.regras()) {
            for (CategoriaConfig cat : config.categorias()) {
                String campoCondicao = regra.condicao().campo();
                boolean campoEncontrado = cat.campos().stream().anyMatch(c -> c.nome().equals(campoCondicao));

                if (campoEncontrado) {
                    Map<String, Object> catData = request.data().get(cat.nome());
                    if (catData != null) {
                        Object valorEscolhido = catData.get(campoCondicao);
                        if (regra.condicao().valor().equals(valorEscolhido)) {
                            for (Efeito efeito : regra.efeitos()) {
                                if (efeito.alvo().equals(campoAlvo)) {
                                    total += efeito.modificador();
                                }
                            }
                        }
                    }
                }
            }
        }
        return total;
    }

    private int rolarDado(String notacaoDado) {
        if (notacaoDado == null || notacaoDado.isBlank()) return 0;

        String[] partes = notacaoDado.split("d");
        int quantidade;
        int faces;

        try {
            if (partes.length == 1) {
                quantidade = 1;
                faces = Integer.parseInt(partes[0]);
            } else {
                quantidade = Integer.parseInt(partes[0]);
                faces = Integer.parseInt(partes[1]);
            }
        } catch (NumberFormatException e) {
            return 0;
        }

        int total = 0;
        for (int i = 0; i < quantidade; i++) {
            total += random.nextInt(faces) + 1;
        }
        return total;
    }

    public List<CharacterResponseDto> findAllByTable(Long tableId, User currentUser) {
        GameTable table = gameTableRepository.findById(tableId).orElseThrow(() -> new RuntimeException("Mesa não encontrada!"));

        boolean isMaster = table.getMaster().getId() == currentUser.getId();
        boolean isMember = tableMemberRepository.findByTableAndUser(table, currentUser).isPresent();

        if (!isMaster && !isMember) {
            throw new RuntimeException("Você não é membro da mesa.");
        }

        if (isMaster) {
            return characterRepository.findByTable(table).stream()
                    .map(CharacterResponseDto::from).toList();
        }

        return characterRepository.findByTable(table).stream()
                .map(CharacterResponseDto::withoutInventory).toList();
    }

    public CharacterResponseDto findById(Long tableId, Long charId, User currentUser) {
        GameTable table = gameTableRepository.findById(tableId).orElseThrow(() -> new RuntimeException("Mesa não encontrada!"));
        Character character = characterRepository.findById(charId).orElseThrow(() -> new RuntimeException("Personagem não encontrado."));

        if (!Objects.equals(character.getTable().getId(), tableId)) {
            throw new RuntimeException("Esse personagem não pertence a essa mesa!");
        }

        boolean isMaster = table.getMaster().getId() == currentUser.getId();
        boolean isOwner = character.getPlayer().getId() == currentUser.getId();

        if (!isMaster && !isOwner) {
            throw new RuntimeException("Você não tem permissão para ver esse personagem");
        }

        return CharacterResponseDto.from(character);
    }

    private void processarCategoriaDadoUpdate(CategoriaConfig cat, Map<String, Object> dadosCategoria, Map<String, Object> dadosAntigos, ConfigDto config, CreateCharacterRequestDto request) {
        for (var campo : cat.campos()) {
            if (campo.tipo() == CampoTipo.numero) {
                Object valorExistente = dadosAntigos.get(campo.nome());
                int valorBase = valorExistente instanceof Number num ? num.intValue() : 0;
                int modificador = calcularModificadorRegras(campo.nome(), config, request);
                dadosCategoria.put(campo.nome(), valorBase + modificador);
            }
        }
    }

    public CharacterResponseDto update(Long tableId, Long charId, CreateCharacterRequestDto request, User currentUser) {
        GameTable table = gameTableRepository.findById(tableId).orElseThrow(() -> new RuntimeException("Mesa não encontrada."));
        Character character = characterRepository.findById(charId).orElseThrow(() -> new RuntimeException("Personagem não encontrado."));

        if (!Objects.equals(character.getTable().getId(), tableId)) {
            throw new RuntimeException("Esse personagem não pertence a essa mesa.");
        }

        boolean isMaster = table.getMaster().getId() == currentUser.getId();
        boolean isOwner = character.getPlayer().getId() == currentUser.getId();

        if (!isMaster && !isOwner) {
            throw new RuntimeException("Você não pode fazer alterações nesse personagem.");
        }

        try {
            ConfigDto config = objectMapper.readValue(table.getCharacterConfig(), ConfigDto.class);

            Map<String, Map<String, Object>> dadosAntigos = objectMapper.readValue(character.getData(), LinkedHashMap.class);

            Map<String, Integer> evolutionData = character.getEvolutionData() != null ? objectMapper.readValue(character.getEvolutionData(), LinkedHashMap.class) : new HashMap<>();

            Map<String, Map<String, Object>> dataFinal = new LinkedHashMap<>();

            for (CategoriaConfig cat : config.categorias()) {
                Map<String, Object> dadosCategoria = new LinkedHashMap<>();
                Map<String, Object> catEnviada = request.data().get(cat.nome());
                Map<String, Object> catAntiga = dadosAntigos.get(cat.nome());

                validarCategoria(cat, catEnviada, config);

                boolean ehDadoCriacao = cat.metodo() == CategoriaMetodo.dado
                        && cat.dadoCriacao() != null
                        && !cat.dadoCriacao().isBlank();

                if (cat.metodo() == CategoriaMetodo.pontos) {
                    processarCategoriaPontos(cat, catEnviada, dadosCategoria, config, request, evolutionData);
                } else if (ehDadoCriacao) {
                    processarCategoriaDadoUpdate(cat, dadosCategoria, catAntiga, config, request);
                } else {
                    processarCategoriaLivre(cat, catEnviada, dadosCategoria, config, request);
                }

                dataFinal.put(cat.nome(), dadosCategoria);
            }

            // Consumir pontos de evolução
            for (CategoriaConfig cat : config.categorias()) {
                if (!evolutionData.containsKey(cat.nome())) continue;

                Map<String, Object> catAntiga = dadosAntigos.get(cat.nome());
                Map<String, Object> catNova = dataFinal.get(cat.nome());

                int oldSum = somarValores(catAntiga);
                int newSum = somarValores(catNova);
                int diff = newSum - oldSum;

                if (diff > 0) {
                    int restante = evolutionData.get(cat.nome()) - diff;
                    if (restante <= 0) {
                        evolutionData.remove(cat.nome());
                    } else {
                        evolutionData.put(cat.nome(), restante);
                    }
                }
            }

            String dataJson = objectMapper.writeValueAsString(dataFinal);

            character.setCharacterName(request.characterName());
            character.setData(dataJson);
            character.setEvolutionData(evolutionData.isEmpty() ? null : objectMapper.writeValueAsString(evolutionData));
            characterRepository.save(character);

            return CharacterResponseDto.from(character);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar os dados do personagem.");
        }
    }

    private int somarValores(Map<String, Object> categoria) {
        if (categoria == null) return 0;
        int soma = 0;
        for (Object valor : categoria.values()) {
            if (valor instanceof Number num) {
                soma += num.intValue();
            }
        }
        return soma;
    }

    public void evolve(Long tableId, Long charId, EvolveRequest request, User currentUser) {
        GameTable table = gameTableRepository.findById(tableId).orElseThrow(() -> new RuntimeException("Mesa não encontrada."));
        Character character = characterRepository.findById(charId).orElseThrow(() -> new RuntimeException("Personagem não encontrado."));

        if (!Objects.equals(character.getTable().getId(), tableId)) {
            throw new RuntimeException("Personagem não pertence a essa mesa.");
        }

        if (table.getMaster().getId() != currentUser.getId()) {
            throw new RuntimeException("Apenas o mestre pode evoluir personagens.");
        }

        try {
            Map<String, Integer> evolData = character.getEvolutionData() != null ? objectMapper.readValue(character.getEvolutionData(), LinkedHashMap.class) : new LinkedHashMap<>();

            evolData.put(request.categoria(), evolData.getOrDefault(request.categoria(), 0) + request.pontos());

            character.setEvolutionData(objectMapper.writeValueAsString(evolData));
            characterRepository.save(character);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar evolução do personagem.");
        }
    }

    public void delete(Long tableId, Long charId, User currentUser) {
        GameTable table = gameTableRepository.findById(tableId).orElseThrow(() -> new RuntimeException("Mesa não encontrada."));
        Character character = characterRepository.findById(charId).orElseThrow(() -> new RuntimeException("Personagem não encontrado."));

        if (!Objects.equals(character.getTable().getId(), tableId)) {
            throw new RuntimeException("Esse personagem não pertence a essa mesa.");
        }

        boolean isMaster = table.getMaster().getId() == currentUser.getId();
        boolean isOwner = character.getPlayer().getId() == currentUser.getId();

        if (!isMaster && !isOwner) {
            throw new RuntimeException("Você não pode deletar esse personagem.");
        }
        characterRepository.delete(character);
    }

    public void addItem(Long tableId, Long charId, ItemActionRequest request, User currentUser) {
        GameTable table = gameTableRepository.findById(tableId).orElseThrow(() -> new RuntimeException("Mesa não encontrada."));

        if (table.getMaster().getId() != currentUser.getId())
            throw new RuntimeException("Apenas o mestre pode gerenciar itens dos personagens.");

        Character character = characterRepository.findById(charId).orElseThrow(() -> new RuntimeException("Personagem não encontrado."));

        if (!Objects.equals(character.getTable().getId(), tableId))
            throw new RuntimeException("Personagem não pertence a esta mesa.");

        try {
            List<ItemTemplate> itensDaMesa = table.getInventoryData() != null ? objectMapper.readValue(table.getInventoryData(), new TypeReference<>() {
            }) : List.of();

            ItemTemplate modelo = itensDaMesa.stream().filter(i -> i.nome().equals(request.itemName())).findFirst().orElseThrow(() -> new RuntimeException("Item '" + request.itemName() + "' não encontrado na mesa."));

            Map<String, InventoryItem> inventory = character.getInventoryData() != null
                    ? objectMapper.readValue(character.getInventoryData(), new TypeReference<>() {})
                    : new LinkedHashMap<>();

            InventoryItem existente = inventory.get(request.itemName());

            if (existente != null) {
                inventory.put(request.itemName(), new InventoryItem(existente.nome(), existente.tipo(), existente.causaDano(), existente.causaCura(), existente.dado(), existente.descricao(), existente.quantidade() + request.quantidade(), existente.usos() == null ? null : existente.usos() + request.quantidade() * (modelo.usos() != null ? modelo.usos() : 1)));
            } else {
                inventory.put(request.itemName(),
                        new InventoryItem(modelo.nome(), modelo.tipo(), modelo.causaDano(),
                                modelo.causaCura(), modelo.dado(), modelo.descricao(),
                                request.quantidade(),
                                modelo.usos() == null ? null : modelo.usos() * request.quantidade()));
            }

            character.setInventoryData(objectMapper.writeValueAsString(inventory));
            characterRepository.save(character);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao adicionar item ao personagem.");
        }
    }

    public void removeItem(Long tableId, Long charId, ItemActionRequest request, User currentUser) {
        GameTable table = gameTableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Mesa não encontrada."));

        if (table.getMaster().getId() != currentUser.getId())
            throw new RuntimeException("Apenas o mestre pode gerenciar itens dos personagens.");

        Character character = characterRepository.findById(charId)
                .orElseThrow(() -> new RuntimeException("Personagem não encontrado."));

        if (!Objects.equals(character.getTable().getId(), tableId))
            throw new RuntimeException("Personagem não pertence a esta mesa.");

        try {
            Map<String, InventoryItem> inventory = character.getInventoryData() != null
                    ? objectMapper.readValue(character.getInventoryData(), new TypeReference<>() {
            })
                    : new LinkedHashMap<>();

            InventoryItem item = inventory.get(request.itemName());
            if (item == null)
                throw new RuntimeException("Item '" + request.itemName() + "' não encontrado no inventário.");

            if (item.quantidade() < request.quantidade())
                throw new RuntimeException("Quantidade insuficiente. Possui apenas " + item.quantidade()
                        + " de '" + request.itemName() + "'.");

            int novaQuantidade = item.quantidade() - request.quantidade();
            if (novaQuantidade <= 0) {
                inventory.remove(request.itemName());
            } else {
                inventory.put(request.itemName(),
                        new InventoryItem(item.nome(), item.tipo(), item.causaDano(),
                                item.causaCura(), item.dado(), item.descricao(),
                                novaQuantidade, item.usos()));
            }
            character.setInventoryData(inventory.isEmpty() ? null : objectMapper.writeValueAsString(inventory));
            characterRepository.save(character);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao remover item do personagem.");
        }
    }

    public CharacterResponseDto rollStats(Long tableId, Long charId, User currentUser) {
        GameTable table = gameTableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Mesa não encontrada."));
        Character character = characterRepository.findById(charId)
                .orElseThrow(() -> new RuntimeException("Personagem não encontrado."));

        boolean isMaster = table.getMaster().getId() == currentUser.getId();
        boolean isOwner = character.getPlayer().getId() == currentUser.getId();
        if (!isMaster && !isOwner)
            throw new RuntimeException("Você não tem permissão para rolar os atributos.");

        try {
            ConfigDto config = objectMapper.readValue(table.getCharacterConfig(), ConfigDto.class);
            Map<String, Map<String, Object>> dataAtual = objectMapper.readValue(
                    character.getData(), new TypeReference<>() {});
            Map<String, Map<String, Object>> dataFinal = new LinkedHashMap<>();

            for (CategoriaConfig cat : config.categorias()) {
                Map<String, Object> catAtual = dataAtual.getOrDefault(cat.nome(), new LinkedHashMap<>());
                Map<String, Object> catFinal = new LinkedHashMap<>();

                boolean podeRolar = cat.metodo() == CategoriaMetodo.dado
                        && cat.dadoCriacao() != null
                        && !cat.dadoCriacao().isBlank();

                if (podeRolar) {
                    for (CampoConfig campo : cat.campos()) {
                        if (campo.tipo() == CampoTipo.numero) {
                            // só rola se ainda não tem valor
                            if (catAtual.get(campo.nome()) == null) {
                                int rolado = rolarDado(cat.dadoCriacao());
                                int mod = calcularModificadorRegras(campo.nome(), config, dataAtual);
                                catFinal.put(campo.nome(), rolado + mod);
                            } else {
                                catFinal.put(campo.nome(), catAtual.get(campo.nome()));
                            }
                        } else if (catAtual.containsKey(campo.nome())) {
                            catFinal.put(campo.nome(), catAtual.get(campo.nome()));
                        }
                    }
                } else {
                    catFinal.putAll(catAtual);
                }
                dataFinal.put(cat.nome(), catFinal);
            }

            return CharacterResponseDto.fromData(character, dataFinal);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao rolar os atributos.");
        }
    }

    private int calcularModificadorRegras(String campoAlvo, ConfigDto config,
                                          Map<String, Map<String, Object>> data) {
        int total = 0;
        for (RegraConfig regra : config.regras()) {
            for (CategoriaConfig cat : config.categorias()) {
                String campoCondicao = regra.condicao().campo();
                boolean encontrado = cat.campos().stream()
                        .anyMatch(c -> c.nome().equals(campoCondicao));
                if (encontrado) {
                    Map<String, Object> catData = data.get(cat.nome());
                    if (catData != null) {
                        Object valor = catData.get(campoCondicao);
                        if (regra.condicao().valor().equals(valor)) {
                            for (Efeito efeito : regra.efeitos()) {
                                if (efeito.alvo().equals(campoAlvo)) {
                                    total += efeito.modificador();
                                }
                            }
                        }
                    }
                }
            }
        }
        return total;
    }
    public RollLinkResponse rollLinked(Long tableId, Long charId, RollLinkRequest request, User currentUser) {
        GameTable table = gameTableRepository.findById(tableId).orElseThrow(() -> new RuntimeException("Mesa não encontrada"));
        Character character = characterRepository.findById(charId).orElseThrow(() -> new RuntimeException("Ficha não encontrada."));
        boolean isMaster = table.getMaster().getId() == currentUser.getId();
        boolean isMember = tableMemberRepository.findByTableAndUser(table, currentUser).isPresent();

        if(!isMaster && !isMember) throw new RuntimeException("Você não é membro dessa mesa.");

        try {
            ConfigDto config = objectMapper.readValue(table.getCharacterConfig(), ConfigDto.class);

            CategoriaConfig cat = config.categorias().stream()
                    .filter(c -> c.nome().equals(request.categoria()))
                    .findFirst().orElseThrow(() -> new RuntimeException("Categoria não encontrada."));
            CampoConfig campo = cat.campos().stream()
                    .filter(c -> c.nome().equals(request.campo()))
                    .findFirst().orElseThrow(() -> new RuntimeException("Campo não encontrado."));

            ConfiguracaoRolagem cfg = campo.configuracaoRolagem() != null
                    ? campo.configuracaoRolagem()
                    : cat.configuracaoRolagem();
            if (cfg == null) throw new RuntimeException("Campo '" + request.campo() + "' não é rolável.");

            Map<String, Map<String, Object>> data = objectMapper.readValue(character.getData(), new TypeReference<>() {});

            // 5. Rolar dado (se tipo usa dado)
            List<DiceRoll> rolagens = new ArrayList<>();
            int total = 0;
            if ("dado".equals(cfg.tipo()) || "ambos".equals(cfg.tipo())) {
                DiceResult resultado = diceRollerService.roll(cfg.dado());
                rolagens = resultado.rolagens();
                total += resultado.total();
            }

            // 6. CampoSomado ("Atributos.Força")
            RollLinkResponse.CampoValor somado = null;
            if (cfg.campoSomado() != null && !cfg.campoSomado().isBlank()) {
                String[] partes = cfg.campoSomado().split("\\.");
                if (partes.length != 2) throw new RuntimeException("campoSomado inválido.");
                Map<String, Object> catSomada = data.get(partes[0]);
                if (catSomada == null || !catSomada.containsKey(partes[1]))
                    throw new RuntimeException("Campo somado '" + cfg.campoSomado() + "' não encontrado no personagem.");
                int valor = ((Number) catSomada.get(partes[1])).intValue();
                somado = new RollLinkResponse.CampoValor(partes[0], partes[1], valor);
                total += valor;
            }

            // 7. Somar valor do próprio campo
            Integer valorCampo = null;
            if (cfg.somarValorCampo()) {
                Map<String, Object> catData = data.get(request.categoria());
                if (catData != null && catData.containsKey(request.campo())) {
                    valorCampo = ((Number) catData.get(request.campo())).intValue();
                    total += valorCampo;
                }
            }

            // 8. Montar resposta
            return new RollLinkResponse(request.categoria(), request.campo(),
                    cfg.dado(), rolagens, somado, valorCampo, total);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao rolar o campo vinculado.");
        }
    }

}
