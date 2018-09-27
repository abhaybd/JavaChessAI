package com.coolioasjulio.chess;

import java.awt.Dimension;
import java.awt.Graphics;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Chess extends JPanel {
    private static final long serialVersionUID = 1L;

    public static void main(String[] args) {
        Chess chess = new Chess(100);
        JFrame frame = new JFrame();
        frame.add(chess);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        char t;
        try (Scanner input = new Scanner(System.in)) {
            System.out.println("What team do you want to be? w/b");
            t = input.nextLine().toLowerCase().charAt(0);
        }

        int playerTeam = (t == 'b') ? Piece.BLACK : Piece.WHITE;
        Player human = new HumanGUIPlayer(playerTeam, chess.getBoard(), chess);
        Player betterComputer = new BetterComputerPlayer(chess.getBoard(), -playerTeam);
//		Player computer = new ComputerPlayer(chess.getBoard(), Piece.BLACK);
        chess.runGame(human, betterComputer);
    }

    private Board board;
    private ArrayList<Move> moves;
    private List<Piece> piecesToDraw;
    private int tileSize;

    public Chess(int tileSize) {
        this.tileSize = tileSize;
        board = new Board();
        board.setup();
        moves = new ArrayList<Move>();
        this.setPreferredSize(new Dimension(tileSize * 8, tileSize * 8));
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

    private void drawPieces(Graphics g) throws IOException {
        List<Piece> toDraw = piecesToDraw != null ? piecesToDraw : board.getPieces();
        for (Piece p : toDraw) {
            Square square = p.getSquare();
            int x = square.getX() * tileSize;
            int y = this.getHeight() - square.getY() * tileSize;
            g.drawImage(Piece.getImage(p), x, y, null);
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

    private void playGame(Player player1, Player player2) {
        int team = 1;
        while (!Thread.interrupted()) {
            List<Piece> beforeState = board.saveState();
            piecesToDraw = beforeState;

            repaint();

            try {
                Player toMove = team == player1.getTeam() ? player1 : player2;
                Move m = toMove.getMove();
                board.restoreState(beforeState);

                board.doMove(m);
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
        }
        printMoves();
    }

    public void runGame(final Player player1, final Player player2) {
        Thread t = new Thread(() -> playGame(player1, player2));
        t.setDaemon(true);
        t.start();
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

    @Override
    public void paintComponent(Graphics g) {
        try {
            drawBoard(g);
            drawPieces(g);
        } catch (Exception e) {
        } // Fail silently
    }
}
