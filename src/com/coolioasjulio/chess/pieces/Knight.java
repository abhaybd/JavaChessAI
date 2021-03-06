package com.coolioasjulio.chess.pieces;

import java.util.ArrayList;

import com.coolioasjulio.chess.Board;
import com.coolioasjulio.chess.Move;
import com.coolioasjulio.chess.Square;

public class Knight extends Piece {

    public Knight(Square square, int team, Board board) {
        super(square, team, board);
    }

    public double getRawValue() {
        return Piece.KNIGHT_VALUE;
    }

    public double getVanillaValue() {
        return Piece.VANILLA_KNIGHT_VALUE;
    }

    public Move[] getMoves() {
        ArrayList<Move> moves = new ArrayList<>();
        for (int i = -2; i <= 2; i++) {
            if (i == 0 || i + square.getX() < 0 || i + square.getX() > 7) {
                continue;
            }
            for (int j = -2; j <= 2; j++) {
                if (j != 0 && Math.abs(j) != Math.abs(i) && j + square.getY() >= 1 && j + square.getY() <= 8) {
                    Square toCheck = new Square(i + square.getX(), j + square.getY());
                    Piece p = board.checkSquare(toCheck);
                    if (p == null || p.team != team) {
                        boolean capture = p != null;
                        moves.add(new Move(this, toCheck, capture));
                    }
                }
            }
        }
        return moves.toArray(new Move[0]);
    }

    public Knight copy() {
        Knight knight = new Knight(this.square, this.team, this.board);
        knight.moved = moved;
        return knight;
    }
}
