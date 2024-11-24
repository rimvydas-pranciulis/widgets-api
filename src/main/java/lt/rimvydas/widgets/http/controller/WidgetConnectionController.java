package lt.rimvydas.widgets.http.controller;

import jakarta.validation.Valid;
import lt.rimvydas.widgets.exception.WidgetValidationException;
import lt.rimvydas.widgets.http.model.ExternalWidgetConnection;
import lt.rimvydas.widgets.http.model.ExternalWidgetConnectionDetails;
import lt.rimvydas.widgets.model.ConnectionPort;
import lt.rimvydas.widgets.model.WidgetConnectionRequest;
import lt.rimvydas.widgets.service.WidgetService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("v1/widget-connections")
public class WidgetConnectionController {

    private final WidgetService widgetService;

    public WidgetConnectionController(WidgetService widgetService) {
        this.widgetService = widgetService;
    }

    @PostMapping
    public ResponseEntity<String> getWidgetConnection(@Valid @RequestBody ExternalWidgetConnection widgetConnection) {
        try {
            widgetService.connectWidgets(
                    toWidgetConnectionRequest(widgetConnection.widget1()),
                    toWidgetConnectionRequest(widgetConnection.widget2())
            );
            return ResponseEntity.ok("OK");
        } catch (WidgetValidationException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private WidgetConnectionRequest toWidgetConnectionRequest(ExternalWidgetConnectionDetails widgetConnectionDetails) {
        return new WidgetConnectionRequest(widgetConnectionDetails.serialNumber(), ConnectionPort.valueOf(widgetConnectionDetails.connectionPort().name()));
    }
}
