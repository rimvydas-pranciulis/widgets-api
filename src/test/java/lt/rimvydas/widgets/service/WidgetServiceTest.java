package lt.rimvydas.widgets.service;

import lt.rimvydas.widgets.exception.WidgetValidationException;
import lt.rimvydas.widgets.model.*;
import lt.rimvydas.widgets.repository.WidgetConnectionRepository;
import lt.rimvydas.widgets.repository.WidgetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WidgetServiceTest {

    @Mock
    private WidgetRepository widgetRepository;

    @Mock
    private WidgetConnectionRepository widgetConnectionRepository;

    @InjectMocks
    private WidgetService service;

    @Captor
    private ArgumentCaptor<WidgetConnection> widgetConnectionArgumentCaptor;

    @Test
    void connectWidgets_throwsValidationException_whenUsingSameSerialNumber() {
        // given
        var widget1ConnectionRequest = new WidgetConnectionRequest("serial1", ConnectionPort.P);
        var widget2ConnectionRequest = new WidgetConnectionRequest("serial1", ConnectionPort.Q);

        // when
        var exception = assertThrows(
                WidgetValidationException.class,
                () -> service.connectWidgets(widget1ConnectionRequest, widget2ConnectionRequest)
        );

        // then
        assertEquals("Serial numbers has to be different", exception.getMessage());
        verifyNoInteractions(widgetConnectionRepository);
    }

    @Test
    void connectWidgets_throwsValidationException_whenWidget1DoesNotExist() {
        // given
        var widget1ConnectionRequest = new WidgetConnectionRequest("serial1", ConnectionPort.P);
        var widget2ConnectionRequest = new WidgetConnectionRequest("serial2", ConnectionPort.Q);

        // when
        var exception = assertThrows(
                WidgetValidationException.class,
                () -> service.connectWidgets(widget1ConnectionRequest, widget2ConnectionRequest)
        );

        // then
        assertEquals("Couldn't find widget by serial number: serial1", exception.getMessage());
        verifyNoInteractions(widgetConnectionRepository);
    }

    @Test
    void connectWidgets_throwsValidationException_whenWidget2DoesNotExist() {
        // given
        var widget1Serial = "serial1";
        var widget2Serial = "serial2";
        var widget1Port = ConnectionPort.P;
        var widget2Port = ConnectionPort.Q;
        var widget1ConnectionRequest = new WidgetConnectionRequest(widget1Serial, widget1Port);
        var widget2ConnectionRequest = new WidgetConnectionRequest(widget2Serial, widget2Port);
        when(widgetRepository.findBySerialNumbers(Set.of(widget1Serial, widget2Serial))).thenReturn(
                List.of(new Widget(1L, "name", widget1Serial, List.of(widget1Port)))
        );

        // when
        var exception = assertThrows(
                WidgetValidationException.class,
                () -> service.connectWidgets(widget1ConnectionRequest, widget2ConnectionRequest)
        );

        // then
        assertEquals("Couldn't find widget by serial number: serial2", exception.getMessage());
        verifyNoInteractions(widgetConnectionRepository);
    }

    @Test
    void connectWidgets_throwsValidationException_whenWidget1DoesSupportConnectionPort() {
        // given
        var widget1Serial = "serial1";
        var widget2Serial = "serial2";
        var widget1ConnectionRequest = new WidgetConnectionRequest(widget1Serial, ConnectionPort.P);
        var widget2ConnectionRequest = new WidgetConnectionRequest(widget2Serial, ConnectionPort.Q);
        when(widgetRepository.findBySerialNumbers(Set.of(widget1Serial, widget2Serial))).thenReturn(
                List.of(new Widget(1L, "name", widget1Serial, List.of(ConnectionPort.Q, ConnectionPort.R)))
        );

        // when
        var exception = assertThrows(
                WidgetValidationException.class,
                () -> service.connectWidgets(widget1ConnectionRequest, widget2ConnectionRequest)
        );

        // then
        assertEquals("Widget 'serial1' doesn't support connection port 'P', supported connection ports are: [Q, R]", exception.getMessage());
        verifyNoInteractions(widgetConnectionRepository);
    }

    @Test
    void connectWidgets_throwsValidationException_whenWidget2DoesSupportConnectionPort() {
        // given
        var widget1Serial = "serial1";
        var widget2Serial = "serial2";
        var widget1ConnectionRequest = new WidgetConnectionRequest(widget1Serial, ConnectionPort.P);
        var widget2ConnectionRequest = new WidgetConnectionRequest(widget2Serial, ConnectionPort.Q);
        when(widgetRepository.findBySerialNumbers(Set.of(widget1Serial, widget2Serial))).thenReturn((List.of(
                new Widget(1L, "name", widget1Serial, List.of(ConnectionPort.P)),
                new Widget(2L, "name", widget2Serial, List.of(ConnectionPort.P, ConnectionPort.R))
        )));

        // when
        var exception = assertThrows(
                WidgetValidationException.class,
                () -> service.connectWidgets(widget1ConnectionRequest, widget2ConnectionRequest)
        );

        // then
        assertEquals("Widget 'serial2' doesn't support connection port 'Q', supported connection ports are: [P, R]", exception.getMessage());
        verifyNoInteractions(widgetConnectionRepository);
    }

    @Test
    void connectWidgets_createsConnection_whenNoExistingConnections() {
        // given
        var widget1Serial = "serial1";
        var widget2Serial = "serial2";
        var widget1Port = ConnectionPort.P;
        var widget2Port = ConnectionPort.Q;
        var widget1Id = 1L;
        var widget2Id = 2L;
        var widget1ConnectionRequest = new WidgetConnectionRequest(widget1Serial, widget1Port);
        var widget2ConnectionRequest = new WidgetConnectionRequest(widget2Serial, widget2Port);
        when(widgetRepository.findBySerialNumbers(Set.of(widget1Serial, widget2Serial))).thenReturn(List.of(
                new Widget(widget1Id, "name", widget1Serial, List.of(widget1Port)),
                new Widget(widget2Id, "name", widget2Serial, List.of(widget2Port))
        ));

        // when
        service.connectWidgets(widget1ConnectionRequest, widget2ConnectionRequest);

        // then
        verify(widgetConnectionRepository).save(widgetConnectionArgumentCaptor.capture());
        var widgetConnection = widgetConnectionArgumentCaptor.getValue();
        assertEquals(widget1Id, widgetConnection.widget1().widgetId());
        assertEquals(widget2Id, widgetConnection.widget2().widgetId());
        assertEquals(widget1Port, widgetConnection.widget1().connectionPort());
        assertEquals(widget2Port, widgetConnection.widget2().connectionPort());
    }

    @Test
    void connectWidgets_throwsValidationException_whenConnectionBetweenWidgetsAlreadyExists() {
        // given
        var widget1Serial = "serial1";
        var widget2Serial = "serial2";
        var widget1Port = ConnectionPort.P;
        var widget2Port = ConnectionPort.Q;
        var widget1Id = 1L;
        var widget2Id = 2L;
        var widget1ConnectionRequest = new WidgetConnectionRequest(widget1Serial, widget1Port);
        var widget2ConnectionRequest = new WidgetConnectionRequest(widget2Serial, widget2Port);
        when(widgetRepository.findBySerialNumbers(Set.of(widget1Serial, widget2Serial))).thenReturn(List.of(
                new Widget(1L, "name", widget1Serial, List.of(widget1Port)),
                new Widget(2L, "name", widget2Serial, List.of(widget2Port))
        ));
        when(widgetConnectionRepository.findWidgetConnections(Set.of(widget1Id, widget2Id))).thenReturn(List.of(new WidgetConnection(
                        new WidgetConnectionDetails(widget2Id, ConnectionPort.P),
                        new WidgetConnectionDetails(widget1Id, ConnectionPort.P)
                )));

        // when
        var exception = assertThrows(
                WidgetValidationException.class,
                () -> service.connectWidgets(widget1ConnectionRequest, widget2ConnectionRequest)
        );

        // then
        assertEquals("Widgets are already connected", exception.getMessage());
        verifyNoMoreInteractions(widgetConnectionRepository);
    }

    @Test
    void connectWidgets_throwsValidationException_whenWidget1ConnectionPortIsUsed() {
        // given
        var widget1Serial = "serial1";
        var widget2Serial = "serial2";
        var widget1Port = ConnectionPort.P;
        var widget2Port = ConnectionPort.Q;
        var widget1Id = 1L;
        var widget2Id = 2L;
        var widget1ConnectionRequest = new WidgetConnectionRequest(widget1Serial, widget1Port);
        var widget2ConnectionRequest = new WidgetConnectionRequest(widget2Serial, widget2Port);
        when(widgetRepository.findBySerialNumbers(Set.of(widget1Serial, widget2Serial))).thenReturn(List.of(
                new Widget(1L, "name", widget1Serial, List.of(widget1Port)),
                new Widget(2L, "name", widget2Serial, List.of(widget2Port))
        ));
        when(widgetConnectionRepository.findWidgetConnections(Set.of(widget1Id, widget2Id))).thenReturn(List.of(new WidgetConnection(
                new WidgetConnectionDetails(widget1Id, widget1Port),
                new WidgetConnectionDetails(3L, ConnectionPort.P)
        )));

        // when
        var exception = assertThrows(
                WidgetValidationException.class,
                () -> service.connectWidgets(widget1ConnectionRequest, widget2ConnectionRequest)
        );

        // then
        assertEquals("Widget 'serial1' doesn't have available connection port 'P'", exception.getMessage());
        verifyNoMoreInteractions(widgetConnectionRepository);
    }

    @Test
    void connectWidgets_throwsValidationException_whenWidget2ConnectionPortIsUsed() {
        // given
        var widget1Serial = "serial1";
        var widget2Serial = "serial2";
        var widget1Port = ConnectionPort.P;
        var widget2Port = ConnectionPort.Q;
        var widget1Id = 1L;
        var widget2Id = 2L;
        var widget1ConnectionRequest = new WidgetConnectionRequest(widget1Serial, widget1Port);
        var widget2ConnectionRequest = new WidgetConnectionRequest(widget2Serial, widget2Port);
        when(widgetRepository.findBySerialNumbers(Set.of(widget1Serial, widget2Serial))).thenReturn(List.of(
                new Widget(1L, "name", widget1Serial, List.of(widget1Port)),
                new Widget(2L, "name", widget2Serial, List.of(widget2Port))
        ));
        when(widgetConnectionRepository.findWidgetConnections(Set.of(widget1Id, widget2Id))).thenReturn(List.of(new WidgetConnection(
                new WidgetConnectionDetails(3L, ConnectionPort.R),
                new WidgetConnectionDetails(widget2Id, widget2Port)
        )));

        // when
        var exception = assertThrows(
                WidgetValidationException.class,
                () -> service.connectWidgets(widget1ConnectionRequest, widget2ConnectionRequest)
        );

        // then
        assertEquals("Widget 'serial2' doesn't have available connection port 'Q'", exception.getMessage());
        verifyNoMoreInteractions(widgetConnectionRepository);
    }

    @Test
    void connectWidgets_createsConnection_whenWidget1HasTwoSameTypePortsAvailable() {
        // given
        var widget1Serial = "serial1";
        var widget2Serial = "serial2";
        var widget1Port = ConnectionPort.P;
        var widget2Port = ConnectionPort.Q;
        var widget1Id = 1L;
        var widget2Id = 2L;
        var widget1ConnectionRequest = new WidgetConnectionRequest(widget1Serial, widget1Port);
        var widget2ConnectionRequest = new WidgetConnectionRequest(widget2Serial, widget2Port);
        when(widgetRepository.findBySerialNumbers(Set.of(widget1Serial, widget2Serial))).thenReturn(List.of(
                new Widget(1L, "name", widget1Serial, List.of(widget1Port, widget1Port)),
                new Widget(2L, "name", widget2Serial, List.of(widget2Port))
        ));
        when(widgetConnectionRepository.findWidgetConnections(Set.of(widget1Id, widget2Id))).thenReturn(List.of(new WidgetConnection(
                new WidgetConnectionDetails(widget1Id, widget1Port),
                new WidgetConnectionDetails(3L, ConnectionPort.P)
        )));

        // when
        service.connectWidgets(widget1ConnectionRequest, widget2ConnectionRequest);

        // then
        verify(widgetConnectionRepository).save(widgetConnectionArgumentCaptor.capture());
        var widgetConnection = widgetConnectionArgumentCaptor.getValue();
        assertEquals(widget1Id, widgetConnection.widget1().widgetId());
        assertEquals(widget2Id, widgetConnection.widget2().widgetId());
        assertEquals(widget1Port, widgetConnection.widget1().connectionPort());
        assertEquals(widget2Port, widgetConnection.widget2().connectionPort());
    }
}