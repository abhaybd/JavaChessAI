package com.coolioasjulio.chess.players;

import com.coolioasjulio.chess.Board;
import com.coolioasjulio.chess.Move;
import com.coolioasjulio.chess.MoveCandidate;
import com.coolioasjulio.chess.heuristics.Heuristic;
import com.coolioasjulio.chess.heuristics.MaterialHeuristic;
import com.coolioasjulio.chess.pieceevaluators.PositionalPieceEvaluator;
import com.coolioasjulio.chess.pieces.Piece;
import com.coolioasjulio.configuration.ConfigurationMenu;
import com.coolioasjulio.configuration.Setting;

import java.util.*;
import java.util.logging.Logger;

public class PrunedMinimaxComputerPlayer extends Player {
    private static final int DEFAULT_SEARCH_DEPTH = 4;
    private static final int DEFAULT_MAX_SEARCH_DEPTH = 6;

    private int depth = DEFAULT_SEARCH_DEPTH;
    private int maxDepth = DEFAULT_MAX_SEARCH_DEPTH;
    private final Heuristic heuristic = new MaterialHeuristic(0);
    private int nodes;
    private int nonTerminalNodes;
    private int cacheHits;
    private final long[][][] historyScores = new long[2][64][64]; // 0=white, 1=black, a1=0,a2=1,...,h8=63
    private final Map<Transposition, MoveCandidate> transpositionTableW = new LimitedLinkedHashMap<>(16777216);
    private final Map<Transposition, MoveCandidate> transpositionTableB = new LimitedLinkedHashMap<>(16777216);

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

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    private ConfigurationMenu createConfigurationMenu() {
        return new ConfigurationMenu("BotLvl2.5",
                new Setting<>("Search Depth", Setting.InputType.INTEGER, this::setSearchDepth, this::getSearchDepth)
                        .setValidator(this::validSearchDepth),
                new Setting<>("Max Search Depth", Setting.InputType.INTEGER, this::setMaxDepth, this::getMaxDepth)
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
        nonTerminalNodes = 1;
        nodes = 1;
        cacheHits = 0;
        transpositionTableW.clear();
        transpositionTableB.clear();
        long start = System.currentTimeMillis();
        MoveCandidate move = minimax(board, depth, team, false, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        long end = System.currentTimeMillis();
        Logger logger = Logger.getLogger("PrunedMinimaxComputerPlayer");
        logger.info(move.toString());
        logger.info(String.format("Avg branching factor: %.2f", ((double) nodes) / nonTerminalNodes));
        logger.info("Cache hits: " + cacheHits);
        double elapsedSec = (end - start) / 1000.0;
        double rate = ((double) nodes) / elapsedSec;
        logger.info(String.format("Evaluated %d nodes in %.3f seconds, %.1f nodes/sec, %.1f leaves/sec\n",
                nodes, elapsedSec, rate, (nodes - nonTerminalNodes) / elapsedSec));
        transpositionTableW.clear();
        transpositionTableB.clear();
        return move.getMove();
    }

    private void sortMoves(Board board, Move[] moves, int team) {
        Comparator<Move> comparator = (m1, m2) -> {
            if (m1.equals(m2)) {
                return 0;
            } else if (m1.isCapture() != m2.isCapture()) {
                return m1.isCapture() ? -1 : 1;
            } else if (m1.isCapture()) {
                PositionalPieceEvaluator eval = new PositionalPieceEvaluator();
                double scoreDiff1 = eval.getValue(board.checkSquare(m1.getEnd())) - eval.getValue(board.checkSquare(m1.getStart()));
                double scoreDiff2 = eval.getValue(board.checkSquare(m2.getEnd())) - eval.getValue(board.checkSquare(m2.getStart()));

                if (scoreDiff1 > scoreDiff2) return -1;
                else if (scoreDiff1 < scoreDiff2) return 1;
            } else {
                int from1 = (m1.getStart().getY() - 1) * 8 + m1.getStart().getX();
                int from2 = (m2.getStart().getY() - 1) * 8 + m2.getStart().getX();
                int to1 = (m1.getEnd().getY() - 1) * 8 + m1.getEnd().getX();
                int to2 = (m2.getEnd().getY() - 1) * 8 + m2.getEnd().getX();
                int teamIndex = m1.getTeam() == Piece.WHITE ? 0 : 1;

                long history1 = historyScores[teamIndex][from1][to1];
                long history2 = historyScores[teamIndex][from2][to2];

                if (history1 > history2) return -1;
                else if (history1 < history2) return 1;
            }
            return m1.toString().compareTo(m2.toString());
        };

        if (team != this.team) {
            comparator = comparator.reversed();
        }

        Arrays.sort(moves, comparator);
    }

    public MoveCandidate minimax(Board board, int depth, int team, boolean didCapture, double alpha, double beta) {
        Transposition transposition = new Transposition(board);
        Map<Transposition, MoveCandidate> transpositionTable = team == Piece.WHITE ? transpositionTableW : transpositionTableB;
        if (transpositionTable.containsKey(transposition)) {
            cacheHits++;
            return transpositionTable.get(transposition);
        }

        int playerTeam = this.team;
        Move[] moves = board.getMoves(team);
        sortMoves(board, moves, team);
        MoveCandidate bestMove = null;
        for (Move move : moves) {
            nodes++;
            Board b = board.fork();
            b.doMove(move);
            if (b.inCheck(move.getTeam())) {
                continue;
            }
            double score = heuristic.getScore(b, playerTeam);
            if (depth > this.depth - this.maxDepth && (depth > 0 || didCapture)) {
                MoveCandidate mc = minimax(b, depth - 1, -team, move.isCapture(), alpha, beta);
                if (mc != null) {
                    score = mc.getScore();
                    nonTerminalNodes++;
                }
            }
            MoveCandidate candidate = new MoveCandidate(move, score);

            if (bestMove == null) bestMove = candidate;
            else if (team == playerTeam && candidate.getScore() > bestMove.getScore()) {
                bestMove = candidate;
            } else if (team != playerTeam && candidate.getScore() < bestMove.getScore()) {
                bestMove = candidate;
            }

            if (team == playerTeam) alpha = Math.max(candidate.getScore(), alpha);
            else beta = Math.min(candidate.getScore(), beta);

            if (beta <= alpha) {
                if (!move.isCapture()) {
                    int teamIndex = move.getTeam() == Piece.WHITE ? 0 : 1;
                    int from = (move.getStart().getY() - 1) * 8 + move.getStart().getX();
                    int to = (move.getEnd().getY() - 1) * 8 + move.getEnd().getX();
                    historyScores[teamIndex][from][to] += 1 << (this.depth - depth);
                }
                break;
            }
        }

        if (bestMove != null) {
            (team == Piece.WHITE ? transpositionTableW : transpositionTableB).put(new Transposition(board), bestMove);
        }

        return bestMove;
    }

    private static class LimitedLinkedHashMap<K, V> extends LinkedHashMap<K, V> {
        private final int maxSize;

        public LimitedLinkedHashMap(int maxSize) {
            super(maxSize);
            this.maxSize = maxSize;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > maxSize;
        }
    }

    private static class Transposition {
        public Board.PositionFingerprint fingerprint;
        public int occurrences;

        public Transposition(Board board) {
            this.fingerprint = board.getFingerprint();
            this.occurrences = board.getNumOccurrences(fingerprint);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Transposition that = (Transposition) o;
            return occurrences == that.occurrences &&
                    fingerprint.equals(that.fingerprint);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fingerprint, occurrences);
        }
    }
}
