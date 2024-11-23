package lt.rimvydas.widgets.http;

import jakarta.validation.Valid;
import lt.rimvydas.widgets.http.model.ExternalWidgetConnection;
import lt.rimvydas.widgets.service.WidgetsService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static lt.rimvydas.widgets.http.WidgetsController.ALLOWED_CONNECTION_PORTS;

@RestController
@RequestMapping("widget-connections")
public class WidgetConnectionsController {

    private WidgetsService widgetsService;

    @PostMapping
    public void getWidgetConnection(@Valid @RequestBody ExternalWidgetConnection widgetConnection) {
        validateWidgetConnection(widgetConnection);
    }

    private void validateWidgetConnection(ExternalWidgetConnection widgetConnection) {
        if (widgetConnection.widget1().serialNumber().equals(widgetConnection.widget2().serialNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Serial numbers has to be different");
        }

        if (isValidConnectionPort(widgetConnection.widget1().connectionPort())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid connection port provided on widget1, allowed values: " + ALLOWED_CONNECTION_PORTS);
        }

        if (isValidConnectionPort(widgetConnection.widget2().connectionPort())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid connection port provided on widget2, allowed values: " + ALLOWED_CONNECTION_PORTS);
        }
    }

    private boolean isValidConnectionPort(String connectionPort) {
        return ALLOWED_CONNECTION_PORTS.contains(connectionPort);
    }
}
