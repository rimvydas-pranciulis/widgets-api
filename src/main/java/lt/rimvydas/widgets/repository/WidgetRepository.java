package lt.rimvydas.widgets.repository;

import lt.rimvydas.widgets.model.ConnectionPort;
import lt.rimvydas.widgets.model.Widget;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public class WidgetRepository {

    private static final String CONNECTION_PORTS_SEPARATOR = ",";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public WidgetRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Widget widget) {
        var query = "INSERT INTO widgets(name, serial_number, connection_ports, created_at) VALUES(:name, :serial_number, :connection_ports, NOW())";
        var connectionPorts = widget.connectionPorts().stream()
                .map(ConnectionPort::name)
                .toList();
        var params = Map.of(
                "name", widget.name(),
                "serial_number", widget.serialNumber(),
                "connection_ports", String.join(CONNECTION_PORTS_SEPARATOR, connectionPorts)
        );
        jdbcTemplate.update(query, params);
    }

    public Optional<Widget> findBySerialNumber(String serialNumber) {
        return findBySerialNumbers(Set.of(serialNumber)).stream()
                .findFirst();
    }

    public List<Widget> findBySerialNumbers(Set<String> serialNumbers) {
        var query = "SELECT * FROM widgets WHERE serial_number IN (:serial_numbers)";
        var params = Map.of("serial_numbers", serialNumbers);
        return jdbcTemplate.query(query, params, this::resultSetToWidgetMapper);
    }

    public void deleteBySerialNumber(String serialNumber) {
        var query = "DELETE FROM widgets WHERE serial_number = :serial_number";
        var params = Map.of("serial_number", serialNumber);
        jdbcTemplate.update(query, params);
    }

    private Widget resultSetToWidgetMapper(ResultSet resultSet, int numRows) throws SQLException {
        var connectionPorts = Arrays.stream(resultSet.getString("connection_ports").split(CONNECTION_PORTS_SEPARATOR))
                .map(ConnectionPort::valueOf)
                .toList();
        return new Widget(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("serial_number"),
                connectionPorts
        );
    }
}
