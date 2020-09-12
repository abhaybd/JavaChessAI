package com.coolioasjulio.chess;

public class MoveCandidate {
    private final Move move;
    private double score;

    public MoveCandidate(Move move, double score) {
        this.move = move;
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public Move getMove() {
        return move;
    }

    @Override
    public String toString() {
        return String.format("%s - %5.2f", move.toString(), score);
    }
}
