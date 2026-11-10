package br.com.botton.service;

import br.com.botton.exception.BusinessRuleException;
import br.com.botton.model.Order;
import br.com.botton.model.OrderRequirement;
import br.com.botton.model.OrderStatus;
import br.com.botton.repository.OrderRepository;
import br.com.botton.repository.SupplyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * ============================================================================
 * CONCEITO DE POO: SERVIÇO DE DOMÍNIO E TRANSAÇÃO ATÔMICA
 * ============================================================================
 * O OrderService coordena as operações do ciclo de vida dos pedidos e sua relação
 * com os insumos de fabricação de bottons.
 *
 * Princípios aplicados:
 * 1. Coesão: Centraliza toda a lógica de criação, validação de composição e
 *    baixa de estoque de pedidos.
 * 2. Atomicidade e Consistência (ACID): O método finishOrder é anotado com
 *    @Transactional. Se um único insumo faltar durante a conclusão, todos os insumos
 *    já descontados sofrem Rollback imediato, preservando a integridade do estoque.
 * 3. Encapsulamento de Regras: A validação de unicidade de insumos e checagem de
 *    existência ocorre antes da persistência.
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final SupplyRepository supplyRepository;

    public OrderService(OrderRepository orderRepository, SupplyRepository supplyRepository) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "OrderRepository é obrigatório");
        this.supplyRepository = Objects.requireNonNull(supplyRepository, "SupplyRepository é obrigatório");
    }

    /**
     * Cadastra um novo pedido com sua lista de insumos necessários por botton.
     */
    @Transactional
    public Long createOrder(Order order) {
        Objects.requireNonNull(order, "Pedido não pode ser nulo.");
        order.validarCampos();

        // Bloqueia insumos para garantir que o cálculo de reservas seja consistente
        supplyRepository.lockAllSupplies();

        // Valida composição dos insumos
        Set<Long> supplyIds = new HashSet<>();
        for (OrderRequirement req : order.getRequirements()) {
            if (!supplyIds.add(req.getSupplyId())) {
                throw new BusinessRuleException("Insumo repetido.");
            }
            if (!supplyRepository.existsById(req.getSupplyId())) {
                throw new BusinessRuleException("Insumo não encontrado.");
            }
        }

        Order saved = orderRepository.save(order);
        orderRepository.saveRequirements(saved.getId(), order.getRequirements());
        return saved.getId();
    }

    /**
     * Finaliza um pedido aberto, executando a ação especificada:
     * - "complete": valida e deduz todos os insumos consumidos em transação única.
     * - "cancel": cancela o pedido liberando as reservas de insumos.
     *
     * @param orderId Identificador do pedido
     * @param action  Ação desejada ("complete" ou "cancel")
     */
    @Transactional
    public void finishOrder(Long orderId, String action) {
        if (!"complete".equals(action) && !"cancel".equals(action)) {
            throw new BusinessRuleException("Ação inválida.");
        }

        // Bloqueia os insumos para evitar concorrência durante a baixa
        supplyRepository.lockAllSupplies();

        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new BusinessRuleException("Pedido não está aberto."));

        if (!order.isAberto()) {
            throw new BusinessRuleException("Pedido não está aberto.");
        }

        if ("complete".equals(action)) {
            concluirProducao(order);
        } else {
            cancelarPedido(order);
        }
    }

    /**
     * Executa a baixa de materiais para conclusão da produção.
     * Se qualquer material estiver em falta, lança BusinessRuleException e a
     * transação do Spring realiza o Rollback de todos os descontos feitos.
     */
    private void concluirProducao(Order order) {
        List<OrderRequirement> requirements = order.getRequirements();
        if (requirements.isEmpty()) {
            requirements = orderRepository.findRequirementsByOrderId(order.getId());
        }

        for (OrderRequirement req : requirements) {
            long totalNecessario = req.calcularConsumoTotal(order.getQuantity());
            boolean baixou = supplyRepository.deductStockIfSufficient(req.getSupplyId(), totalNecessario);

            if (!baixou) {
                // Ao lançar a exceção, o Spring realiza ROLLBACK automático de tudo que foi baixado nesta transação!
                throw new BusinessRuleException("Estoque insuficiente. Reponha os insumos antes de concluir.");
            }
        }

        order.concluir();
        orderRepository.updateStatus(order.getId(), OrderStatus.CONCLUÍDO);
    }

    /**
     * Cancela o pedido e libera suas reservas no ateliê.
     */
    private void cancelarPedido(Order order) {
        order.cancelar();
        orderRepository.updateStatus(order.getId(), OrderStatus.CANCELADO);
    }

    /**
     * Retorna todos os pedidos formatados para a resposta do dashboard.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllOrdersFormatted() {
        List<Order> orders = orderRepository.findAllOrdered();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Order o : orders) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", o.getId());
            map.put("customer", o.getCustomer());
            map.put("product", o.getProduct());
            map.put("quantity", o.getQuantity());
            map.put("due_date", o.getDueDate().toString());
            map.put("status", o.getStatus().name());
            result.add(map);
        }

        return result;
    }
}
