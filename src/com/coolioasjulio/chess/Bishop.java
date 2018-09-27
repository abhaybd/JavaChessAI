package com.coolioasjulio.chess;

import java.util.ArrayList;

public class Bishop extends Piece {

    public Bishop(Square square, int team, Board board) {
        super(square, team, board);
    }

    public Bishop(String square, int team, Board board) throws InvalidSquareException {
        super(square, team, board);
    }

    private boolean between(int toCheck, int bottom, int upper) {
        return bottom <= toCheck && toCheck <= upper;
    }

    public double getRawValue() {
        return Piece.BISHOP_VALUE;
    }

    public double getVanillaValue() {
        return Piece.VANILLA_BISHOP_VALUE;
    }

    public Move[] getMoves() {
        Square square = super.getSquare();
        int team = super.getTeam();
        ArrayList<Move> moves = new ArrayList<Move>();
        for (int x = -1; x <= 1; x += 2) {
            for (int y = -1; y <= 1; y += 2) {
                try {
                    int mult = 1;
                    Piece p = board.checkSquare(new Square(x + square.getX(), y + square.getY()));
                    while (p == null && between(x * mult + square.getX(), 0, 7)
                            && between(y * mult + square.getY(), 1, 8)) {
                        moves.add(
                                new Move(this, square, new Square(x * mult + square.getX(), y * mult + square.getY())));
                        mult++;
                        p = board.checkSquare(new Square(x * mult + square.getX(), y * mult + square.getY()));
                    }
                    if (p != null && p.team != team) {
                        moves.add(new Move(this, square, p.getSquare(), true));
                    }
                } catch (InvalidSquareException e) {
                    continue;
                }
            }
        }
        return moves.toArray(new Move[0]);
    }

    public Bishop copy() {
        return new Bishop(this.square, this.team, this.board);
    }
}
