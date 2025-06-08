package client.ui;

import client.gamedata.GameInfo;

import javax.swing.*;
import java.awt.*;

public class SwingGameInfoVisualisator extends JPanel {
    private final JLabel phaseLabel = new JLabel();
    private final JLabel turnLabel = new JLabel();
    private final JLabel moveLabel = new JLabel();
    private final JLabel myPosLabel = new JLabel();
    private final JLabel enemyPosLabel = new JLabel();
    private final JLabel treasureLabel = new JLabel();
    private final JLabel statusLabel = new JLabel();

    public SwingGameInfoVisualisator(GameInfo info) {
        setLayout(new GridLayout(0, 1));
        setBorder(BorderFactory.createTitledBorder("Game Info"));

        add(phaseLabel);
        add(turnLabel);
        add(moveLabel);
        add(myPosLabel);
        add(enemyPosLabel);
        add(treasureLabel);
        add(statusLabel);

        updateLabels(info);

        info.addPropertyChangeListener(evt -> SwingUtilities.invokeLater(() -> updateLabels(info)));
    }

    private void updateLabels(GameInfo info) {
        phaseLabel.setText("Phase: " + info.getPhase());
        turnLabel.setText("Turn: " + info.getTurn());
        moveLabel.setText("Move: " + info.getMove());
        myPosLabel.setText("My position: " + info.getMyPosition());
        enemyPosLabel.setText("Enemy position: " + info.getEnemyPosition());
        treasureLabel.setText("Treasure found on position: " + info.getTreasureFound());
        statusLabel.setText(info.getStatus());
    }
}
