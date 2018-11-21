package com.coolioasjulio.chess.endgameevaluators;

import java.util.LinkedList;
import java.util.List;

import com.coolioasjulio.chess.Board;
import com.coolioasjulio.chess.pieces.Piece;

public class SpeelmanEndgameEvaluator implements EndgameEvaluator {

    @Override
    public boolean inEndgame(Board board) {
        List<Piece> black = new LinkedList<>(board.getPieces(Piece.BLACK));
        List<Piece> white = new LinkedList<>(board.getPieces(Piece.WHITE));

        black.remove(board.getKing(Piece.BLACK));
        white.remove(board.getKing(Piece.WHITE));

        return black.stream().mapToDouble(Piece::getRawValue).sum() <= 13
                && white.stream().mapToDouble(Piece::getRawValue).sum() <= 13;
    }
}
