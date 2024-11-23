package lt.rimvydas.widgets.service;

import lt.rimvydas.widgets.service.model.ConnectionPort;
import lt.rimvydas.widgets.service.model.Widget;
import lt.rimvydas.widgets.service.model.WidgetConnection;
import lt.rimvydas.widgets.service.model.WidgetConnectionDetails;
import lt.rimvydas.widgets.service.model.WidgetConnectionRequest;
import lt.rimvydas.widgets.service.repository.WidgetConnectionsRepository;
import lt.rimvydas.widgets.service.repository.WidgetsRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
// TODO: Plural vs Singular
public class WidgetsService {

    private final WidgetsRepository widgetsRepository;
    private final WidgetConnectionsRepository widgetConnectionsRepository;

    public WidgetsService(WidgetsRepository widgetsRepository, WidgetConnectionsRepository widgetConnectionsRepository) {
        this.widgetsRepository = widgetsRepository;
        this.widgetConnectionsRepository = widgetConnectionsRepository;
    }

    public void test() {
        System.out.println(widgetsRepository.findBySerialNumbers(Set.of("serial", "serial2", "serial3")));
    }

    public Widget createWidget(Widget widget) {
        widgetsRepository.save(widget);
        return getWidget(widget.serialNumber()).orElseThrow(
                () -> new RuntimeException("Couldn't find created widget by serial number: " + widget.serialNumber()));
    }

    public Optional<Widget> getWidget(String serialNumber) {
        return widgetsRepository.findBySerialNumber(serialNumber);
    }

    public void deleteWidget(String serialNumber) {
        widgetsRepository.deleteBySerialNumber(serialNumber);
    }

    public void connectWidgets(WidgetConnectionRequest widget1ConnectionRequest, WidgetConnectionRequest widget2ConnectionRequest) {
        var widgets = widgetsRepository.findBySerialNumbers(Set.of(widget1ConnectionRequest.serialNumber(), widget2ConnectionRequest.serialNumber()));
        var widget1 = validateAndGetWidget(widgets, widget1ConnectionRequest.serialNumber(), widget1ConnectionRequest.connectionPort());
        var widget2 = validateAndGetWidget(widgets, widget2ConnectionRequest.serialNumber(), widget2ConnectionRequest.connectionPort());

        var existingConnections = widgetConnectionsRepository.findWidgetConnections(Set.of(widget1.id(), widget2.id()));
        validateThatWidgetsAreNotAlreadyConnected(widget1, widget2, existingConnections);
        validateExistingConnections(widget1, existingConnections, widget1ConnectionRequest);
        validateExistingConnections(widget2, existingConnections, widget2ConnectionRequest);

        var newWidgetConnection = new WidgetConnection(
                new WidgetConnectionDetails(widget1.id(), widget1ConnectionRequest.connectionPort()),
                new WidgetConnectionDetails(widget2.id(), widget2ConnectionRequest.connectionPort())
        );
        widgetConnectionsRepository.save(newWidgetConnection);
    }

    private void validateThatWidgetsAreNotAlreadyConnected(
            Widget widget1,
            Widget widget2,
            List<WidgetConnection> existingConnections
    ) {
        var widgetIds = List.of(widget1.id(), widget2.id());
        existingConnections.stream()
                .filter(connection -> widgetIds.contains((connection.widget1().widgetId())) && widgetIds.contains(connection.widget2().widgetId()))
                .findFirst()
                .ifPresent(v -> {throw new WidgetValidationException("Widgets are already connected");});
    }

    private void validateExistingConnections(
            Widget widget,
            List<WidgetConnection> existingConnections,
            WidgetConnectionRequest connectionRequest
    ) {
        var availablePorts = new ArrayList<>(widget.connectionPorts());
        var usedPorts = getUsedPortsByWidgetId(existingConnections, widget.id());
        usedPorts.forEach(availablePorts::remove);
        if (!availablePorts.contains(connectionRequest.connectionPort())) {
            String errorMessage = String.format(
                    "Widget '%s' doesn't have available connection port '%s'", connectionRequest.serialNumber(), connectionRequest.connectionPort()
            );
            throw new WidgetValidationException(errorMessage);
        }
    }

    private List<ConnectionPort> getUsedPortsByWidgetId(List<WidgetConnection> widgetConnections, long widgetId) {
        var usedPorts = new ArrayList<ConnectionPort>();
        for (WidgetConnection widgetConnection : widgetConnections) {
            if (widgetConnection.widget1().widgetId() == widgetId) {
                usedPorts.add(widgetConnection.widget1().connectionPort());
            }
            if (widgetConnection.widget2().widgetId() == widgetId) {
                usedPorts.add(widgetConnection.widget2().connectionPort());
            }
        }

        return usedPorts;
    }

    private Widget validateAndGetWidget(List<Widget> widgets, String serialNumber, ConnectionPort connectionPort) {
        var widget = widgets.stream()
                .filter(w -> w.serialNumber().equals(serialNumber))
                .findFirst()
                .orElseThrow(() -> widgetNotFoundException(serialNumber));

        if (!widget.connectionPorts().contains(connectionPort)) {
            throw connectionPortNotSupportedException(serialNumber, connectionPort, widget.connectionPorts());
        }

        return widget;
    }

    private WidgetValidationException widgetNotFoundException(String serialNumber) {
        return new WidgetValidationException("Couldn't find widget by serial number: " + serialNumber);
    }

    private WidgetValidationException connectionPortNotSupportedException(String serialNumber, ConnectionPort connectionPort, List<ConnectionPort> supportedPorts) {
        var errorMessage = String.format(
                "Widget '%s' doesn't support connection port '%s', supported connection ports are: %s",
                serialNumber,
                connectionPort,
                supportedPorts
        );
        return new WidgetValidationException(errorMessage);
    }
}
