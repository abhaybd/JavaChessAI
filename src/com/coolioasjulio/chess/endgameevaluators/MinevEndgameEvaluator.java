package com.coolioasjulio.chess.endgameevaluators;

import com.coolioasjulio.chess.Board;
import com.coolioasjulio.chess.pieces.King;
import com.coolioasjulio.chess.pieces.Pawn;

public class MinevEndgameEvaluator implements EndgameEvaluator {

    @Override
    public boolean inEndgame(Board board) {
        return board.getPieces().stream().filter(e -> !(e instanceof King || e instanceof Pawn)).count() <= 4;
    }
}
