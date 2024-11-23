package lt.rimvydas.widgets.http.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ExternalWidgetConnectionDetails(
        @NotEmpty String serialNumber,
        @NotNull ExternalConnectionPort connectionPort
) {

}
