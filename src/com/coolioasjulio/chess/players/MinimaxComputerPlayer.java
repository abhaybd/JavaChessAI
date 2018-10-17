package com.coolioasjulio.chess.players;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

import com.coolioasjulio.chess.Board;
import com.coolioasjulio.chess.Logger;
import com.coolioasjulio.chess.Move;
import com.coolioasjulio.chess.MoveCandidate;
import com.coolioasjulio.chess.Player;
import com.coolioasjulio.chess.heuristics.Heuristic;
import com.coolioasjulio.chess.heuristics.PositionalHeuristic;

public class MinimaxComputerPlayer extends Player {
    private static final int KEEP_MOVES = 3;
    private static final double SPACE_SCORE = 0.0;

    private long expiredTime;
    private Heuristic heuristic;

    public MinimaxComputerPlayer(Board board) {
        super(board);
        this.heuristic = new PositionalHeuristic(SPACE_SCORE);
        this.board = board;
    }

    @Override
    public Move getMove() {
        List<MoveCandidate> bestMoves = new ArrayList<MoveCandidate>();
        expiredTime = Long.MAX_VALUE; // System.currentTimeMillis() + TIMEOUT_MILLIS;
//        for (int depth = 2; System.currentTimeMillis() <= expiredTime; depth += 2) {
//            Logger.getGlobalLogger().log("Searching with depth: " + depth);
//            MinimaxRecursiveTask task = new MinimaxRecursiveTask(board, depth, team);
//            List<MoveCandidate> moves = ForkJoinPool.commonPool().invoke(task);
//            if (moves != null) {
//                bestMoves.addAll(moves);
//            } else {
//                break;
//            }
//        }

        bestMoves.addAll(ForkJoinPool.commonPool().invoke(new MinimaxRecursiveTask(board, 2, team)));

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

    private class MinimaxRecursiveTask extends RecursiveTask<List<MoveCandidate>> {
        private static final long serialVersionUID = 1L;

        private Board board;
        private int depth;
        private int team;
        private Move move;
        private int playerTeam;

        public MinimaxRecursiveTask(Board board, int depth, int team) {
            this(board, depth, team, null);
        }

        public MinimaxRecursiveTask(Board board, int depth, int team, Move move) {
            this.board = board;
            this.depth = depth;
            this.team = team;
            this.move = move;
            playerTeam = MinimaxComputerPlayer.this.team;
        }

        @Override
        protected List<MoveCandidate> compute() {
            if (depth <= 1) {
                return work();
            } else {
                if (System.currentTimeMillis() > expiredTime) {
                    return null;
                }

                Collection<MinimaxRecursiveTask> futures = invokeAll(createSubtasks());
                List<MoveCandidate> candidates = new LinkedList<>();
                for (MinimaxRecursiveTask future : futures) {
                    List<MoveCandidate> possibleMoves = future.join();

                    // Timeout, so the search was aborted
                    if (possibleMoves == null) {
                        return null;
                    }

                    // If you detect a checkmate, return it immediately.
                    if (possibleMoves.size() == 0) {
                        return Arrays.asList(new MoveCandidate(future.move, team == playerTeam ? -1000 : 1000));
                    }

                    MoveCandidate mc = possibleMoves.get(0);
                    candidates.add(new MoveCandidate(future.move, mc.getScore()));
                }
                return ordered(candidates);
            }
        }

        private List<MinimaxRecursiveTask> createSubtasks() {
            Move[] moves = board.getMoves(team);
            List<MinimaxRecursiveTask> subtasks = new LinkedList<>();
            for (Move m : moves) {
                Board boardCopy = board.copy();
                boardCopy.doMove(m);

                if (boardCopy.inCheck(team)) {
                    continue;
                }

                MinimaxRecursiveTask task = new MinimaxRecursiveTask(boardCopy, depth - 1, -team, m);
                subtasks.add(task);
            }
            return subtasks;
        }

        private List<MoveCandidate> ordered(List<MoveCandidate> moves) {
            if (team == playerTeam) {
                return moves.stream().sorted(Comparator.comparing(MoveCandidate::getScore))
                        .collect(Collectors.toList());
            } else if (team == -playerTeam) {
                return moves.stream().sorted(Comparator.comparing(MoveCandidate::getScore).reversed())
                        .collect(Collectors.toList());
            } else {
                throw new IllegalArgumentException("Team must be -1 or 1!");
            }
        }

        private List<MoveCandidate> work() {
            Move[] moves = board.getMoves(team);
            List<MoveCandidate> candidates = new LinkedList<>();
            for (Move m : moves) {
                Board boardCopy = board.copy();

                boardCopy.doMove(m);

                if (boardCopy.inCheck(team)) {
                    continue;
                }

                candidates.add(new MoveCandidate(m, heuristic.getScore(boardCopy, team)));

                if (System.currentTimeMillis() > expiredTime) {
                    return null;
                }
            }

            return ordered(candidates);
        }
    }
}
