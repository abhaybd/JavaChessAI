package com.coolioasjulio.chess;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.coolioasjulio.chess.players.Player;

public abstract class ChessGame {

    protected Board board;
    protected ArrayList<Move> moves;
    protected int tileSize;
    protected List<Square> highlightedSquares;
    private Thread gameThread;

    public ChessGame(int tileSize) {
        this.tileSize = tileSize;
        board = new Board();
        board.setup();
        moves = new ArrayList<>();
        highlightedSquares = new ArrayList<>();
    }

    public void addHighlightedSquare(Square square) {
        highlightedSquares.add(square);
    }

    public int getTileSize() {
        return tileSize;
    }

    public Board getBoard() {
        return board;
    }

    public void interruptGame() {
        gameThread.interrupt();
    }

    /**
     * Run a game between these two players in a separate thread.
     * 
     * @param white The white player.
     * @param black The black player.
     */
    public void playGameAsync(final Player white, final Player black) {
        if (gameThread != null) {
            throw new IllegalStateException("Game has already been started! Create a new instance!");
        }

        gameThread = new Thread(() -> playGame(white, black));
        gameThread.start();
    }

    public int playGame(Player white, Player black) {
        white.setTeam(Piece.WHITE);
        black.setTeam(Piece.BLACK);
        int team = Piece.WHITE;
        int winner = 0;

        while (!Thread.interrupted()) {
            List<Piece> beforeState = board.saveState();

            onTurnStarted(team);

            draw(beforeState);

            boolean check = false;
            try {
                Player toMove = team == white.getTeam() ? white : black;
                Move m = toMove.getMove();

                board.restoreState(beforeState);

                board.doMove(m);
                if (board.inCheck(team) || board.inCheckMate(team)) {
                    throw new InvalidMoveException("You are in check or checkmate!");
                }

                moves.add(m);

                team *= -1;
                if (board.inCheckMate(team)) {
                    Logger.getGlobalLogger().log("Checkmate!");
                    Logger.getGlobalLogger().log(team == Piece.WHITE ? "0-1" : "1-0");
                    winner = -team;
                    break;
                } else if (board.inStaleMate(team)) {
                    Logger.getGlobalLogger().log("Stalemate!");
                    Logger.getGlobalLogger().log("1/2-1/2");
                    winner = 0;
                    break;
                } else if (board.inCheck(team)) {
                    check = true;
                    Logger.getGlobalLogger().log("Check!");
                }
            } catch (InvalidMoveException e) {
                if (e.getMessage() == null || e.getMessage().equals("")) {
                    Logger.getGlobalLogger().logErr("Invalid! Try again!");
                    e.printStackTrace();
                } else {
                    Logger.getGlobalLogger().logErr("Invalid! Try again! - " + e.getMessage());
                }
                board.restoreState(beforeState);
            } catch (Exception e) {
                e.printStackTrace();
                board.restoreState(beforeState);
            } finally {
                highlightedSquares.clear();
            }
            draw();
            onTurnEnded(-team, check);
        }
        draw();

        return winner;
    }

    public void printMoves(PrintStream out) {
        out.println("\n===MOVES===");
        for (int i = 0; i < moves.size(); i += 2) {
            int moveNum = (i / 2) + 1;
            String whiteMove = moves.get(i).toString();
            if (i == moves.size() - 1) {
                out.println(String.format("%02d. %2$6s 1-0", moveNum, whiteMove));
            } else if (i == moves.size() - 2) {
                out.println(String.format("%02d. %2$6s, %3$6s 0-1", moveNum, whiteMove, moves.get(i + 1).toString()));
            } else {
                out.println(String.format("%02d. %2$6s, %3$6s", moveNum, whiteMove, moves.get(i + 1).toString()));
            }
        }
    }

    public void draw() {
        draw(board.getPieces());
    }

    public abstract void draw(List<Piece> toDraw);

    public void onTurnStarted(int team) {
        // Empty
    }

    public void onTurnEnded(int team, boolean check) {
        // Empty
    }
}
