package com.coolioasjulio.chess.heuristics;

import com.coolioasjulio.chess.Board;

public class PositionalHeuristic implements Heuristic {
    private double spaceScore;

    public PositionalHeuristic(double spaceScore) {
        this.spaceScore = spaceScore;
    }

    public double getScore(Board board, int team) {
        double space = (double) board.getMoves(team).length * (double) spaceScore;
        double material = board.getMaterialScore(team);
        double oppMaterial = board.getMaterialScore(-team);
        double checkmateModifier = 0;
        if (board.inCheckMate(-team))
            checkmateModifier = 1000;
        else if (board.inCheckMate(team))
            checkmateModifier = -1000;
        return space + material - oppMaterial + checkmateModifier;
    }
}
