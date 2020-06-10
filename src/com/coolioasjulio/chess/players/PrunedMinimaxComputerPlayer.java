package com.coolioasjulio.chess.players;

import com.coolioasjulio.chess.Board;
import com.coolioasjulio.chess.Move;
import com.coolioasjulio.chess.MoveCandidate;
import com.coolioasjulio.chess.heuristics.Heuristic;
import com.coolioasjulio.chess.heuristics.MaterialHeuristic;
import com.coolioasjulio.configuration.ConfigurationMenu;
import com.coolioasjulio.configuration.Setting;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PrunedMinimaxComputerPlayer extends Player {
    private static final int DEFAULT_SEARCH_DEPTH = 4;

    private int depth = DEFAULT_SEARCH_DEPTH;
    private Heuristic heuristic = new MaterialHeuristic(0);
    private int cutoffs = 0;

    public PrunedMinimaxComputerPlayer(Board board) {
        super(board);
        ConfigurationMenu.addConfigMenu(createConfigurationMenu());
    }

    public int getSearchDepth() {
        return depth;
    }

    public void setSearchDepth(int depth) {
        this.depth = depth;
    }

    private ConfigurationMenu createConfigurationMenu() {
        return new ConfigurationMenu("BotLvl2.5",
                new Setting<>("Search Depth", Setting.InputType.INTEGER, this::setSearchDepth, this::getSearchDepth)
                        .setValidator(this::validSearchDepth));
    }

    private boolean validSearchDepth(String text) {
        try {
            if ("".equals(text)) {
                return true;
            }
            int i = Integer.parseInt(text);
            return i >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public Move getMove() {
        cutoffs = 0;
        MoveCandidate move = minimax(board, depth, team, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        Logger.getLogger("PrunedMinimaxComputerPlayer").info(move.toString());
        Logger.getLogger("PrunedMinimaxComputerPlayer").info("Cutoffs: " + cutoffs);
        return move.getMove();
    }

    public MoveCandidate minimax(Board board, int depth, int team, double alpha, double beta) {
        int playerTeam = this.team;
        Move[] allMoves = board.getMoves(team);
        Comparator<MinimaxTuple> c = Comparator.comparing(MinimaxTuple::getScore);
        if (team == playerTeam) c = c.reversed();
        List<MinimaxTuple> tuples = Arrays.stream(allMoves)
                .map(m -> MinimaxTuple.create(board, m, heuristic, playerTeam))
                .filter(Objects::nonNull)
                .sorted(c)
                .collect(Collectors.toList());
        MoveCandidate bestMove = null;
        for (MinimaxTuple tuple : tuples) {
            Board b = tuple.board;
            double score = tuple.getScore();
            if (depth != 0) {
                MoveCandidate mc = minimax(b, depth-1, -team, alpha, beta);
                if (mc != null) score = mc.getScore();
            }
            MoveCandidate candidate = new MoveCandidate(tuple.move, score);

            if (team == playerTeam) alpha = Math.max(score, alpha);
            else beta = Math.min(score, beta);

            if (bestMove == null) bestMove = candidate;
            else if (team == playerTeam && candidate.getScore() > bestMove.getScore()) {
                bestMove = candidate;
            } else if (team != playerTeam && candidate.getScore() < bestMove.getScore()) {
                bestMove = candidate;
            }

            if (beta <= alpha) {
                cutoffs++;
                break;
            }
        }
        return bestMove;
    }

    public static class MinimaxTuple {
        public static MinimaxTuple create(Board board, Move move, Heuristic heuristic, int playerTeam) {
            Board b = board.fork();
            b.doMove(move);
            if (b.inCheck(move.getTeam())) return null;
            double score = heuristic.getScore(b, playerTeam);
            MinimaxTuple tuple = new MinimaxTuple();
            tuple.board = b;
            tuple.move = move;
            tuple.score = score;
            return tuple;
        }

        private Board board;
        private Move move;
        private double score;

        public double getScore() {
            return score;
        }
    }
}
