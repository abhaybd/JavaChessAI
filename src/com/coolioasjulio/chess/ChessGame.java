package com.coolioasjulio.chess;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.coolioasjulio.chess.exceptions.InvalidMoveException;
import com.coolioasjulio.chess.pieces.Piece;
import com.coolioasjulio.chess.players.Player;

public abstract class ChessGame {
    private static final Logger logger = Logger.getLogger("ChessGame");

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

    public void setTileSize(int tileSize) {
        this.tileSize = tileSize;
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
        gameThread.setDaemon(true);
        gameThread.start();
    }

    public int playGame(Player white, Player black) {
        white.setTeam(Piece.WHITE);
        black.setTeam(Piece.BLACK);
        int team = Piece.WHITE;
        int winner = 0;

        while (!Thread.interrupted()) {
            Board.BoardState beforeState = board.saveState();

            onTurnStarted(team);

            draw(beforeState.pieces);

            boolean check = false;
            try {
                Player toMove = team == white.getTeam() ? white : black;
                Move m = toMove.getMove();

                if (m == null) {
                    throw new InvalidMoveException("Please select valid squares!");
                }

                board.doMove(m);
                if (board.inCheck(team) || board.inCheckMate(team)) {
                    throw new InvalidMoveException("You are in check or checkmate!");
                }

                moves.add(m);

                team *= -1;
                if (board.inCheckMate(team)) {
                    logger.info("Checkmate!");
                    logger.info(team == Piece.WHITE ? "0-1" : "1-0");
                    winner = -team;
                    break;
                } else if (board.inStaleMate(team)) {
                    logger.info("Stalemate!");
                    logger.info("1/2-1/2");
                    winner = 0;
                    break;
                } else if (board.inCheck(team)) {
                    check = true;
                    logger.info("Check!");
                }
            } catch (InvalidMoveException e) {
                if (e.getMessage() == null || e.getMessage().equals("")) {
                    logger.warning("Invalid! Try again!");
                    e.printStackTrace();
                } else {
                    logger.warning("Invalid! Try again! - " + e.getMessage());
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
                out.printf("%02d. %2$6s 1-0%n", moveNum, whiteMove);
            } else if (i == moves.size() - 2) {
                out.printf("%02d. %2$6s, %3$6s 0-1%n", moveNum, whiteMove, moves.get(i + 1).toString());
            } else {
                out.printf("%02d. %2$6s, %3$6s%n", moveNum, whiteMove, moves.get(i + 1).toString());
            }
        }
    }

    public void draw() {
        draw(board.getPieces());
    }

    protected abstract void draw(List<Piece> toDraw);

    public void onTurnStarted(int team) {
        // Empty
    }

    public void onTurnEnded(int team, boolean check) {
        // Empty
    }
}
