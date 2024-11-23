package lt.rimvydas.widgets.http.model;

import jakarta.validation.constraints.NotNull;

public record ExternalWidgetConnection(
        @NotNull ExternalWidgetConnectionDetails widget1,
        @NotNull ExternalWidgetConnectionDetails widget2
) {

}