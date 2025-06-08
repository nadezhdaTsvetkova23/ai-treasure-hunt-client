package client.ui;

import client.gamedata.TechnicalInfo;

public class TechnicalInfoVisualisator {
    public TechnicalInfoVisualisator(TechnicalInfo model) {
        model.addPropertyChangeListener(evt -> {
            if ("technicalInfo".equals(evt.getPropertyName())) {
                displayTechnicalInfo((String) evt.getNewValue());
            }
        });
    }

    private void displayTechnicalInfo(String msg) {
        System.err.println("[Technical/Validation Error] " + msg);
    }
}
