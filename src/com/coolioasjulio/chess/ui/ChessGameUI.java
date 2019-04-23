package com.coolioasjulio.chess.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.coolioasjulio.chess.ChessGame;
import com.coolioasjulio.chess.Square;
import com.coolioasjulio.chess.pieces.Piece;

public class ChessGameUI extends ChessGame {

    private ChessGamePanel panel;
    private Color light, dark;
    private JLabel label;
    private List<Piece> toDraw;

    public ChessGameUI(int tileSize, Color light, Color dark) {
        super(tileSize);
        this.light = light;
        this.dark = dark;
        panel = new ChessGamePanel();
    }

    @Override
    public void onTurnStarted(int team) {
        if (label != null) {
            label.setText("Current move: " + (team == Piece.WHITE ? "white" : "black"));
        }
    }

    @Override
    public void onTurnEnded(int team, boolean check) {
        if (check) {
            JOptionPane.showMessageDialog(panel, "Check!", "Message", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Override
    public void setTileSize(int tileSize) {
        super.setTileSize(tileSize);
        panel.setPreferredSize(new Dimension(8 * tileSize, 8 * tileSize));
        PieceImage.clearCachedImages();
    }

    public void setTurnIndicator(JLabel label) {
        this.label = label;
    }

    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void draw(List<Piece> toDraw) {
        this.toDraw = toDraw;
        panel.repaint();
    }

    private class ChessGamePanel extends JPanel {
        private static final long serialVersionUID = 1L;

        public ChessGamePanel() {
            this.setPreferredSize(new Dimension(tileSize * 8, tileSize * 8));
        }

        private void drawBoard(Graphics g) {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    int x = i * tileSize;
                    int y = j * tileSize;
                    g.setColor(dark);
                    if ((i + j) % 2 == 0)
                        g.setColor(light);
                    g.fillRect(x, y, tileSize, tileSize);
                }
            }
        }

        private void drawPieces(Graphics g) throws IOException {
            toDraw = toDraw == null ? board.getPieces() : toDraw;
            for (int i = 0; i < toDraw.size(); i++) {
                Piece p = toDraw.get(i);
                Square square = p.getSquare();
                int x = square.getX() * tileSize;
                int y = this.getHeight() - square.getY() * tileSize;

                g.drawImage(PieceImage.getImage(p, tileSize), x, y, null);
            }
        }

        private void drawHighlightedSquares(Graphics g) {
            g.setColor(new Color(0, 255, 255, 50));
            for (Square square : highlightedSquares) {
                int x = square.getX() * tileSize;
                int y = getHeight() - square.getY() * tileSize;
                g.fillRect(x, y, tileSize, tileSize);
            }
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            try {
                drawBoard(g);
                drawHighlightedSquares(g);
                drawPieces(g);
            } catch (ConcurrentModificationException | IOException e) {
                e.printStackTrace(); // Catch any multithreaded/IO problems
            }
        }
    }
}
