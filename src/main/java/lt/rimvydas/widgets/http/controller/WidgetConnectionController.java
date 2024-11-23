package lt.rimvydas.widgets.http.controller;

import jakarta.validation.Valid;
import lt.rimvydas.widgets.http.model.ExternalWidgetConnection;
import lt.rimvydas.widgets.http.model.ExternalWidgetConnectionDetails;
import lt.rimvydas.widgets.model.ConnectionPort;
import lt.rimvydas.widgets.model.WidgetConnectionRequest;
import lt.rimvydas.widgets.service.WidgetService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("widget-connections")
public class WidgetConnectionController {

    private WidgetService widgetService;

    @PostMapping
    public String getWidgetConnection(@Valid @RequestBody ExternalWidgetConnection widgetConnection) {
        widgetService.connectWidgets(
                toWidgetConnectionRequest(widgetConnection.widget1()),
                toWidgetConnectionRequest(widgetConnection.widget2())
        );
        return "OK";
    }

    private WidgetConnectionRequest toWidgetConnectionRequest(ExternalWidgetConnectionDetails widgetConnectionDetails) {
        return new WidgetConnectionRequest(widgetConnectionDetails.serialNumber(), ConnectionPort.valueOf(widgetConnectionDetails.connectionPort().name()));
    }
}
