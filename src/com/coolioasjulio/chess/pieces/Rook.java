package com.coolioasjulio.chess.pieces;

import java.util.ArrayList;

import com.coolioasjulio.chess.Board;
import com.coolioasjulio.chess.Move;
import com.coolioasjulio.chess.Square;

public class Rook extends Piece {

    public Rook(Square square, int team, Board board) {
        super(square, team, board);
    }

    public double getRawValue() {
        return Piece.ROOK_VALUE;
    }

    public double getVanillaValue() {
        return Piece.VANILLA_ROOK_VALUE;
    }

    @Override
    public Move[] getMoves() {
        ArrayList<Move> moves = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if (Math.abs(x) == Math.abs(y) || !Square.validSquare(x + square.getX(), y + square.getY())) {
                    continue;
                }
                Piece p = board.checkSquare(new Square(x + square.getX(), y + square.getY()));
                int mult = 1;
                while (p == null) {
                    moves.add(
                            new Move(this, new Square(x * mult + square.getX(), y * mult + square.getY())));
                    mult++;
                    if (!Square.validSquare(x * mult + square.getX(), y * mult + square.getY())) break;
                    p = board.checkSquare(new Square(x * mult + square.getX(), y * mult + square.getY()));
                }
                if (p != null && p.team != team) {
                    moves.add(new Move(this, p.getSquare(), true));
                }
            }
        }
        return moves.toArray(new Move[0]);
    }

    public Rook copy() {
        Rook rook = new Rook(this.square, this.team, this.board);
        rook.moved = moved;
        return rook;
    }
}