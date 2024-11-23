package lt.rimvydas.widgets.http;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lt.rimvydas.widgets.http.model.ExternalWidget;
import lt.rimvydas.widgets.service.WidgetsService;
import lt.rimvydas.widgets.service.model.ConnectionPort;
import lt.rimvydas.widgets.service.model.Widget;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

// TODO: versioning
@RestController
@RequestMapping("widgets")
public class WidgetsController {

    public static final List<String> ALLOWED_CONNECTION_PORTS = List.of("P", "R", "Q");

    private final WidgetsService widgetsService;

    public WidgetsController(WidgetsService widgetsService) {
        this.widgetsService = widgetsService;
    }

    // TODO: remove
    @GetMapping("test")
    public void test() {
        widgetsService.test();
    }

    @PostMapping
    public ExternalWidget createWidget(@Valid @RequestBody ExternalWidget widget) {
        validateConnectionPorts(widget);
        if (widgetsService.getWidget(widget.serialNumber()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("Widget with serial number %s already exists", widget.serialNumber()));
        }

        var createdWidget = widgetsService.createWidget(toInternalWidget(widget));
        return toExternalWidget(createdWidget);
    }

    @DeleteMapping("{serialNumber}")
    public void deleteWidget(@PathVariable @NotEmpty String serialNumber) {
        // TODO: delete connections?
        widgetsService.deleteWidget(serialNumber);
    }

    private void validateConnectionPorts(ExternalWidget widget) {
        if (!widget.connectionPorts().stream().allMatch(ALLOWED_CONNECTION_PORTS::contains)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid connection port provided, supported values: " + ALLOWED_CONNECTION_PORTS);
        }
    }

    private Widget toInternalWidget(ExternalWidget widget) {
        var connectionPorts = widget.connectionPorts().stream()
                .map(ConnectionPort::valueOf)
                .toList();
        return new Widget(null, widget.name(), widget.serialNumber(), connectionPorts);
    }

    private ExternalWidget toExternalWidget(Widget widget) {
        var connectionPorts = widget.connectionPorts().stream()
                .map(ConnectionPort::name)
                .toList();
        return new ExternalWidget(widget.name(), widget.serialNumber(), connectionPorts);
    }
}
