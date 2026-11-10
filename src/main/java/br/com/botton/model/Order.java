package br.com.botton.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * ============================================================================
 * CONCEITO DE POO: ENTIDADE AGREGADORA (Aggregate Root) E ENCAPSULAMENTO
 * ============================================================================
 * A classe Order (Pedido de Bottons) representa uma demanda solicitada por um cliente.
 *
 * Princípios aplicados:
 * 1. Composição: Um pedido é composto por uma lista de requisitos (OrderRequirement)
 *    que especificam a receita de insumos necessária.
 * 2. Ciclo de Vida Controlado: A transição de estados (ABERTO -> CONCLUÍDO ou ABERTO -> CANCELADO)
 *    é protegida por métodos da própria classe (concluir(), cancelar()), impedindo que
 *    estados inválidos sejam atribuídos externamente.
 * 3. Integridade do Agregado: A lista interna de requisitos não é exposta diretamente
 *    para mutação externa (cópia defensiva / lista não modificável).
 */
public class Order {

    private Long id;
    private String customer;
    private String product;
    private int quantity;
    private LocalDate dueDate;
    private OrderStatus status;
    private final List<OrderRequirement> requirements = new ArrayList<>();

    public Order() {
        this.status = OrderStatus.ABERTO;
    }

    public Order(Long id, String customer, String product, int quantity, LocalDate dueDate, OrderStatus status) {
        this.id = id;
        this.customer = customer != null ? customer.trim() : null;
        this.product = product != null ? product.trim() : null;
        this.quantity = quantity;
        this.dueDate = dueDate;
        this.status = status != null ? status : OrderStatus.ABERTO;
        validarCampos();
    }

    public Order(String customer, String product, int quantity, LocalDate dueDate) {
        this(null, customer, product, quantity, dueDate, OrderStatus.ABERTO);
    }

    // ========================================================================
    // REGRAS DE NEGÓCIO E CONTROLE DE ESTADO
    // ========================================================================

    /**
     * Valida os campos essenciais do pedido.
     */
    public void validarCampos() {
        if (this.customer == null || this.customer.isBlank()) {
            throw new IllegalArgumentException("O nome do cliente é obrigatório.");
        }
        if (this.product == null || this.product.isBlank()) {
            throw new IllegalArgumentException("O modelo/produto é obrigatório.");
        }
        if (this.quantity <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        if (this.dueDate == null) {
            throw new IllegalArgumentException("A data de entrega é obrigatória.");
        }
    }

    /**
     * Verifica se o pedido está aberto para produção.
     */
    public boolean isAberto() {
        return this.status == OrderStatus.ABERTO;
    }

    /**
     * ========================================================================
     * TRANSIÇÃO DE ESTADO: Conclui a produção do pedido.
     * ========================================================================
     */
    public void concluir() {
        if (!isAberto()) {
            throw new IllegalStateException("Apenas pedidos em estado ABERTO podem ser concluídos.");
        }
        this.status = OrderStatus.CONCLUÍDO;
    }

    /**
     * ========================================================================
     * TRANSIÇÃO DE ESTADO: Cancela o pedido liberando as reservas.
     * ========================================================================
     */
    public void cancelar() {
        if (!isAberto()) {
            throw new IllegalStateException("Apenas pedidos em estado ABERTO podem ser cancelados.");
        }
        this.status = OrderStatus.CANCELADO;
    }

    /**
     * Adiciona um requisito de insumo ao pedido, garantindo que o mesmo insumo não seja duplicado.
     */
    public void adicionarRequisito(OrderRequirement requirement) {
        Objects.requireNonNull(requirement, "Requisito não pode ser nulo.");
        for (OrderRequirement r : this.requirements) {
            if (r.getSupplyId().equals(requirement.getSupplyId())) {
                throw new IllegalArgumentException("Insumo repetido na composição do pedido: ID " + requirement.getSupplyId());
            }
        }
        requirement.setOrderId(this.id);
        this.requirements.add(requirement);
    }

    /**
     * Define a lista de requisitos substituindo os anteriores.
     */
    public void setRequirements(List<OrderRequirement> novosRequisitos) {
        this.requirements.clear();
        if (novosRequisitos != null) {
            for (OrderRequirement req : novosRequisitos) {
                adicionarRequisito(req);
            }
        }
    }

    // ========================================================================
    // GETTERS E SETTERS
    // ========================================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
        for (OrderRequirement req : this.requirements) {
            req.setOrderId(id);
        }
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer != null ? customer.trim() : null;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product != null ? product.trim() : null;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    /**
     * Retorna uma visão não modificável dos requisitos para proteger o encapsulamento.
     */
    public List<OrderRequirement> getRequirements() {
        return Collections.unmodifiableList(requirements);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", customer='" + customer + '\'' +
                ", product='" + product + '\'' +
                ", quantity=" + quantity +
                ", dueDate=" + dueDate +
                ", status=" + status +
                ", requirementsCount=" + requirements.size() +
                '}';
    }
}
