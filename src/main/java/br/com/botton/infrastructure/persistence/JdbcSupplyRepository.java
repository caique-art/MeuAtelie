package br.com.botton.infrastructure.persistence;

import br.com.botton.model.Supply;
import br.com.botton.repository.SupplyRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * ============================================================================
 * CONCEITO DE POO: POLIMORFISMO E IMPLEMENTAÇÃO DE INTERFACE
 * ============================================================================
 * Esta classe é a implementação concreta da interface SupplyRepository via Spring JDBC.
 *
 * Princípios aplicados:
 * 1. Polimorfismo: O restante do sistema trabalha com o tipo abstrato SupplyRepository,
 *    permitindo que esta implementação seja substituída (por exemplo, por JPA/Hibernate
 *    ou uma implementação em memória para testes) sem alterar uma linha de regra de negócio.
 * 2. Separação de Responsabilidades (SRP): Isola todo o acesso a dados e comandos SQL
 *    referentes à tabela 'supplies' nesta única classe especializada.
 */
@Repository
public class JdbcSupplyRepository implements SupplyRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * RowMapper reutilizável que converte uma linha do ResultSet em um objeto Supply (POO).
     */
    private static final RowMapper<Supply> SUPPLY_ROW_MAPPER = (rs, rowNum) -> new Supply(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("unit"),
            rs.getLong("stock"),
            rs.getLong("minimum"),
            rs.getLong("maximum")
    );

    public JdbcSupplyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Supply> findById(Long id) {
        String sql = "SELECT id, name, unit, stock, minimum, maximum FROM supplies WHERE id = ?";
        try {
            Supply supply = jdbcTemplate.queryForObject(sql, SUPPLY_ROW_MAPPER, id);
            return Optional.ofNullable(supply);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Supply> findAll() {
        String sql = "SELECT id, name, unit, stock, minimum, maximum FROM supplies ORDER BY id";
        return jdbcTemplate.query(sql, SUPPLY_ROW_MAPPER);
    }

    @Override
    public Supply save(Supply supply) {
        String sql = "INSERT INTO supplies (name, unit, stock, minimum, maximum) VALUES (?, ?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, supply.getName());
            ps.setString(2, supply.getUnit());
            ps.setLong(3, supply.getStock());
            ps.setLong(4, supply.getMinimum());
            ps.setLong(5, supply.getMaximum());
            return ps;
        }, keyHolder);

        long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        supply.setId(generatedId);
        return supply;
    }

    @Override
    public boolean update(Supply supply) {
        String sql = "UPDATE supplies SET name = ?, unit = ?, stock = ?, minimum = ?, maximum = ? WHERE id = ?";
        int updated = jdbcTemplate.update(
                sql,
                supply.getName(),
                supply.getUnit(),
                supply.getStock(),
                supply.getMinimum(),
                supply.getMaximum(),
                supply.getId()
        );
        return updated > 0;
    }

    @Override
    public boolean updateStock(Long id, long amount) {
        // Valida que o saldo não fique negativo nem exceda 1 bilhão
        String sql = "UPDATE supplies SET stock = stock + ? WHERE id = ? AND stock + ? BETWEEN 0 AND 1000000000";
        int updated = jdbcTemplate.update(sql, amount, id, amount);
        return updated > 0;
    }

    @Override
    public boolean deductStockIfSufficient(Long id, long needed) {
        String sql = "UPDATE supplies SET stock = stock - ? WHERE id = ? AND stock >= ?";
        int updated = jdbcTemplate.update(sql, needed, id, needed);
        return updated > 0;
    }

    @Override
    public void lockAllSupplies() {
        jdbcTemplate.queryForList("SELECT id FROM supplies ORDER BY id FOR UPDATE");
    }

    @Override
    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM supplies WHERE id = ?",
                Integer.class,
                id
        );
        return count != null && count > 0;
    }

    @Override
    public long findReservedQuantityBySupplyId(Long supplyId) {
        String sql = """
            SELECT COALESCE(SUM(CAST(o.quantity AS SIGNED) * r.per_unit), 0)
            FROM requirements r
            JOIN orders o ON o.id = r.order_id
            WHERE r.supply_id = ? AND o.status = 'ABERTO'
        """;
        Long reserved = jdbcTemplate.queryForObject(sql, Long.class, supplyId);
        return reserved != null ? reserved : 0L;
    }
}
