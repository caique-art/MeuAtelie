package br.com.botton.repository;

import br.com.botton.model.Order;
import br.com.botton.model.OrderRequirement;
import br.com.botton.model.OrderStatus;

import java.util.List;
import java.util.Optional;

/**
 * ============================================================================
 * CONCEITO DE POO: ABSTRAÇÃO E INTERFACES (Repository Pattern)
 * ============================================================================
 * Interface que define o contrato de persistência para pedidos e seus requisitos de insumos.
 */
public interface OrderRepository {

    /**
     * Localiza um pedido pelo ID.
     */
    Optional<Order> findById(Long id);

    /**
     * Localiza e bloqueia o pedido para atualização concorrente (SELECT FOR UPDATE).
     */
    Optional<Order> findByIdForUpdate(Long id);

    /**
     * Retorna todos os pedidos ordenados por status (ABERTO primeiro), data de entrega e ID.
     */
    List<Order> findAllOrdered();

    /**
     * Salva o cabeçalho de um novo pedido e retorna a entidade com o ID atribuído.
     */
    Order save(Order order);

    /**
     * Salva os requisitos de insumos associados a um pedido.
     */
    void saveRequirements(Long orderId, List<OrderRequirement> requirements);

    /**
     * Recupera os requisitos de insumos cadastrados para um pedido.
     */
    List<OrderRequirement> findRequirementsByOrderId(Long orderId);

    /**
     * Atualiza o status do pedido (ex: para CONCLUÍDO ou CANCELADO).
     */
    boolean updateStatus(Long orderId, OrderStatus newStatus);
}
