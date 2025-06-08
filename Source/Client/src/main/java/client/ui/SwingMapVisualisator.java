package client.ui;

import client.gamedata.GameInfo;
import client.map.ClientFullMap;
import client.map.Coordinate;
import client.map.EGameTerrain;
import client.map.Field;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.Set;

public class SwingMapVisualisator {
    private JFrame frame;
    private JPanel gridPanel;
    private JLabel[][] cellLabels;
    private int width, height;

    private SwingGameInfoVisualisator infoPanel;
    private GameInfo gameInfo;

    private Coordinate treasureCoordinate = null;

    public SwingMapVisualisator(int width, int height, GameInfo gameInfoModel) {
        this.width = width;
        this.height = height;
        this.gameInfo = gameInfo;

        frame = new JFrame("Game Map Visualisation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());

        gridPanel = new JPanel(new GridBagLayout());
        cellLabels = new JLabel[height][width];

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(1, 1, 1, 1);

        for (int x = 0; x <= width; x++) {
            gbc.gridx = x;
            gbc.gridy = 0;
            JLabel label;
            if (x == 0) {
                label = new JLabel("  ");
            } else {
                label = new JLabel(String.valueOf(x - 1), SwingConstants.CENTER);
                label.setFont(new Font("Monospaced", Font.BOLD, 16));
            }
            gridPanel.add(label, gbc);
        }

        for (int y = 0; y < height; y++) {
            gbc.gridx = 0;
            gbc.gridy = y + 1;
            JLabel rowLabel = new JLabel(String.valueOf(y), SwingConstants.CENTER);
            rowLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
            gridPanel.add(rowLabel, gbc);

            for (int x = 0; x < width; x++) {
                gbc.gridx = x + 1;
                gbc.gridy = y + 1;
                JLabel cell = new JLabel(" ", SwingConstants.CENTER);
                cell.setPreferredSize(new Dimension(36, 36));
                cell.setFont(new Font("Monospaced", Font.PLAIN, 26));
                cell.setOpaque(true);
                cell.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                cellLabels[y][x] = cell;
                gridPanel.add(cell, gbc);
            }
        }

        // Info Panel
        infoPanel = new SwingGameInfoVisualisator(gameInfoModel);
        infoPanel.setBorder(BorderFactory.createTitledBorder("Game Info"));

        mainPanel.add(gridPanel, BorderLayout.CENTER);
        mainPanel.add(infoPanel, BorderLayout.SOUTH);

        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void updateMap(
            ClientFullMap map,
            Set<Coordinate> discovered,
            Coordinate myPos,
            Coordinate enemyPos
    ) {
        Map<Coordinate, Field> allFields = map.getAllFields();

        if (treasureCoordinate == null) {
            for (Coordinate coord : discovered) {
                Field field = allFields.get(coord);
                if (field != null && field.isTreasurePresent()) {
                    treasureCoordinate = coord;
                    break;
                }
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Coordinate coord = new Coordinate(x, y);
                JLabel cell = cellLabels[y][x];
                Field field = allFields.get(coord);

                if (coord.equals(myPos) && coord.equals(enemyPos)) {
                    cell.setText("⚔️");
                } else if (coord.equals(myPos) && treasureCoordinate != null && coord.equals(treasureCoordinate)) {
                    cell.setText("💰");
                } else if (coord.equals(myPos)) {
                    cell.setText("🧑");
                } else if (coord.equals(enemyPos)) {
                    cell.setText("😈");
                } else if (coord.equals(treasureCoordinate)) {
                    cell.setText("💰");
                } else if (discovered.contains(coord) && field != null && field.isFortPresent()) {
                    cell.setText("🏰");
                } else if (field != null) {
                    cell.setText(getTerrainEmoji(field.getTerrainType(), discovered.contains(coord)));
                } else {
                    cell.setText(" ");
                }

                // Background color by terrain
                if (field == null) {
                    cell.setBackground(Color.WHITE);
                } else if (field.getTerrainType() == EGameTerrain.WATER) {
                    cell.setBackground(new Color(135, 180, 250));
                } else if (field.getTerrainType() == EGameTerrain.GRASS) {
                    cell.setBackground(discovered.contains(coord)
                            ? new Color(170, 255, 170)
                            : new Color(110, 200, 110));
                } else if (field.getTerrainType() == EGameTerrain.MOUNTAIN) {
                    cell.setBackground(new Color(200, 170, 110));
                }
            }
        }
        frame.repaint();
    }

    private String getTerrainEmoji(EGameTerrain terrain, boolean discovered) {
        return switch (terrain) {
            case GRASS -> discovered ? "🟢" : "🟩";
            case MOUNTAIN -> discovered ? "🟤" : "🟫";
            case WATER -> "🟦";
        };
    }
}
