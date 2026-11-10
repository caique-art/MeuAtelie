package br.com.botton.exception;

/**
 * ============================================================================
 * CONCEITO DE POO: HERANÇA E ESPECIALIZAÇÃO DE EXCEÇÕES
 * ============================================================================
 * Exceção especializada que indica que uma entidade (Insumo ou Pedido)
 * procurada por um ID específico não existe na base de dados.
 */
public class EntityNotFoundException extends BusinessRuleException {

    public EntityNotFoundException(String message) {
        super(message);
    }
}
