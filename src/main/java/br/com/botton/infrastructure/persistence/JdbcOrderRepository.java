package br.com.botton.infrastructure.persistence;

import br.com.botton.model.Order;
import br.com.botton.model.OrderRequirement;
import br.com.botton.model.OrderStatus;
import br.com.botton.repository.OrderRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * ============================================================================
 * CONCEITO DE POO: POLIMORFISMO E IMPLEMENTAÇÃO DE REPOSITÓRIO
 * ============================================================================
 * Implementação da interface OrderRepository com persistência relacional (Spring JDBC).
 * Isola queries SQL das tabelas 'orders' e 'requirements'.
 */
@Repository
public class JdbcOrderRepository implements OrderRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<Order> ORDER_ROW_MAPPER = (rs, rowNum) -> new Order(
            rs.getLong("id"),
            rs.getString("customer"),
            rs.getString("product"),
            rs.getInt("quantity"),
            rs.getDate("due_date").toLocalDate(),
            OrderStatus.valueOf(rs.getString("status"))
    );

    private static final RowMapper<OrderRequirement> REQUIREMENT_ROW_MAPPER = (rs, rowNum) -> new OrderRequirement(
            rs.getLong("order_id"),
            rs.getLong("supply_id"),
            rs.getInt("per_unit")
    );

    public JdbcOrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Order> findById(Long id) {
        String sql = "SELECT id, customer, product, quantity, due_date, status FROM orders WHERE id = ?";
        try {
            Order order = jdbcTemplate.queryForObject(sql, ORDER_ROW_MAPPER, id);
            if (order != null) {
                order.setRequirements(findRequirementsByOrderId(id));
            }
            return Optional.ofNullable(order);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Order> findByIdForUpdate(Long id) {
        String sql = "SELECT id, customer, product, quantity, due_date, status FROM orders WHERE id = ? FOR UPDATE";
        try {
            Order order = jdbcTemplate.queryForObject(sql, ORDER_ROW_MAPPER, id);
            if (order != null) {
                order.setRequirements(findRequirementsByOrderId(id));
            }
            return Optional.ofNullable(order);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Order> findAllOrdered() {
        // Ordena: Pedidos 'ABERTO' primeiro, depois por data de entrega e ID
        String sql = "SELECT id, customer, product, quantity, due_date, status FROM orders ORDER BY CASE WHEN status = 'ABERTO' THEN 0 ELSE 1 END, due_date, id";
        return jdbcTemplate.query(sql, ORDER_ROW_MAPPER);
    }

    @Override
    public Order save(Order order) {
        String sql = "INSERT INTO orders (customer, product, quantity, due_date, status) VALUES (?, ?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, order.getCustomer());
            ps.setString(2, order.getProduct());
            ps.setInt(3, order.getQuantity());
            ps.setDate(4, Date.valueOf(order.getDueDate()));
            ps.setString(5, order.getStatus().name());
            return ps;
        }, keyHolder);

        long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        order.setId(generatedId);
        return order;
    }

    @Override
    public void saveRequirements(Long orderId, List<OrderRequirement> requirements) {
        String sql = "INSERT INTO requirements (order_id, supply_id, per_unit) VALUES (?, ?, ?)";
        for (OrderRequirement req : requirements) {
            jdbcTemplate.update(sql, orderId, req.getSupplyId(), req.getPerUnit());
        }
    }

    @Override
    public List<OrderRequirement> findRequirementsByOrderId(Long orderId) {
        String sql = "SELECT order_id, supply_id, per_unit FROM requirements WHERE order_id = ? ORDER BY supply_id";
        return jdbcTemplate.query(sql, REQUIREMENT_ROW_MAPPER, orderId);
    }

    @Override
    public boolean updateStatus(Long orderId, OrderStatus newStatus) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        int updated = jdbcTemplate.update(sql, newStatus.name(), orderId);
        return updated > 0;
    }
}
