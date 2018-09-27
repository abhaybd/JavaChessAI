package com.coolioasjulio.chess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BetterComputerPlayer implements Player {
    private static final long TIMEOUT_MILLIS = 2000;
    private static final int KEEP_MOVES = 2;

    private Board board;
    private int team;
    private long expiredTime;

    public BetterComputerPlayer(Board board, int team) {
        this.board = board;
        this.team = team;
    }
    
    private List<MoveCandidate> bestMovesAtDepth(int depth, int team, double alpha, double beta) {
        Move[] moves = board.getMoves(team);
        List<MoveCandidate> bestMoves = new ArrayList<MoveCandidate>();
        for (Move m : moves) {
            List<Piece> before = board.saveState();
            try {
                board.doMove(m);
                if (!board.inCheck(team)) {
                    double score;
                    if (depth <= 1) {
                        score = board.getScore(team);
                    } else {
                        MoveCandidate[] possibleMoves = minimax(depth - 1, -team, alpha, beta);
                        if (possibleMoves == null || possibleMoves.length == 0)
                            continue;
                        MoveCandidate mc = possibleMoves[0];
                        score = mc.getScore();
                    }
                    
                    if(team == this.team) {
                        alpha = Math.min(alpha, score);
                    } else {
                        beta = Math.max(beta, score);
                    }
                    
                    bestMoves.add(new MoveCandidate(m, score));
                }
            } finally {
                board.restoreState(before);
                if (bestMoves.size() > 0 && (System.currentTimeMillis() > expiredTime || beta >= alpha)) {
                    break;
                }
            }
        }
        
        return bestMoves;
    }

    private MoveCandidate[] minimax(int depth, int team, double alpha, double beta) {
        List<MoveCandidate> bestMoves = bestMovesAtDepth(depth, team, alpha, beta);
        int toKeep = Math.min(KEEP_MOVES, bestMoves.size());
        if(team == this.team) {
            return bestMoves.stream()
                    .sorted(Comparator.comparing(MoveCandidate::getScore))
                    .collect(Collectors.toList())
                    .subList(0, toKeep)
                    .toArray(new MoveCandidate[0]);
        } else if (team == -this.team) {
            return bestMoves.stream()
                    .sorted(Comparator.comparing(MoveCandidate::getScore).reversed())
                    .collect(Collectors.toList())
                    .subList(0, toKeep)
                    .toArray(new MoveCandidate[0]);
        } else {
            throw new IllegalArgumentException("Team must be -1 or 1!");
        }
    }

    @Override
    public Move getMove() {
        List<MoveCandidate> bestMoves = new ArrayList<MoveCandidate>();
        expiredTime = System.currentTimeMillis() + TIMEOUT_MILLIS;
        for (int depth = 2; System.currentTimeMillis() <= expiredTime; depth += 2) {
            System.out.println("Searching with depth: " + depth);
            MoveCandidate[] moves = minimax(depth, team, Double.MAX_VALUE, Double.MIN_VALUE);
            if (System.currentTimeMillis() <= expiredTime) {
                bestMoves.addAll(Arrays.asList(moves));
            }
        }
        int toKeep = Math.min(KEEP_MOVES, bestMoves.size());
        System.out.println(bestMoves.toString());
        
        List<MoveCandidate> kept = bestMoves
                .stream()
                .distinct()
                .sorted(Comparator.comparing(MoveCandidate::getScore))
                .collect(Collectors.toList())
                .subList(0, toKeep);
        
        MoveCandidate bestMove = softmaxSelect(
                kept,
                kept.stream().map(e -> 1.0 / e.getScore()).collect(Collectors.toList()));
        
        System.out.printf("%s - Score: %.2f\n", bestMove.getMove().toString(), bestMove.getScore());
        return bestMove.getMove();
    }
    
    private <T> T softmaxSelect(List<T> toSelect, List<Double> scores) {
        if(toSelect.size() != scores.size() || toSelect.size() == 0 || scores.size() == 0) {
            throw new IllegalArgumentException("Must have equal sizes and nonzero!");            
        }
        
        List<Double> unNormalizedProbabilities = scores.stream().map(Math::exp).collect(Collectors.toList());
        double denominator = unNormalizedProbabilities.stream().reduce(Double::sum).orElseThrow(IllegalStateException::new);
        double[] probabilities = unNormalizedProbabilities.stream().mapToDouble(d -> d / denominator).toArray();
        double random = Math.random();
        for(int i = 0; i < probabilities.length; i++) {
            random -= probabilities[i];
            if(random <= 0) {
                return toSelect.get(i);
            }
        }
        return toSelect.get(toSelect.size() - 1);
    }

    @Override
    public int getTeam() {
        return team;
    }

    @Override
    public Board getBoard() {
        return board;
    }
}
