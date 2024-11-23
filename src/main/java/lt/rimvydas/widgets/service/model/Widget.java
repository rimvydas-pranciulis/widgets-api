package lt.rimvydas.widgets.service.model;

import java.util.List;

public record Widget(
        Long id,
        String name,
        String serialNumber,
        List<ConnectionPort> connectionPorts
) {

}