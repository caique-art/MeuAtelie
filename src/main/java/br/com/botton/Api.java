package br.com.botton;

import br.com.botton.exception.BusinessRuleException;
import br.com.botton.model.Order;
import br.com.botton.model.OrderRequirement;
import br.com.botton.service.OrderService;
import br.com.botton.service.SupplyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

/**
 * ============================================================================
 * CONCEITO DE POO: CAMADA CONTROLADORA (REST Controller) E PADRÃO FACADE/DTO
 * ============================================================================
 * A classe Api atua como a fronteira de comunicação HTTP (Web / REST API) do sistema.
 *
 * Princípios aplicados:
 * 1. Princípio da Responsabilidade Única (SRP): O Controller NÃO executa regras de negócio,
 *    NÃO calcula alertas de estoque e NÃO executa comandos SQL diretamente. Sua única
 *    responsabilidade é receber a requisição HTTP, validar o formato dos dados via DTO,
 *    delegar para os Serviços especializados (SupplyService e OrderService) e retornar
 *    a resposta HTTP adequada.
 * 2. Injeção de Dependências (DI): Os serviços de domínio são injetados pelo Spring via construtor.
 * 3. DTO (Data Transfer Objects): Os records internos (Supply, Ingredient, Demand, Movement)
 *    definem os contratos de entrada/saída com validação declarativa (@Valid, @NotBlank, @Min, @Max),
 *    mantendo compatibilidade total com o frontend e com os testes automatizados.
 * 4. Tratamento de Exceções: Erros de regra de negócio disparados pelo domínio são capturados
 *    e convertidos em ResponseStatusException (HTTP 400 Bad Request) com mensagens claras.
 */
@RestController
@RequestMapping("/api")
public class Api {

    private final SupplyService supplyService;
    private final OrderService orderService;

    /**
     * Injeção de dependência via construtor dos serviços de negócio (SupplyService e OrderService).
     */
    public Api(SupplyService supplyService, OrderService orderService) {
        this.supplyService = Objects.requireNonNull(supplyService, "SupplyService é obrigatório");
        this.orderService = Objects.requireNonNull(orderService, "OrderService é obrigatório");
    }

    // ========================================================================
    // CONTRATOS DE TRANSFERÊNCIA DE DADOS (DTOs com Records do Java)
    // ========================================================================

    /**
     * DTO para criação e edição de insumos.
     */
    public record Supply(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Size(max = 30) String unit,
            @Min(0) @Max(1000000000) long stock,
            @Min(0) @Max(1000000000) long minimum,
            @Min(0) @Max(1000000000) long maximum
    ) {}

    /**
     * DTO para representar cada insumo consumido por unidade de botton.
     */
    public record Ingredient(
            @Min(1) long supplyId,
            @Min(1) @Max(10000) int perUnit
    ) {}

    /**
     * DTO para criação de novos pedidos de clientes.
     */
    public record Demand(
            @NotBlank @Size(max = 120) String customer,
            @NotBlank @Size(max = 120) String product,
            @Min(1) @Max(1000000) int quantity,
            @NotNull LocalDate dueDate,
            @NotEmpty List<@Valid Ingredient> ingredients
    ) {}

    /**
     * DTO para registrar entradas ou saídas manuais no estoque.
     */
    public record Movement(
            @Min(-1000000000) @Max(1000000000) long amount
    ) {}

    // ========================================================================
    // ENDPOINTS REST
    // ========================================================================

    /**
     * Retorna o estado consolidado do sistema (insumos com cálculos de reservas/alertas e lista de pedidos).
     * Endpoint consumido pela tela inicial do ateliê.
     */
    @GetMapping("/state")
    public Map<String, Object> state() {
        try {
            var supplies = supplyService.getSuppliesWithCalculatedState();
            var orders = orderService.getAllOrdersFormatted();
            return Map.of("supplies", supplies, "orders", orders);
        } catch (Exception e) {
            throw bad(e.getMessage());
        }
    }

    /**
     * Cadastra um novo insumo.
     */
    @PostMapping("/supplies")
    public Map<String, Long> addSupply(@Valid @RequestBody Supply s) {
        try {
            if (s.maximum() < s.minimum()) {
                throw bad("O máximo deve ser maior ou igual ao mínimo.");
            }
            br.com.botton.model.Supply domainSupply = new br.com.botton.model.Supply(
                    s.name(),
                    s.unit(),
                    s.stock(),
                    s.minimum(),
                    s.maximum()
            );
            Long id = supplyService.createSupply(domainSupply);
            return Map.of("id", id);
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            throw bad(e.getMessage());
        }
    }

    /**
     * Atualiza os dados de um insumo existente.
     */
    @PutMapping("/supplies/{id}")
    public void editSupply(@PathVariable long id, @Valid @RequestBody Supply s) {
        try {
            if (s.maximum() < s.minimum()) {
                throw bad("Máximo menor que mínimo.");
            }
            br.com.botton.model.Supply domainSupply = new br.com.botton.model.Supply(
                    id,
                    s.name(),
                    s.unit(),
                    s.stock(),
                    s.minimum(),
                    s.maximum()
            );
            supplyService.updateSupply(id, domainSupply);
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            throw bad(e.getMessage());
        }
    }

    /**
     * Realiza uma movimentação manual de estoque (+ entrada, - saída).
     */
    @PostMapping("/supplies/{id}/movement")
    public void move(@PathVariable long id, @Valid @RequestBody Movement m) {
        try {
            supplyService.moveStock(id, m.amount());
        } catch (Exception e) {
            throw bad(e.getMessage());
        }
    }

    /**
     * Cadastra um novo pedido de bottons associando seus insumos necessários.
     */
    @PostMapping("/orders")
    public Map<String, Long> addOrder(@Valid @RequestBody Demand d) {
        try {
            Order order = new Order(d.customer(), d.product(), d.quantity(), d.dueDate());
            for (Ingredient ing : d.ingredients()) {
                order.adicionarRequisito(new OrderRequirement(ing.supplyId(), ing.perUnit()));
            }
            Long id = orderService.createOrder(order);
            return Map.of("id", id);
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (BusinessRuleException bre) {
            throw bad(bre.getMessage());
        } catch (Exception e) {
            throw bad(e.getMessage());
        }
    }

    /**
     * Finaliza um pedido aberto (ação 'complete' para concluir ou 'cancel' para cancelar).
     */
    @PostMapping("/orders/{id}/{action}")
    public void finish(@PathVariable long id, @PathVariable String action) {
        try {
            orderService.finishOrder(id, action);
        } catch (BusinessRuleException bre) {
            throw bad(bre.getMessage());
        } catch (Exception e) {
            throw bad(e.getMessage());
        }
    }

    /**
     * Método auxiliar de conveniência para construir exceções HTTP 400 Bad Request.
     */
    private ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
