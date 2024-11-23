package lt.rimvydas.widgets.http.model;

import jakarta.validation.constraints.NotEmpty;

public record ExternalWidgetConnectionDetails(
        @NotEmpty String serialNumber,
        @NotEmpty String connectionPort
) {

}
