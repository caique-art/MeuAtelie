package br.com.botton.model;

/**
 * ============================================================================
 * CONCEITO DE POO: ENCAPSULAMENTO DE ESTADOS (Enum)
 * ============================================================================
 * Representa os possíveis estados do ciclo de vida de um pedido de bottons.
 * O uso do enum garante consistência, evitando valores literais inválidos.
 */
public enum OrderStatus {
    ABERTO("Aguardando produção"),
    CONCLUÍDO("Produção finalizada e estoque baixado"),
    CANCELADO("Cancelado e reservas liberadas");

    private final String descricao;

    OrderStatus(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /**
     * Verifica se o pedido ainda está em aberto e pode receber ações de concluir ou cancelar.
     */
    public boolean isAberto() {
        return this == ABERTO;
    }
}
