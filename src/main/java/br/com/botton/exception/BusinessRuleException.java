package br.com.botton.exception;

/**
 * ============================================================================
 * CONCEITO DE POO: TRATAMENTO DE EXCEÇÕES DE NEGÓCIO (Herança de Exception)
 * ============================================================================
 * Exceção lançada quando alguma regra de negócio do domínio é violada
 * (ex: estoque insuficiente, status inválido para ação, limites incoerentes).
 *
 * Herda de RuntimeException (Unchecked Exception), permitindo que transações
 * gerenciadas pelo Spring (@Transactional) façam rollback automático.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }

    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
