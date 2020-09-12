package com.coolioasjulio.chess.pieces;

import java.util.ArrayList;

import com.coolioasjulio.chess.Board;
import com.coolioasjulio.chess.Move;
import com.coolioasjulio.chess.Square;
import com.coolioasjulio.chess.exceptions.InvalidSquareException;

public class King extends Piece {

    public King(Square square, int team, Board board) {
        super(square, team, board);
    }

    public double getRawValue() {
        return Piece.KING_VALUE;
    }

    public double getVanillaValue() {
        return Piece.VANILLA_KING_VALUE;
    }

    @Override
    public Move[] getMoves() {
        ArrayList<Move> moves = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if ((x == 0 && y == 0) || !Square.validSquare(x + square.getX(), y + square.getY()))
                    continue;
                Square s = new Square(x + square.getX(), y + square.getY());
                Piece p = board.checkSquare(s);
                boolean capture = (p != null && p.team != team);
                if (p == null || capture) {
                    Move move = new Move(this, s, capture);
                    moves.add(move);
                }
            }
        }
        return moves.toArray(new Move[0]);
    }

    public King copy() {
        King k = new King(this.square, this.team, this.board);
        k.moved = moved;
        return k;
    }
}
