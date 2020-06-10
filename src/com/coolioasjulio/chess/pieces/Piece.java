package com.coolioasjulio.chess.pieces;

import java.util.Objects;

import com.coolioasjulio.chess.Board;
import com.coolioasjulio.chess.Move;
import com.coolioasjulio.chess.Square;
import com.coolioasjulio.chess.exceptions.InvalidMoveException;
import com.coolioasjulio.chess.exceptions.InvalidSquareException;

public abstract class Piece {
    public static final double KING_VALUE = 200;
    public static final double QUEEN_VALUE = 9;
    public static final double ROOK_VALUE = 5;
    public static final double BISHOP_VALUE = 3.3;
    public static final double KNIGHT_VALUE = 3.2;
    public static final double PAWN_VALUE = 1;

    public static final int VANILLA_KING_VALUE = Integer.MAX_VALUE;
    public static final int VANILLA_QUEEN_VALUE = 9;
    public static final int VANILLA_ROOK_VALUE = 5;
    public static final int VANILLA_BISHOP_VALUE = 3;
    public static final int VANILLA_KNIGHT_VALUE = 3;
    public static final int VANILLA_PAWN_VALUE = 1;

    public static final int WHITE = 1;
    public static final int BLACK = -1;

    public static String getType(Piece p) {
        if (p instanceof Pawn) {
            return "";
        }
        if (p instanceof Knight) {
            return "N";
        }
        if (p instanceof Rook) {
            return "R";
        }
        if (p instanceof Bishop) {
            return "B";
        }
        if (p instanceof Queen) {
            return "Q";
        }
        if (p instanceof King) {
            return "K";
        }
        System.err.println("Unrecognized piece!");
        return null;
    }

    protected Square square;
    protected int team;
    protected Board board;
    protected boolean moved = false;

    public Piece(String square, int team, Board board) throws InvalidSquareException {
        if (square.length() != 2 || Math.abs(team) != 1) {
            throw new InvalidSquareException();
        }
        this.team = team;
        this.square = Square.parseString(square);
        this.board = board;
    }

    public Piece(Square square, int team, Board board) {
        this.square = square;
        this.team = team;
        this.board = board;
    }

    public abstract Move[] getMoves();

    public abstract Piece copy();

    /**
     * Mathematical value of the piece
     *
     * @return
     */
    public abstract double getRawValue();

    /**
     * INTERNAL USE ONLY.
     * 
     * @param move Move to make.
     * @throws InvalidMoveException If something goes wrong.
     */
    public void move(Square move) throws InvalidMoveException {
        move(move, null);
    }

    /**
     * INTERNAL USE ONLY.
     * 
     * @param move      Move to make.
     * @param promotion Piece to promote to, if applicable.
     * @throws InvalidMoveException If something goes wrong.
     */
    public void move(Square move, String promotion) throws InvalidMoveException {
        moved = true;
        Piece p = board.checkSquare(move);
        if (p != null && p.team == team) {
            throw new InvalidMoveException("Can't move into your own piece!");
        }
        square = move;
    }

    public boolean hasMove(Move move) {
        Move[] moves = getMoves();
        for (Move m : moves) {
            if (m.toString().equals(move.toString()))
                return true;
        }
        return false;
    }

    public boolean hasMoved() {
        return moved;
    }

    public String getType() {
        return Piece.getType(this);
    }

    public String getName() {
        String name = getType().toLowerCase();
        if (name.length() == 0) {
            name = "p";
        }
        return name;
    }

    /**
     * Chess value of the piece
     *
     * @return
     */
    public double getVanillaValue() {
        return -1;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public int getTeam() {
        return team;
    }

    public Square getSquare() {
        return square;
    }

    public String toString() {
        return Piece.getType(this) + square.getSquare();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Piece)) {
            return false;
        }
        Piece p = (Piece) o;
        return p.square.equals(this.square) && p.getType().equals(getType()) && p.team == this.team;
    }

    @Override
    public int hashCode() {
        return Objects.hash(team, square, getType());
    }
}
