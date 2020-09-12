package com.coolioasjulio.chess;

import com.coolioasjulio.chess.exceptions.InvalidMoveException;
import com.coolioasjulio.chess.pieces.King;
import com.coolioasjulio.chess.pieces.Piece;
import com.coolioasjulio.chess.pieces.Rook;

public class QueenSideCastle extends Move {
    public QueenSideCastle(King king) {
        super(king, new Square(2, king.getTeam() == Piece.WHITE ? 1 : 8));
    }

    @Override
    public void doMove(Board b) {
        King k = b.getKing(team);
        if (b.canQueenSideCastle(k)) {
            Rook r = (Rook) b.checkSquare(new Square(0, k.getSquare().getY()));
            k.move(new Square(2, k.getSquare().getY()));
            r.move(new Square(3, r.getSquare().getY()));
        } else {
            throw new InvalidMoveException("Cannot castle!");
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof QueenSideCastle && super.equals(o);
    }

    @Override
    public String toString() {
        return "o-o-o";
    }
}
