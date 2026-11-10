package br.com.botton.model;

import java.util.Objects;

/**
 * ============================================================================
 * CONCEITO DE POO: CLASSE DE ASSOCIAÇÃO / COMPOSIÇÃO
 * ============================================================================
 * Representa a composição de insumos necessária para fabricar cada unidade de botton
 * em um pedido específico.
 *
 * Exemplo: Para fazer 1 botton do modelo "58mm", gasta-se:
 * - 1 unidade de Chapa 58mm (perUnit = 1)
 * - 1 unidade de Película (perUnit = 1)
 * - 1 unidade de Alfinete (perUnit = 1)
 */
public class OrderRequirement {

    private Long orderId;
    private Long supplyId;
    private int perUnit;

    public OrderRequirement() {
    }

    public OrderRequirement(Long orderId, Long supplyId, int perUnit) {
        this.orderId = orderId;
        this.supplyId = supplyId;
        this.perUnit = perUnit;
    }

    public OrderRequirement(Long supplyId, int perUnit) {
        this(null, supplyId, perUnit);
    }

    /**
     * ========================================================================
     * COMPORTAMENTO ENCAPSULADO:
     * Calcula o total necessário deste insumo para a quantidade total do pedido.
     * Fórmula: Total = Quantidade de Bottons * Consumo por Unidade
     * ========================================================================
     *
     * @param orderQuantity Quantidade de bottons do pedido
     * @return Total de insumos que serão consumidos
     */
    public long calcularConsumoTotal(int orderQuantity) {
        if (orderQuantity <= 0) {
            throw new IllegalArgumentException("A quantidade do pedido deve ser maior que zero.");
        }
        return (long) orderQuantity * this.perUnit;
    }

    // ========================================================================
    // GETTERS E SETTERS
    // ========================================================================

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getSupplyId() {
        return supplyId;
    }

    public void setSupplyId(Long supplyId) {
        this.supplyId = supplyId;
    }

    public int getPerUnit() {
        return perUnit;
    }

    public void setPerUnit(int perUnit) {
        if (perUnit <= 0) {
            throw new IllegalArgumentException("O consumo por unidade deve ser maior que zero.");
        }
        this.perUnit = perUnit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderRequirement that = (OrderRequirement) o;
        return Objects.equals(orderId, that.orderId) && Objects.equals(supplyId, that.supplyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, supplyId);
    }

    @Override
    public String toString() {
        return "OrderRequirement{" +
                "orderId=" + orderId +
                ", supplyId=" + supplyId +
                ", perUnit=" + perUnit +
                '}';
    }
}
