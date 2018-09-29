package com.coolioasjulio.chess;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public class ChessGame extends JPanel {
    private static final long serialVersionUID = 1L;

    private Board board;
    private ArrayList<Move> moves;
    private List<Piece> piecesToDraw;
    private int tileSize;
    private List<Square> highlightedSquares;

    public ChessGame(int tileSize) {
        this.tileSize = tileSize;
        board = new Board();
        board.setup();
        moves = new ArrayList<>();
        highlightedSquares = new ArrayList<>();
        this.setPreferredSize(new Dimension(tileSize * 8, tileSize * 8));
    }

    public void addHighlightedSquare(Square square) {
        highlightedSquares.add(square);
    }

    public int getTileSize() {
        return tileSize;
    }

    private boolean isEven(int i) {
        return i % 2 == 0;
    }

    public Board getBoard() {
        return board;
    }

    /**
     * Run a game between these two players.
     * 
     * @param white The white player.
     * @param black The black player.
     */
    public void runGame(final Player white, final Player black) {
        Thread t = new Thread(() -> playGame(white, black));
        t.setDaemon(true);
        t.start();
    }

    private void playGame(Player white, Player black) {
        white.setTeam(Piece.WHITE);
        black.setTeam(Piece.BLACK);
        int team = Piece.WHITE;

        while (!Thread.interrupted()) {
            List<Piece> beforeState = board.saveState();
            piecesToDraw = beforeState;

            repaint();

            try {
                Player toMove = team == white.getTeam() ? white : black;
                Move m = toMove.getMove();

                board.restoreState(beforeState);

                board.doMove(m);
                if (board.inCheck(team) || board.inCheckMate(team)) {
                    throw new InvalidMoveException();
                }

                moves.add(m);

                team *= -1;
                if (board.inCheckMate(team)) {
                    System.out.println("Checkmate!");
                    System.out.println(team == Piece.WHITE ? "0-1" : "1-0");
                    break;
                } else if (board.inStaleMate(team)) {
                    System.out.println("Stalemate!");
                    System.out.println("1/2-1/2");
                    break;
                } else if (board.inCheck(team)) {
                    System.out.println("Check!");
                }
            } catch (InvalidMoveException e) {
                System.err.println("Invalid! Try again!");
                board.restoreState(beforeState);
            } catch (Exception e) {
                e.printStackTrace();
                board.restoreState(beforeState);
            }
            highlightedSquares.clear();
        }
        piecesToDraw = null;
        printMoves();
        repaint();
    }

    private void printMoves() {
        System.out.println();
        for (int i = 0; i < moves.size(); i += 2) {
            int moveNum = (i / 2) + 1;
            String whiteMove = moves.get(i).toString();
            if (i == moves.size() - 1) {
                System.out.println(String.format("%02d. %2$6s 1-0", moveNum, whiteMove));
            } else if (i == moves.size() - 2) {
                System.out.println(
                        String.format("%02d. %2$6s, %3$6s 0-1", moveNum, whiteMove, moves.get(i + 1).toString()));
            } else {
                System.out
                        .println(String.format("%02d. %2$6s, %3$6s", moveNum, whiteMove, moves.get(i + 1).toString()));
            }
        }
    }

    private void drawBoard(Graphics g) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int x = i * tileSize;
                int y = j * tileSize;
                g.setColor(Board.TAN);
                if (isEven(i + j))
                    g.setColor(Board.BROWN);
                g.fillRect(x, y, tileSize, tileSize);
            }
        }
    }

    private void drawPieces(Graphics g) throws IOException {
        List<Piece> toDraw = piecesToDraw != null ? piecesToDraw : board.getPieces();
        for (int i = 0; i < toDraw.size(); i++) {
            Piece p = toDraw.get(i);
            Square square = p.getSquare();
            int x = square.getX() * tileSize;
            int y = this.getHeight() - square.getY() * tileSize;
            g.drawImage(Piece.getImage(p), x, y, null);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
