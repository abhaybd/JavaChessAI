package com.coolioasjulio.chess.evaluators;

import com.coolioasjulio.chess.pieces.Piece;

public interface PieceEvaluator {
    double getValue(Piece piece);
}
