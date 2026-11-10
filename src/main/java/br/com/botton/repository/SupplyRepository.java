package br.com.botton.repository;

import br.com.botton.model.Supply;
import java.util.List;
import java.util.Optional;

/**
 * ============================================================================
 * CONCEITO DE POO: ABSTRAÇÃO E INTERFACES (Repository Pattern)
 * ============================================================================
 * A interface SupplyRepository define o "contrato" de operações de persistência
 * para a entidade Supply, sem acoplar a aplicação a uma tecnologia específica
 * de banco de dados (como JDBC, JPA ou MongoDB).
 *
 * Princípios aplicados:
 * 1. Abstração: A camada de serviço conversa apenas com esta interface,
 *    desconhecendo detalhes como queries SQL ou conexões.
 * 2. Inversão de Dependência (DIP - SOLID): Módulos de alto nível não dependem
 *    de módulos de baixo nível; ambos dependem de abstrações.
 */
public interface SupplyRepository {

    /**
     * Localiza um insumo pelo seu identificador único.
     */
    Optional<Supply> findById(Long id);

    /**
     * Retorna todos os insumos cadastrados ordenados por ID.
     */
    List<Supply> findAll();

    /**
     * Salva um novo insumo no banco de dados e retorna a entidade com o ID gerado.
     */
    Supply save(Supply supply);

    /**
     * Atualiza os dados de um insumo existente.
     * @return true se o registro foi atualizado, false se não foi encontrado.
     */
    boolean update(Supply supply);

    /**
     * Aplica uma movimentação atômica no estoque físico do insumo.
     * @param id ID do insumo
     * @param amount Quantidade a somar (positiva) ou subtrair (negativa)
     * @return true se a movimentação foi bem-sucedida
     */
    boolean updateStock(Long id, long amount);

    /**
     * Executa baixa direta no estoque se o saldo for suficiente.
     * @param id ID do insumo
     * @param needed Quantidade necessária para baixar
     * @return true se a baixa foi executada com sucesso
     */
    boolean deductStockIfSufficient(Long id, long needed);

    /**
     * Bloqueia os registros de insumos para leitura concorrente (SELECT FOR UPDATE)
     * durante operações críticas de reserva e baixa.
     */
    void lockAllSupplies();

    /**
     * Verifica se existe um insumo com o ID informado.
     */
    boolean existsById(Long id);

    /**
     * Retorna a quantidade total de um insumo reservada por pedidos com status 'ABERTO'.
     */
    long findReservedQuantityBySupplyId(Long supplyId);
}
