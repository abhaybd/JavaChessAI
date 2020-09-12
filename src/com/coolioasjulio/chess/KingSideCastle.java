package com.coolioasjulio.chess;

import com.coolioasjulio.chess.exceptions.InvalidMoveException;
import com.coolioasjulio.chess.pieces.King;
import com.coolioasjulio.chess.pieces.Piece;
import com.coolioasjulio.chess.pieces.Rook;

public class KingSideCastle extends Move {
    public KingSideCastle(King king) {
        super(king, new Square(6, king.getTeam() == Piece.WHITE ? 1 : 8));
    }

    @Override
    public void doMove(Board b) {
        King k = b.getKing(team);
        if (b.canKingSideCastle(k)) {
            Rook r = (Rook) b.checkSquare(new Square(7, k.getSquare().getY()));
            k.move(new Square(6, k.getSquare().getY()));
            r.move(new Square(5, r.getSquare().getY()));
        } else {
            throw new InvalidMoveException("Cannot castle!");
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof KingSideCastle && super.equals(o);
    }

    @Override
    public String toString() {
        return "o-o";
    }
}
