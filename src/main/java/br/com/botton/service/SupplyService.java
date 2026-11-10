package br.com.botton.service;

import br.com.botton.exception.BusinessRuleException;
import br.com.botton.exception.EntityNotFoundException;
import br.com.botton.model.AlertStatus;
import br.com.botton.model.Supply;
import br.com.botton.repository.SupplyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * ============================================================================
 * CONCEITO DE POO: CAMADA DE SERVIÇO (Service Layer Pattern)
 * ============================================================================
 * O SupplyService orquestra as regras de negócio relacionadas a insumos (Supply).
 *
 * Princípios aplicados:
 * 1. Separação de Preocupações (SoC): A regra de negócio e os cálculos não ficam no Controller
 *    nem no Repository, mas concentrados aqui.
 * 2. Injeção de Dependências (DI): Recebe a interface SupplyRepository pelo construtor,
 *    promovendo o desacoplamento e facilitando a escrita de testes unitários com mocks.
 * 3. Gerenciamento Transacional: Métodos que alteram dados utilizam @Transactional
 *    para assegurar a atomicidade das operações.
 */
@Service
public class SupplyService {

    private final SupplyRepository supplyRepository;

    /**
     * Injeção de dependência via construtor (recomendado em Spring/POO).
     */
    public SupplyService(SupplyRepository supplyRepository) {
        this.supplyRepository = Objects.requireNonNull(supplyRepository, "SupplyRepository é obrigatório");
    }

    /**
     * Cadastra um novo insumo após validar limites de estoque.
     */
    @Transactional
    public Long createSupply(Supply supply) {
        Objects.requireNonNull(supply, "Insumo não pode ser nulo.");
        supply.validarLimites();
        Supply saved = supplyRepository.save(supply);
        return saved.getId();
    }

    /**
     * Atualiza as informações cadastrais e limites de um insumo existente.
     */
    @Transactional
    public void updateSupply(Long id, Supply updatedData) {
        Objects.requireNonNull(updatedData, "Dados de atualização não podem ser nulos.");
        updatedData.setId(id);
        updatedData.validarLimites();

        boolean updated = supplyRepository.update(updatedData);
        if (!updated) {
            throw new EntityNotFoundException("Insumo não encontrado.");
        }
    }

    /**
     * Realiza uma movimentação manual de estoque (+ entrada, - saída).
     */
    @Transactional
    public void moveStock(Long id, long amount) {
        if (amount == 0) {
            throw new BusinessRuleException("Informe uma quantidade diferente de zero.");
        }
        boolean success = supplyRepository.updateStock(id, amount);
        if (!success) {
            throw new BusinessRuleException("Insumo não encontrado ou saldo inválido.");
        }
    }

    /**
     * Retorna a lista de insumos com seus dados computados (reservas, saldo disponível, alertas).
     * Usado pelo dashboard do ateliê.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSuppliesWithCalculatedState() {
        List<Supply> supplies = supplyRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Supply s : supplies) {
            long reserved = supplyRepository.findReservedQuantityBySupplyId(s.getId());
            long available = s.calcularDisponivel(reserved);
            AlertStatus alert = s.determinarAlerta(reserved);
            long replenish = s.calcularReposicao(reserved);

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", s.getId());
            map.put("name", s.getName());
            map.put("unit", s.getUnit());
            map.put("stock", s.getStock());
            map.put("minimum", s.getMinimum());
            map.put("maximum", s.getMaximum());
            map.put("reserved", reserved);
            map.put("available", available);
            map.put("alert", alert.name());
            map.put("replenish", replenish);

            result.add(map);
        }

        return result;
    }

    /**
     * Verifica se um insumo existe pelo ID.
     */
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return supplyRepository.existsById(id);
    }

    /**
     * Bloqueia os registros de insumos no banco de dados para concorrência segura.
     */
    public void lockSupplies() {
        supplyRepository.lockAllSupplies();
    }
}
