package lt.rimvydas.widgets.http.model;

import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public record ExternalWidget(

        @Length(min = 1, max = 250)
        String name,
        @Length(min = 1, max = 250)
        String serialNumber,
        @Size(min = 1, max = 3)
        List<ExternalConnectionPort> connectionPorts
) {

}
