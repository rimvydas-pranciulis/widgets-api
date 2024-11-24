package lt.rimvydas.widgets.service;

import lt.rimvydas.widgets.exception.WidgetValidationException;
import lt.rimvydas.widgets.model.ConnectionPort;
import lt.rimvydas.widgets.model.Widget;
import lt.rimvydas.widgets.model.WidgetConnection;
import lt.rimvydas.widgets.model.WidgetConnectionDetails;
import lt.rimvydas.widgets.model.WidgetConnectionRequest;
import lt.rimvydas.widgets.repository.WidgetConnectionRepository;
import lt.rimvydas.widgets.repository.WidgetRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class WidgetService {

    private final WidgetRepository widgetRepository;
    private final WidgetConnectionRepository widgetConnectionRepository;

    public WidgetService(WidgetRepository widgetRepository, WidgetConnectionRepository widgetConnectionRepository) {
        this.widgetRepository = widgetRepository;
        this.widgetConnectionRepository = widgetConnectionRepository;
    }

    public void createWidget(Widget widget) {
        if (widgetRepository.findBySerialNumber(widget.serialNumber()).isPresent()) {
            throw new WidgetValidationException("Widget with serial number already exists: " + widget.serialNumber());
        }
        widgetRepository.save(widget);
    }

    public void deleteWidget(String serialNumber) {
        var widgetId = widgetRepository.findBySerialNumber(serialNumber)
                .map(Widget::id)
                .orElseThrow(() -> new WidgetValidationException("Widget with serial number not found: " + serialNumber));

        if (!widgetConnectionRepository.findWidgetConnections(Set.of(widgetId)).isEmpty()) {
            throw new WidgetValidationException("Can't delete widget, because it has connections to other widgets");
        }

        widgetRepository.deleteBySerialNumber(serialNumber);
    }

    public void connectWidgets(WidgetConnectionRequest widget1ConnectionRequest, WidgetConnectionRequest widget2ConnectionRequest) {
        if (widget1ConnectionRequest.serialNumber().equals(widget2ConnectionRequest.serialNumber())) {
            throw new WidgetValidationException("Serial numbers has to be different");
        }

        var widgets = widgetRepository.findBySerialNumbers(Set.of(widget1ConnectionRequest.serialNumber(), widget2ConnectionRequest.serialNumber()));
        var widget1 = validateAndGetWidget(widgets, widget1ConnectionRequest.serialNumber(), widget1ConnectionRequest.connectionPort());
        var widget2 = validateAndGetWidget(widgets, widget2ConnectionRequest.serialNumber(), widget2ConnectionRequest.connectionPort());

        var existingConnections = widgetConnectionRepository.findWidgetConnections(Set.of(widget1.id(), widget2.id()));
        validateThatWidgetsAreNotAlreadyConnected(widget1, widget2, existingConnections);
        validateExistingConnections(widget1, existingConnections, widget1ConnectionRequest);
        validateExistingConnections(widget2, existingConnections, widget2ConnectionRequest);

        var newWidgetConnection = new WidgetConnection(
                new WidgetConnectionDetails(widget1.id(), widget1ConnectionRequest.connectionPort()),
                new WidgetConnectionDetails(widget2.id(), widget2ConnectionRequest.connectionPort())
        );
        widgetConnectionRepository.save(newWidgetConnection);
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

    private void validateThatWidgetsAreNotAlreadyConnected(Widget widget1, Widget widget2, List<WidgetConnection> existingConnections) {
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
}
