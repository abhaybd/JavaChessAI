package com.coolioasjulio.chess.players;

import com.coolioasjulio.chess.Board;
import com.coolioasjulio.chess.Move;
import com.coolioasjulio.chess.MoveCandidate;
import com.coolioasjulio.chess.heuristics.Heuristic;
import com.coolioasjulio.chess.heuristics.MaterialHeuristic;
import com.coolioasjulio.configuration.ConfigurationMenu;
import com.coolioasjulio.configuration.Setting;
import java.util.logging.Logger;

public class PrunedMinimaxComputerPlayer extends Player {
    private static final int DEFAULT_SEARCH_DEPTH = 4;

    private int depth = DEFAULT_SEARCH_DEPTH;
    private Heuristic heuristic = new MaterialHeuristic(0);

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
            return i % 2 == 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public Move getMove() {
        MoveCandidate move = minimax(board, depth, team, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        Logger.getLogger("PrunedMinimaxComputerPlayer").info(move::toString);
        return move.getMove();
    }

    public MoveCandidate minimax(Board board, int depth, int team, double alpha, double beta) {
        Move[] moves = board.getMoves(team);
        int playerTeam = this.team;
        MoveCandidate bestMove = null;
        for (Move move : moves) {
            Board b = board.fork();
            b.doMove(move);
            if (b.inCheck(team)) continue;
            double score;
            if (depth == 0) {
                score = heuristic.getScore(b, team);
            } else {
                MoveCandidate mc = minimax(b, depth-1, -team, alpha, beta);
                score = mc == null ? heuristic.getScore(b, playerTeam) : mc.getScore();
            }
            MoveCandidate candidate = new MoveCandidate(move, score);

            if (team == playerTeam) alpha = Math.max(score, alpha);
            else beta = Math.min(score, beta);

            if (bestMove == null) bestMove = candidate;
            else if (team == playerTeam && candidate.getScore() > bestMove.getScore()) {
                bestMove = candidate;
            } else if (team != playerTeam && candidate.getScore() < bestMove.getScore()) {
                bestMove = candidate;
            }

            if (beta <= alpha) break;
        }
        return bestMove;
    }
}
