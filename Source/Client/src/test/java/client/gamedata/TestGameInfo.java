package client.gamedata;

import org.junit.jupiter.api.Test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class TestGameInfo {

    @Test
    void givenGameInfo_whenPhaseIsSet_thenListenerIsNotified() {
        GameInfo info = new GameInfo();
        AtomicBoolean notified = new AtomicBoolean(false);

        info.addPropertyChangeListener(evt -> {
            if ("phase".equals(evt.getPropertyName()) && "Battle".equals(evt.getNewValue())) {
                notified.set(true);
            }
        });

        info.setPhase("Battle");
        assertTrue(notified.get());
        assertEquals("Battle", info.getPhase());
    }

    @Test
    void givenGameInfo_whenStatusIsSet_thenListenerIsNotified() {
        GameInfo info = new GameInfo();
        AtomicBoolean notified = new AtomicBoolean(false);

        info.addPropertyChangeListener(evt -> {
            if ("status".equals(evt.getPropertyName()) && "WIN".equals(evt.getNewValue())) {
                notified.set(true);
            }
        });

        info.setStatus("WIN");
        assertTrue(notified.get());
        assertEquals("WIN", info.getStatus());
    }

    @Test
    void givenGameInfo_whenPrintGameInfoCLI_thenNoExceptionIsThrown() {
        GameInfo info = new GameInfo();
        info.setPhase("Phase");
        info.setTurn(42);
        info.setMove("UP");
        info.setMyPosition("(1,2)");
        info.setEnemyPosition("(2,3)");
        info.setTreasureFound("(4,4)");
        info.setStatus("WIN");

        assertDoesNotThrow(info::printGameInfoCLI);
    }
}
