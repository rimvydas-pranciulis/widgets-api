package lt.rimvydas.widgets.repository;

import lt.rimvydas.widgets.model.ConnectionPort;
import lt.rimvydas.widgets.model.WidgetConnection;
import lt.rimvydas.widgets.model.WidgetConnectionDetails;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class WidgetConnectionRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public WidgetConnectionRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(WidgetConnection widgetConnection) {
        var query = """
                INSERT INTO widget_connections(widget1_id, widget2_id, widget1_connection_port, widget2_connection_port, created_at)
                VALUES(:widget1_id, :widget2_id, :widget1_connection_port, :widget2_connection_port, NOW())""";
        var params = Map.of(
                "widget1_id", widgetConnection.widget1().widgetId(),
                "widget2_id", widgetConnection.widget2().widgetId(),
                "widget1_connection_port", widgetConnection.widget1().connectionPort().name(),
                "widget2_connection_port", widgetConnection.widget2().connectionPort().name()
        );
        jdbcTemplate.update(query, params);
    }

    public List<WidgetConnection> findWidgetConnections(Set<Long> widgetIds) {
        var query = "SELECT * FROM widget_connections WHERE widget1_id IN (:widget_ids) OR widget2_id IN (:widget_ids)";
        var params = Map.of("widget_ids", widgetIds);
        return jdbcTemplate.query(query, params, this::resultSetToWidgetConnectionMapper);
    }

    private WidgetConnection resultSetToWidgetConnectionMapper(ResultSet rs, int numRows) throws SQLException {
        return new WidgetConnection(
                new WidgetConnectionDetails(rs.getLong("widget1_id"), ConnectionPort.valueOf(rs.getString("widget1_connection_port"))),
                new WidgetConnectionDetails(rs.getLong("widget2_id"), ConnectionPort.valueOf(rs.getString("widget2_connection_port")))
        );
    }
}
