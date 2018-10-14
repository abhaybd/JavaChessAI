package com.coolioasjulio.chess.players;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.coolioasjulio.chess.Board;
import com.coolioasjulio.chess.Logger;
import com.coolioasjulio.chess.Move;
import com.coolioasjulio.chess.MoveCandidate;
import com.coolioasjulio.chess.Player;
import com.coolioasjulio.chess.heuristics.Heuristic;
import com.coolioasjulio.chess.heuristics.PositionalHeuristic;

public class MinimaxComputerPlayer extends Player {
    private static final long TIMEOUT_MILLIS = 2000;
    private static final int KEEP_MOVES = 3;
    private static final double SPACE_SCORE = 0.0;

    private long expiredTime;
    private Heuristic heuristic;

    public MinimaxComputerPlayer(Board board) {
        super(board);
        this.heuristic = new PositionalHeuristic(SPACE_SCORE);
        this.board = board;
    }

    private List<MoveCandidate> movesAtDepth(Board board, int depth, int team) {
        Move[] moves = board.getMoves(team);
        List<MoveCandidate> bestMoves = new ArrayList<>();
        for (Move m : moves) {
            Board boardCopy = board.copy();
            boardCopy.doMove(m);

            if (boardCopy.inCheck(team))
                continue;

            double score;
            if (depth <= 1) {
                // This should NOT be the AI
                score = heuristic.getScore(boardCopy, team);
            } else {
                MoveCandidate[] possibleMoves = minimax(boardCopy, depth - 1, -team);

                // Timeout
                if (possibleMoves == null) {
                    return null;
                }

                // If you detect a checkmate, return it immediately.
                if (possibleMoves.length == 0) {
                    return Arrays.asList(new MoveCandidate(m, team == this.team ? -1000 : 1000));
                }

                MoveCandidate mc = possibleMoves[0];
                score = mc.getScore();
            }

            bestMoves.add(new MoveCandidate(m, score));

            if (System.currentTimeMillis() > expiredTime) {
                return null; // A half-searched tree is as good as a not searched tree
            }
        }

        return bestMoves;
    }

    private MoveCandidate[] minimax(Board board, int depth, int team) {
        List<MoveCandidate> bestMoves = movesAtDepth(board, depth, team);
        if (bestMoves == null)
            return null;
        int toKeep = Math.min(KEEP_MOVES, bestMoves.size());
        if (team == this.team) {
            return bestMoves.stream().sorted(Comparator.comparing(MoveCandidate::getScore)).collect(Collectors.toList())
                    .subList(0, toKeep).toArray(new MoveCandidate[0]);
        } else if (team == -this.team) {
            return bestMoves.stream().sorted(Comparator.comparing(MoveCandidate::getScore).reversed())
                    .collect(Collectors.toList()).subList(0, toKeep).toArray(new MoveCandidate[0]);
        } else {
            throw new IllegalArgumentException("Team must be -1 or 1!");
        }
    }

    @Override
    public Move getMove() {
        List<MoveCandidate> bestMoves = new ArrayList<MoveCandidate>();
        expiredTime = System.currentTimeMillis() + TIMEOUT_MILLIS;
        for (int depth = 2; System.currentTimeMillis() <= expiredTime; depth += 2) {
            Logger.getGlobalLogger().log("Searching with depth: " + depth);
            MoveCandidate[] moves = minimax(board, depth, team);
            if (moves != null) {
                bestMoves.addAll(Arrays.asList(moves));
            }
        }

        if (bestMoves.size() == 0) {
            throw new IllegalStateException("TIMEOUT value is too little!");
        }

        Logger.getGlobalLogger().log();
        int toKeep = Math.min(KEEP_MOVES, bestMoves.size());

        List<MoveCandidate> kept = bestMoves.stream().distinct().sorted(Comparator.comparing(MoveCandidate::getScore))
                .collect(Collectors.toList()).subList(0, toKeep);
        Logger.getGlobalLogger().log(kept.toString());

        MoveCandidate bestMove = softmaxSelect(kept,
                kept.stream().map(e -> -e.getScore()).collect(Collectors.toList()));

        Logger.getGlobalLogger().logf("%s - Score: %.2f\n", bestMove.getMove().toString(), bestMove.getScore());
        return bestMove.getMove();
    }

    private <T> T softmaxSelect(List<T> toSelect, List<Double> scores) {
        if (toSelect.size() != scores.size() || toSelect.size() == 0 || scores.size() == 0) {
            throw new IllegalArgumentException("Must have equal sizes and nonzero!");
        }

        List<Double> unNormalizedProbabilities = scores.stream().map(Math::exp).map(e -> e == 0.0 ? 1e-8 : e)
                .map(e -> e == Double.POSITIVE_INFINITY ? Double.MAX_VALUE / toSelect.size() : e)
                .collect(Collectors.toList());
        double denominator = unNormalizedProbabilities.stream().reduce(Double::sum)
                .orElseThrow(IllegalStateException::new);
        double[] probabilities = unNormalizedProbabilities.stream().mapToDouble(d -> d / denominator).toArray();
        double random = Math.random();
        for (int i = 0; i < probabilities.length; i++) {
            random -= probabilities[i];
            if (random <= 0) {
                return toSelect.get(i);
            }
        }
        throw new IllegalStateException("The softmax selection malfunctioned! The numbers do not sum to 1!");
    }
}
