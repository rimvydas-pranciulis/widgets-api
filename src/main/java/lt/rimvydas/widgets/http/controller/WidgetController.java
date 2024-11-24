package lt.rimvydas.widgets.http.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lt.rimvydas.widgets.exception.WidgetValidationException;
import lt.rimvydas.widgets.http.model.ExternalWidget;
import lt.rimvydas.widgets.service.WidgetService;
import lt.rimvydas.widgets.model.ConnectionPort;
import lt.rimvydas.widgets.model.Widget;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("v1/widgets")
public class WidgetController {

    private final WidgetService widgetService;

    public WidgetController(WidgetService widgetService) {
        this.widgetService = widgetService;
    }

    @PostMapping
    public ResponseEntity<String> createWidget(@Valid @RequestBody ExternalWidget widget) {
        try {
            widgetService.createWidget(toInternalWidget(widget));
            return ResponseEntity.ok("OK");
        } catch (WidgetValidationException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("{serialNumber}")
    public ResponseEntity<String> deleteWidget(@PathVariable @NotEmpty String serialNumber) {
        try {
            widgetService.deleteWidget(serialNumber);
            return ResponseEntity.ok("OK");
        } catch (WidgetValidationException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private Widget toInternalWidget(ExternalWidget widget) {
        var connectionPorts = widget.connectionPorts().stream()
                .map(v -> ConnectionPort.valueOf(v.name()))
                .toList();
        return new Widget(null, widget.name(), widget.serialNumber(), connectionPorts);
    }
}
