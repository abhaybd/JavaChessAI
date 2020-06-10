package com.coolioasjulio.chess;

import java.util.Objects;

import com.coolioasjulio.chess.pieces.Piece;

public class Move {
    private Piece piece;
    private int team;
    private Square start, end;
    private String type;
    private boolean capture;
    private boolean kingSideCastle;
    private boolean queenSideCastle;

    /**
     * Create a Move object representing a castle move.
     *
     * @param team           The team that is castling.
     * @param kingSideCastle If true, castle kingside, else queenside.
     */
    public Move(int team, boolean kingSideCastle) {
        this.kingSideCastle = kingSideCastle;
        this.queenSideCastle = !kingSideCastle;
        this.team = team;
    }

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
        this.piece = piece;
        this.start = start;
        this.end = end;
        this.type = Piece.getType(piece);
        this.capture = capture;
        this.team = piece.getTeam();
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
     * Get if this move represents either a kingside or queenside castle.
     *
     * @return True if this move represents castle move, false otherwise.
     */
    public boolean isCastle() {
        return kingSideCastle || queenSideCastle;
    }

    /**
     * Get if this move represents a kingside castle.
     *
     * @return True if this move is a kingside castle, false otherwise.
     */
    public boolean isKingSideCastle() {
        return kingSideCastle;
    }

    /**
     * Get if this move represents a queenside castle.
     *
     * @return True if this move is a queenside castle, false otherwise.
     */
    public boolean isQueenSideCastle() {
        return queenSideCastle;
    }

    /**
     * Get the piece doing the move.
     *
     * @return The Piece object doing the move.
     */
    public Piece getPiece() {
        return piece;
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

    /**
     * Get the short notation for this move.
     *
     * @return The short notation, as defined by standard chess rules.
     */
    public String shortNotation() {
        String aux = "";
        for (Piece p : piece.getBoard().getPieces()) {
            Move move = new Move(p, p.getSquare(), end);
            if (p.hasMove(move)) {
                if (!p.equals(piece)) {
                    if (p.getSquare().getY() == piece.getSquare().getY())
                        aux = String.valueOf(p.getSquare().toString().charAt(0));
                    else if (p.getSquare().getX() == piece.getSquare().getX())
                        aux = String.valueOf(p.getSquare().toString().charAt(1));
                }
            }
        }
        return type + aux + (capture ? "x" : "") + end.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Move)) {
            return false;
        }
        Move move = (Move) o;
        return this.piece.equals(move.piece) && this.start.equals(move.start) && this.end.equals(move.end)
                && this.capture == move.capture;
    }

    @Override
    public int hashCode() {
        return Objects.hash(piece, start, end, capture);
    }

    @Override
    public String toString() {
        if (isKingSideCastle())
            return "o-o";
        else if (isQueenSideCastle())
            return "o-o-o";
        String action = capture ? "x" : "-";
        String startSquare = start.toString();
        String endSquare = end.toString();
        return type + startSquare + action + endSquare;
    }
}
