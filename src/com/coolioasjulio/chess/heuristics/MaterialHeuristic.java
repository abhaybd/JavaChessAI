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
        double space = spaceScore == 0 ? 0 : (double) board.getMoves(team).length * spaceScore;
        double material = pieceEvaluator.getMaterialValue(board, team);
        double oppMaterial = pieceEvaluator.getMaterialValue(board, -team);
        if (board.inCheckMate(-team)) {
            return 1000;
        } else if (board.inCheckMate(team)) {
            return -1000;
        }
        double score = space + material - oppMaterial;
        boolean isDraw = board.isDrawByThreeFoldRepetition() || board.inStaleMate(team) || board.inStaleMate(-team);
        if (isDraw) {
            if (score <= -4) {
                score = 100;
            } else if (score >= 4) {
                score = -100;
            } else {
                score = 0;
            }
        }

        return score;
    }
}
