package com.coolioasjulio.chess.heuristics;

import com.coolioasjulio.chess.Board;
import com.coolioasjulio.chess.evaluators.PieceEvaluator;
import com.coolioasjulio.chess.evaluators.PositionalPieceEvaluator;

public class PositionalHeuristic implements Heuristic {
    private double spaceScore;
    private PieceEvaluator pieceEvaluator;

    public PositionalHeuristic(double spaceScore) {
        this(spaceScore, new PositionalPieceEvaluator());
    }

    public PositionalHeuristic(double spaceScore, PieceEvaluator pieceEvaluator) {
        this.spaceScore = spaceScore;
        this.pieceEvaluator = pieceEvaluator;
    }

    public double getScore(Board board, int team) {
        double space = (double) board.getMoves(team).length * (double) spaceScore;
        double material = board.getMaterialScore(team, pieceEvaluator);
        double oppMaterial = board.getMaterialScore(-team, pieceEvaluator);
        double checkmateModifier = 0;
        if (board.inCheckMate(-team))
            checkmateModifier = 1000;
        else if (board.inCheckMate(team))
            checkmateModifier = -1000;
        return space + material - oppMaterial + checkmateModifier;
    }
}
