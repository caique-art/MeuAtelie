package br.com.botton.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ============================================================================
 * CONCEITO DE POO: TESTES UNITÁRIOS DE DOMÍNIO PURO
 * ============================================================================
 * Como a lógica de negócio foi encapsulada nos métodos da entidade Supply
 * (em vez de espalhada em controllers ou SQL), podemos testá-la de forma
 * ultra-rápida e isolada, sem subir banco de dados ou contexto do Spring!
 */
class SupplyTest {

    @Test
    @DisplayName("Deve calcular saldo disponível corretamente (Estoque Físico - Reservas)")
    void deveCalcularDisponivel() {
        Supply supply = new Supply("Chapa 58mm", "un.", 100, 20, 200);

        long disponivel = supply.calcularDisponivel(30);

        assertEquals(70L, disponivel);
    }

    @Test
    @DisplayName("Deve determinar o alerta correto com base no saldo e limites")
    void deveDeterminarAlertaCorreto() {
        Supply supply = new Supply("Alfinete", "un.", 100, 20, 200);

        // Disponível 100 - 120 = -20 (Falta para pedidos)
        assertEquals(AlertStatus.FALTA, supply.determinarAlerta(120));

        // Disponível 100 - 90 = 10 (Abaixo do mínimo de 20)
        assertEquals(AlertStatus.BAIXO, supply.determinarAlerta(90));

        // Disponível 100 - 50 = 50 (Equilibrado, entre 20 e 200)
        assertEquals(AlertStatus.OK, supply.determinarAlerta(50));

        // Com estoque de 300 e 0 reservas = 300 (Acima do máximo de 200)
        Supply excesso = new Supply("Alfinete", "un.", 300, 20, 200);
        assertEquals(AlertStatus.EXCESSO, excesso.determinarAlerta(0));
    }

    @Test
    @DisplayName("Deve calcular quantidade exata de reposição quando estiver abaixo do mínimo")
    void deveCalcularReposicao() {
        Supply supply = new Supply("Película", "un.", 100, 30, 150);

        // Disponível: 100 - 90 = 10. Mínimo é 30. Reposição necessária = 30 - 10 = 20.
        assertEquals(20L, supply.calcularReposicao(90));

        // Disponível: 100 - 10 = 90. Mínimo é 30. Reposição = 0.
        assertEquals(0L, supply.calcularReposicao(10));
    }

    @Test
    @DisplayName("Não deve permitir estoque máximo menor que o mínimo")
    void naoDevePermitirMaximoMenorQueMinimo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Supply("Película", "un.", 50, 100, 50)
        );
    }

    @Test
    @DisplayName("Deve aplicar movimentação manual positiva e negativa com proteção de saldo")
    void deveMovimentarEstoqueComSeguranca() {
        Supply supply = new Supply("Película", "un.", 50, 10, 100);

        supply.movimentarEstoque(25);
        assertEquals(75L, supply.getStock());

        supply.movimentarEstoque(-50);
        assertEquals(25L, supply.getStock());

        // Não pode negativar estoque
        assertThrows(IllegalArgumentException.class, () -> supply.movimentarEstoque(-30));
        // Não pode movimentar zero
        assertThrows(IllegalArgumentException.class, () -> supply.movimentarEstoque(0));
    }
}
