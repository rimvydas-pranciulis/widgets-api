package lt.rimvydas.widgets.http.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lt.rimvydas.widgets.http.model.ExternalWidget;
import lt.rimvydas.widgets.service.WidgetService;
import lt.rimvydas.widgets.model.ConnectionPort;
import lt.rimvydas.widgets.model.Widget;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO: versioning
@RestController
@RequestMapping("widgets")
public class WidgetController {

    private final WidgetService widgetService;

    public WidgetController(WidgetService widgetService) {
        this.widgetService = widgetService;
    }

    @PostMapping
    public String createWidget(@Valid @RequestBody ExternalWidget widget) {
        widgetService.createWidget(toInternalWidget(widget));
        return "OK";
    }

    @DeleteMapping("{serialNumber}")
    public void deleteWidget(@PathVariable @NotEmpty String serialNumber) {
        // TODO: delete connections
        widgetService.deleteWidget(serialNumber);
    }

    private Widget toInternalWidget(ExternalWidget widget) {
        var connectionPorts = widget.connectionPorts().stream()
                .map(v -> ConnectionPort.valueOf(v.name()))
                .toList();
        return new Widget(null, widget.name(), widget.serialNumber(), connectionPorts);
    }
}
