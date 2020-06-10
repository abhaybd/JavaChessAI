package com.coolioasjulio.chess.players;

import com.coolioasjulio.chess.selectors.SoftplusSelector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.logging.Logger;

import com.coolioasjulio.chess.Board;
import com.coolioasjulio.chess.Move;
import com.coolioasjulio.chess.MoveCandidate;
import com.coolioasjulio.chess.heuristics.Heuristic;
import com.coolioasjulio.chess.heuristics.MaterialHeuristic;
import com.coolioasjulio.chess.selectors.GreedySelector;
import com.coolioasjulio.chess.selectors.RandomSelector;
import com.coolioasjulio.chess.selectors.Selector;
import com.coolioasjulio.configuration.ConfigurationMenu;
import com.coolioasjulio.configuration.Setting;
import com.coolioasjulio.configuration.Setting.InputType;

public class MinimaxComputerPlayer extends Player {
    private static final int DEFAULT_KEEP_MOVES = 3;
    private static final int DEFAULT_SEARCH_DEPTH = 2;
    private static final Logger logger = Logger.getLogger("MinimaxComputerPlayer");

    private static List<Selector> selectors = new ArrayList<>(
            Arrays.asList(new SoftplusSelector(), new RandomSelector(), new GreedySelector()));

    public static void addSelectorChoice(Selector selector) {
        selectors.add(selector);
    }

    public static void removeSelectorChoice(Selector selector) {
        selectors.remove(selector);
    }

    private Heuristic heuristic;
    private int keepMoves;
    private Selector selector;
    private int depth;

    public MinimaxComputerPlayer(Board board) {
        super(board);
        this.board = board;
        this.depth = DEFAULT_SEARCH_DEPTH;
        setHeuristic(new MaterialHeuristic(0.0)).setKeepMoves(DEFAULT_KEEP_MOVES).setSelector(new SoftplusSelector());
        ConfigurationMenu.addConfigMenu(createConfigurationMenu());
    }

    public MinimaxComputerPlayer setHeuristic(Heuristic heuristic) {
        this.heuristic = heuristic;
        return this;
    }

    public MinimaxComputerPlayer setKeepMoves(int keepMoves) {
        this.keepMoves = keepMoves;
        return this;
    }

    public MinimaxComputerPlayer setSelector(Selector selector) {
        this.selector = selector;
        return this;
    }

    public int getKeepMoves() {
        return keepMoves;
    }

    public Selector getSelector() {
        return selector;
    }

    public void setSearchDepth(int depth) {
        this.depth = depth;
    }

    public int getSearchDepth() {
        return depth;
    }

    private ConfigurationMenu createConfigurationMenu() {
        return new ConfigurationMenu("BotLvl2",
                new Setting<>("Keep Moves", InputType.INTEGER, this::setKeepMoves, this::getKeepMoves),
                new Setting<>("Selector", this::setSelector, this::getSelector,
                        selectors.toArray(new Selector[0])),
                new Setting<>("Search Depth", InputType.INTEGER, this::setSearchDepth, this::getSearchDepth)
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
        List<MoveCandidate> bestMoves = ForkJoinPool.commonPool().invoke(new MinimaxRecursiveTask(board, depth, team));

        int toKeep = Math.min(keepMoves, bestMoves.size());
        logger.info(bestMoves.toString());
        List<MoveCandidate> kept = bestMoves.subList(0, toKeep);
        logger.info(kept.toString());

        MoveCandidate bestMove = selector.select(kept, MoveCandidate::getScore);

        logger.info(String.format("%s - Score: %.2f\n", bestMove.getMove().toString(), bestMove.getScore()));
        return bestMove.getMove();
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
            if (depth == 0) {
                return work();
            } else {
                Collection<MinimaxRecursiveTask> futures = invokeAll(createSubtasks());
                List<MoveCandidate> candidates = new LinkedList<>();
                for (MinimaxRecursiveTask future : futures) {
                    if (future.getException() != null) {
                        future.getException().printStackTrace();
                        throw new RuntimeException(future.getException());
                    }
                    List<MoveCandidate> possibleMoves = future.join();
                    double score = possibleMoves.isEmpty() ?
                            heuristic.getScore(future.board, playerTeam) :
                            possibleMoves.get(0).getScore();
                    candidates.add(new MoveCandidate(future.move, score));
                }
                return ordered(candidates);
            }
        }

        private List<MinimaxRecursiveTask> createSubtasks() {
            Move[] moves = board.getMoves(team);
            List<MinimaxRecursiveTask> subtasks = new ArrayList<>(moves.length);
            for (Move m : moves) {
                Board boardCopy = board.fork();
                boardCopy.doMove(m);

                if (boardCopy.inCheck(team)) {
                    continue;
                }

                MinimaxRecursiveTask task = new MinimaxRecursiveTask(boardCopy, depth - 1, -team, m);
                subtasks.add(task);
            }
            return subtasks;
        }

        private List<MoveCandidate> work() {
            Move[] moves = board.getMoves(team);
            List<MoveCandidate> candidates = new ArrayList<>(moves.length);
            for (Move m : moves) {
                Board boardCopy = board.fork();

                boardCopy.doMove(m);

                if (boardCopy.inCheck(team)) {
                    continue;
                }

                candidates.add(new MoveCandidate(m, heuristic.getScore(boardCopy, playerTeam)));
            }

            return ordered(candidates);
        }

        private List<MoveCandidate> ordered(List<MoveCandidate> moves) {
            Comparator<MoveCandidate> comparator;
            if (team == playerTeam) {
                comparator = Comparator.comparing(MoveCandidate::getScore).reversed();
            } else if (team == -playerTeam) {
                comparator = Comparator.comparing(MoveCandidate::getScore);
            } else {
                throw new IllegalArgumentException("Team must be -1 or 1!");
            }

            if (move == null) { // this is an identifier of the root node
                moves.sort(comparator);
                return moves;
            } else {
                return moves.isEmpty() ? moves : Collections.singletonList(moves.stream().min(comparator).get());
            }
        }
    }
}
