package com.coolioasjulio.chess.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

public class ChessAxisLabel extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final String letters = "abcdefgh";

    public enum Axis {
        Vertical, Horizontal
    }

    private Axis axis;
    private Color background, textColor;

    public ChessAxisLabel(Axis axis, int tileSize, Color background, Color textColor) {
        this.axis = axis;
        this.background = background;
        this.textColor = textColor;
        switch (axis) {
            case Vertical:
                this.setPreferredSize(new Dimension(tileSize / 4, tileSize * 8));
                break;

            case Horizontal:
                this.setPreferredSize(new Dimension(tileSize * 8, tileSize / 4));
                break;
        }
    }
    
    public void setTileSize(int tileSize) {
        switch (axis) {
            case Vertical:
                this.setPreferredSize(new Dimension(tileSize / 4, tileSize * 8));
                break;

            case Horizontal:
                this.setPreferredSize(new Dimension(tileSize * 8, tileSize / 4));
                break;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(background);
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(textColor);
        for (int i = 0; i < 8; i++) {
            int x, y;
            int tileSize;
            switch (axis) {
                case Vertical:
                    tileSize = getHeight() / 8;
                    x = getWidth() / 2;
                    y = getHeight() - (tileSize * i + tileSize / 2);
                    g.drawString(String.valueOf(i + 1), x, y);
                    break;

                case Horizontal:
                    tileSize = getWidth() / 8;
                    x = tileSize * i + tileSize / 2;
                    y = getHeight() / 2;
                    g.drawString(String.valueOf(letters.charAt(i)), x, y);
                    break;
            }
        }
    }
}
