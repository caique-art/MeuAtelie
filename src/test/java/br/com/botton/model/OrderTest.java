package br.com.botton.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ============================================================================
 * CONCEITO DE POO: TESTES UNITÁRIOS DO CICLO DE VIDA DO PEDIDO
 * ============================================================================
 * Testes focados nas regras de transição de estado da entidade Order.
 */
class OrderTest {

    @Test
    @DisplayName("Pedido novo deve iniciar em estado ABERTO")
    void novoPedidoDeveEstarAberto() {
        Order order = new Order("Cliente Ana", "Botton 58mm", 50, LocalDate.now().plusDays(5));

        assertTrue(order.isAberto());
        assertEquals(OrderStatus.ABERTO, order.getStatus());
    }

    @Test
    @DisplayName("Deve transitar para CONCLUÍDO quando estiver ABERTO")
    void deveConcluirPedidoAberto() {
        Order order = new Order("Cliente Ana", "Botton 58mm", 50, LocalDate.now().plusDays(5));

        order.concluir();

        assertEquals(OrderStatus.CONCLUÍDO, order.getStatus());
        assertFalse(order.isAberto());

        // Não pode concluir novamente
        assertThrows(IllegalStateException.class, order::concluir);
    }

    @Test
    @DisplayName("Deve transitar para CANCELADO quando estiver ABERTO")
    void deveCancelarPedidoAberto() {
        Order order = new Order("Cliente Carlos", "Botton 38mm", 20, LocalDate.now().plusDays(2));

        order.cancelar();

        assertEquals(OrderStatus.CANCELADO, order.getStatus());
        assertFalse(order.isAberto());

        // Não pode cancelar novamente nem concluir pedido cancelado
        assertThrows(IllegalStateException.class, order::cancelar);
        assertThrows(IllegalStateException.class, order::concluir);
    }

    @Test
    @DisplayName("Deve rejeitar insumo duplicado na composição do pedido")
    void deveRejeitarInsumoDuplicado() {
        Order order = new Order("Cliente Maria", "Botton 45mm", 10, LocalDate.now().plusDays(1));

        order.adicionarRequisito(new OrderRequirement(1L, 1));

        // Tentar adicionar o mesmo insumo ID 1 novamente
        assertThrows(IllegalArgumentException.class, () ->
                order.adicionarRequisito(new OrderRequirement(1L, 2))
        );
    }

    @Test
    @DisplayName("Deve calcular consumo total de cada requisito com base na quantidade do pedido")
    void deveCalcularConsumoTotalDoRequisito() {
        OrderRequirement req = new OrderRequirement(1L, 2); // 2 unidades por botton

        long totalPara100Bottons = req.calcularConsumoTotal(100);

        assertEquals(200L, totalPara100Bottons);
    }
}
