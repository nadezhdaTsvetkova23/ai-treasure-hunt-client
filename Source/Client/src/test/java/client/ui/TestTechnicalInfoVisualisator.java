package client.ui;

import client.gamedata.TechnicalInfo;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class TestTechnicalInfoVisualisator {

    @Test
    void givenTechnicalInfoChanged_whenDisplayIsCalled_thenOutputContainsMessage() {
        
        TechnicalInfo info = new TechnicalInfo();
        TechnicalInfoVisualisator visualisator = new TechnicalInfoVisualisator(info);

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        info.setLastMessage("Test error!");

        System.setErr(originalErr); 
        assertTrue(errContent.toString().contains("Test error!"));
    }
}
