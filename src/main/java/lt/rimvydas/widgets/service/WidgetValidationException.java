package lt.rimvydas.widgets.service;

// TODO: map exception to BAD_REQUEST
public class WidgetValidationException extends RuntimeException {

    public WidgetValidationException(String message) {
        super(message);
    }
}
