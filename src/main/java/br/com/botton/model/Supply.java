package br.com.botton.model;

import java.util.Objects;

/**
 * ============================================================================
 * CONCEITO DE POO: ENTIDADE DE DOMÍNIO E ENCAPSULAMENTO
 * ============================================================================
 * A classe Supply (Insumo) representa um material no mundo real (ex: Chapa 58mm,
 * Alfinete, Película plástica).
 *
 * Princípios aplicados:
 * 1. Ocultamento de Informação (Data Hiding): Todos os atributos são privados (private).
 * 2. Invariantes de Negócio: Os métodos garantem que o objeto nunca entre em estado inválido
 *    (por exemplo, estoque máximo inferior ao mínimo, ou estoque físico negativo).
 * 3. Modelo Rico (Rich Domain Model): A classe não é apenas um repositório de dados burro
 *    (anêmico), ela contém comportamentos e cálculos que pertencem naturalmente a ela.
 */
public class Supply {

    private Long id;
    private String name;
    private String unit;
    private long stock;
    private long minimum;
    private long maximum;

    /**
     * Construtor padrão (necessário para frameworks e instanciação vazia).
     */
    public Supply() {
    }

    /**
     * Construtor completo para inicialização do objeto com todos os seus atributos.
     */
    public Supply(Long id, String name, String unit, long stock, long minimum, long maximum) {
        this.id = id;
        this.name = name != null ? name.trim() : null;
        this.unit = unit != null ? unit.trim() : null;
        this.stock = stock;
        this.minimum = minimum;
        this.maximum = maximum;
        validarLimites();
    }

    /**
     * Construtor para novos insumos (sem ID persistido ainda).
     */
    public Supply(String name, String unit, long stock, long minimum, long maximum) {
        this(null, name, unit, stock, minimum, maximum);
    }

    // ========================================================================
    // REGRAS DE NEGÓCIO E COMPORTAMENTOS (MÉTODOS DE DOMÍNIO)
    // ========================================================================

    /**
     * Valida os limites de estoque. O estoque máximo deve ser sempre
     * maior ou igual ao mínimo.
     *
     * @throws IllegalArgumentException se o máximo for menor que o mínimo
     */
    public void validarLimites() {
        if (this.maximum < this.minimum) {
            throw new IllegalArgumentException("O estoque máximo deve ser maior ou igual ao mínimo.");
        }
    }

    /**
     * Calcula o estoque disponível considerando o que já está comprometido com pedidos abertos.
     * Fórmula: Disponível = Estoque Físico - Reservado
     *
     * @param reservado Total de unidades deste insumo alocadas em pedidos abertos
     * @return Quantidade disponível
     */
    public long calcularDisponivel(long reservado) {
        return this.stock - reservado;
    }

    /**
     * Determina a situação de alerta do insumo com base nas reservas atuais.
     *
     * @param reservado Quantidade reservada por pedidos em aberto
     * @return Status do alerta (FALTA, BAIXO, EXCESSO ou OK)
     */
    public AlertStatus determinarAlerta(long reservado) {
        long disponivel = calcularDisponivel(reservado);
        return AlertStatus.calcular(disponivel, this.minimum, this.maximum);
    }

    /**
     * Calcula a quantidade sugerida para reposição caso o estoque esteja abaixo do mínimo.
     *
     * @param reservado Quantidade reservada por pedidos em aberto
     * @return Quantidade a repor (0 se não houver necessidade)
     */
    public long calcularReposicao(long reservado) {
        long disponivel = calcularDisponivel(reservado);
        return Math.max(0, this.minimum - disponivel);
    }

    /**
     * Verifica se o insumo possui estoque físico suficiente para uma dada baixa.
     *
     * @param quantidadeNecessaria Quantidade necessária
     * @return true se houver estoque suficiente
     */
    public boolean possuiEstoqueSuficiente(long quantidadeNecessaria) {
        return this.stock >= quantidadeNecessaria;
    }

    /**
     * Aplica uma movimentação manual no estoque (positiva para entrada, negativa para saída).
     *
     * @param quantidade Quantidade a movimentar (+ entrada, - saída)
     */
    public void movimentarEstoque(long quantidade) {
        if (quantidade == 0) {
            throw new IllegalArgumentException("Informe uma quantidade diferente de zero.");
        }
        long novoSaldo = this.stock + quantidade;
        if (novoSaldo < 0 || novoSaldo > 1_000_000_000L) {
            throw new IllegalArgumentException("Movimentação resultaria em saldo inválido para o insumo: " + novoSaldo);
        }
        this.stock = novoSaldo;
    }

    /**
     * Deduz uma quantidade do estoque físico ao finalizar uma produção.
     *
     * @param quantidade Quantidade consumida
     */
    public void baixarEstoque(long quantidade) {
        if (quantidade < 0) {
            throw new IllegalArgumentException("A quantidade de baixa não pode ser negativa.");
        }
        if (!possuiEstoqueSuficiente(quantidade)) {
            throw new IllegalStateException("Estoque insuficiente para o insumo '" + this.name + "'.");
        }
        this.stock -= quantidade;
    }

    // ========================================================================
    // GETTERS E SETTERS (ACESSO ENCAPSULADO AOS ATRIBUTOS)
    // ========================================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit != null ? unit.trim() : null;
    }

    public long getStock() {
        return stock;
    }

    public void setStock(long stock) {
        this.stock = stock;
    }

    public long getMinimum() {
        return minimum;
    }

    public void setMinimum(long minimum) {
        this.minimum = minimum;
        validarLimites();
    }

    public long getMaximum() {
        return maximum;
    }

    public void setMaximum(long maximum) {
        this.maximum = maximum;
        validarLimites();
    }

    // ========================================================================
    // EQUALS, HASHCODE E TOSTRING (BOAS PRÁTICAS DE POO EM JAVA)
    // ========================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Supply supply = (Supply) o;
        return Objects.equals(id, supply.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Supply{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", unit='" + unit + '\'' +
                ", stock=" + stock +
                ", minimum=" + minimum +
                ", maximum=" + maximum +
                '}';
    }
}
