package lt.rimvydas.widgets.http.model;

import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public record ExternalWidget(

        @Length(min = 1, max = 255)
        String name,
        @Length(min = 1, max = 255)
        String serialNumber,
        @Size(min = 1, max = 3)
        List<String> connectionPorts // TODO: enum?
) {

}
