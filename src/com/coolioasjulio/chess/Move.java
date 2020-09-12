package com.coolioasjulio.chess;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.coolioasjulio.chess.exceptions.InvalidMoveException;
import com.coolioasjulio.chess.pieces.Piece;

public class Move {
    protected int team;
    protected Square start, end;
    protected String type;
    protected boolean capture;

    /**
     * Create a Move object representing a move that does not capture.
     *
     * @param piece The piece that is making the move.
     * @param start The starting square of the piece.
     * @param end   The square the piece is moving to.
     */
    public Move(Piece piece, Square start, Square end) {
        this(piece, start, end, false);
    }

    /**
     * Create a Move object representing a regular move that may or may not capture a piece.
     *
     * @param piece   The piece that is making the move.
     * @param start   The starting square of the piece.
     * @param end     The square the piece is moving to.
     * @param capture If true, this move is a capture. Standard chess rules apply to captures.
     */
    public Move(Piece piece, Square start, Square end, boolean capture) {
        this.team = piece.getTeam();
        this.start = start;
        this.end = end;
        this.type = Piece.getType(piece);
        this.capture = capture;
    }

    public void doMove(Board b) {
        Piece p = b.checkSquare(getStart());
        List<Square> endSquares = Arrays.stream(p.getMoves()).map(Move::getEnd)
                .collect(Collectors.toList());
        if (!endSquares.contains(getEnd())) {
            throw new InvalidMoveException("Invalid move or end square!");
        }
        if (doesCapture()) {
            Piece toCapture = b.checkSquare(getEnd());
            if (toCapture.getTeam() != p.getTeam()) {
                b.removePiece(toCapture);
            }
        }
        p.move(getEnd());
    }

    /**
     * Get the team of the player making the move.
     *
     * @return The team of the player making the move.
     */
    public int getTeam() {
        return team;
    }

    /**
     * Get the starting square of this move.
     *
     * @return The square that the piece is on before the move.
     */
    public Square getStart() {
        return start;
    }

    /**
     * Get the ending square of this move.
     *
     * @return The square that the piece is moving to.
     */
    public Square getEnd() {
        return end;
    }

    /**
     * Get if this move represents a capture move.
     *
     * @return True if this move captures a piece, false otherwise.
     */
    public boolean doesCapture() {
        return capture;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Move)) {
            return false;
        }
        Move move = (Move) o;
        return this.type.equals(move.type) && this.start.equals(move.start) && this.end.equals(move.end)
                && this.capture == move.capture;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, start, end, capture);
    }

    @Override
    public String toString() {
        String action = capture ? "x" : "-";
        String startSquare = start.toString();
        String endSquare = end.toString();
        return type + startSquare + action + endSquare;
    }
}
