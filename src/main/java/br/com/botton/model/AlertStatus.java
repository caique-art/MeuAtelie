package br.com.botton.model;

/**
 * ============================================================================
 * CONCEITO DE POO: ENUMERADOR RICO (Type-Safe Enum com Comportamento)
 * ============================================================================
 * Representa os estados de alerta possíveis para o estoque de um insumo.
 * Em vez de usar Strings soltas ("OK", "BAIXO", etc.), encapsulamos os valores
 * permitidos e a regra de decisão dentro do próprio Enum.
 */
public enum AlertStatus {
    /**
     * Quando o saldo disponível é menor que zero (pedidos abertos superam o estoque físico).
     */
    FALTA("Falta para pedidos"),

    /**
     * Quando o saldo disponível está positivo, mas abaixo do estoque mínimo estipulado.
     */
    BAIXO("Estoque baixo"),

    /**
     * Quando o saldo disponível ultrapassa o estoque máximo estipulado.
     */
    EXCESSO("Em excesso"),

    /**
     * Quando o estoque disponível está dentro dos limites adequados (mínimo <= disponível <= máximo).
     */
    OK("Equilibrado");

    private final String descricao;

    AlertStatus(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /**
     * ========================================================================
     * REGRA DE NEGÓCIO ENCAPSULADA:
     * Calcula o status de alerta com base na quantidade disponível e nos limites.
     * ========================================================================
     *
     * @param disponivel Saldo físico menos a quantidade reservada por pedidos abertos
     * @param minimo     Limite mínimo desejável para segurança operacional
     * @param maximo     Limite máximo para evitar desperdício/custo de estocagem
     * @return O status correspondente
     */
    public static AlertStatus calcular(long disponivel, long minimo, long maximo) {
        if (disponivel < 0) {
            return FALTA;
        } else if (disponivel < minimo) {
            return BAIXO;
        } else if (disponivel > maximo) {
            return EXCESSO;
        } else {
            return OK;
        }
    }
}
