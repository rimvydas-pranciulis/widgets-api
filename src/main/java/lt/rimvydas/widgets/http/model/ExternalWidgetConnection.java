package lt.rimvydas.widgets.http.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ExternalWidgetConnection(
        @NotNull @Valid
        ExternalWidgetConnectionDetails widget1,
        @NotNull @Valid
        ExternalWidgetConnectionDetails widget2
) {

}