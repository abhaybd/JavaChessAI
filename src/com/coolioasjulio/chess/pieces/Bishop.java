package com.coolioasjulio.chess.pieces;

import java.util.ArrayList;

import com.coolioasjulio.chess.Board;
import com.coolioasjulio.chess.Move;
import com.coolioasjulio.chess.Square;
import com.coolioasjulio.chess.exceptions.InvalidSquareException;

public class Bishop extends Piece {

    public Bishop(Square square, int team, Board board) {
        super(square, team, board);
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
        ArrayList<Move> moves = new ArrayList<>();
        for (int x = -1; x <= 1; x += 2) {
            for (int y = -1; y <= 1; y += 2) {
                if (!Square.validSquare(x + square.getX(), y + square.getY())) continue;
                int mult = 1;
                Piece p = board.checkSquare(new Square(x + square.getX(), y + square.getY()));
                while (p == null) {
                    moves.add(
                            new Move(this, square, new Square(x * mult + square.getX(), y * mult + square.getY())));
                    mult++;
                    if (!Square.validSquare(x * mult + square.getX(), y * mult + square.getY())) break;
                    p = board.checkSquare(new Square(x * mult + square.getX(), y * mult + square.getY()));
                }
                if (p != null && p.team != team) {
                    moves.add(new Move(this, square, p.getSquare(), true));
                }
            }
        }
        return moves.toArray(new Move[0]);
    }

    public Bishop copy() {
        Bishop bishop = new Bishop(this.square, this.team, this.board);
        bishop.moved = moved;
        return bishop;
    }
}
