package br.com.botton;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * PONTO DE ENTRADA DA APLICAÇÃO SPRING BOOT
 * ============================================================================
 * Esta classe inicializa o container de Inversão de Controle (IoC) e Injeção
 * de Dependências (DI) do Spring Boot.
 *
 * A anotação @SpringBootApplication ativa:
 * 1. @Configuration: Permite registrar beans extras no contexto.
 * 2. @EnableAutoConfiguration: Configura automaticamente DataSource, Web Server, etc.
 * 3. @ComponentScan: Varre o pacote br.com.botton e seus subpacotes
 *    (model, repository, service, infrastructure, etc.) instanciando os componentes.
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
