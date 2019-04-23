package com.coolioasjulio.chess.heuristics;

import com.coolioasjulio.chess.Board;
import com.coolioasjulio.chess.pieceevaluators.PieceEvaluator;
import com.coolioasjulio.chess.pieceevaluators.PositionalPieceEvaluator;

public class MaterialHeuristic implements Heuristic {
    private double spaceScore;
    private PieceEvaluator pieceEvaluator;

    public MaterialHeuristic(double spaceScore) {
        this(spaceScore, new PositionalPieceEvaluator());
    }

    public MaterialHeuristic(double spaceScore, PieceEvaluator pieceEvaluator) {
        this.spaceScore = spaceScore;
        this.pieceEvaluator = pieceEvaluator;
    }

    public double getScore(Board board, int team) {
        double space = (double) board.getMoves(team).length * (double) spaceScore;
        double material = pieceEvaluator.getMaterialValue(board, team);
        double oppMaterial = pieceEvaluator.getMaterialValue(board, -team);
        double checkmateModifier = 0;
        if (board.inCheckMate(-team))
            checkmateModifier = 1000;
        else if (board.inCheckMate(team))
            checkmateModifier = -1000;
        return space + material - oppMaterial + checkmateModifier;
    }
}
