package client.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TestPlayerRegistrationException {

    @Test
    void givenMessage_whenConstructingPlayerRegistrationException_thenMessageIsSet() {
        PlayerRegistrationException ex = new PlayerRegistrationException("msg");
        assertEquals("msg", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void givenMessageAndCause_whenConstructingPlayerRegistrationException_thenCauseIsSet() {
        Throwable cause = new RuntimeException();
        PlayerRegistrationException ex = new PlayerRegistrationException("fail", cause);
        assertEquals("fail", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
}
