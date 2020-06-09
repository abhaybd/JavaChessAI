package com.coolioasjulio.chess.players;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.coolioasjulio.chess.Board;
import com.coolioasjulio.chess.Move;
import com.coolioasjulio.chess.MoveCandidate;
import com.coolioasjulio.chess.heuristics.Heuristic;
import com.coolioasjulio.chess.heuristics.MaterialHeuristic;
import com.coolioasjulio.chess.selectors.GreedySelector;
import com.coolioasjulio.chess.selectors.RandomSelector;
import com.coolioasjulio.chess.selectors.Selector;
import com.coolioasjulio.chess.selectors.SoftmaxSelector;
import com.coolioasjulio.configuration.ConfigurationMenu;
import com.coolioasjulio.configuration.Setting;
import com.coolioasjulio.configuration.Setting.InputType;

public class MinimaxComputerPlayer extends Player {
    private static final int DEFAULT_KEEP_MOVES = 3;
    private static final int DEFAULT_SEARCH_DEPTH = 2;
    private static final Logger logger = Logger.getLogger("MinimaxComputerPlayer");

    private static List<Selector> selectors = new ArrayList<>(
            Arrays.asList(new SoftmaxSelector(), new RandomSelector(), new GreedySelector()));

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
        setHeuristic(new MaterialHeuristic(0.0)).setKeepMoves(DEFAULT_KEEP_MOVES).setSelector(new SoftmaxSelector());
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
            return i % 2 == 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public Move getMove() {
        List<MoveCandidate> bestMoves = ForkJoinPool.commonPool().invoke(new MinimaxRecursiveTask(board, depth, team));

        int toKeep = Math.min(keepMoves, bestMoves.size());

        List<MoveCandidate> kept = bestMoves.stream().distinct().sorted(Comparator.comparing(MoveCandidate::getScore))
                .collect(Collectors.toList()).subList(0, toKeep);
        logger.info(kept.toString());

        MoveCandidate bestMove = selector.select(kept,
                kept.stream().map(e -> -e.getScore()).collect(Collectors.toList()));

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
            if (depth <= 1) {
                return work();
            } else {
                Collection<MinimaxRecursiveTask> futures = invokeAll(createSubtasks());
                List<MoveCandidate> candidates = new LinkedList<>();
                for (MinimaxRecursiveTask future : futures) {
                    List<MoveCandidate> possibleMoves = future.join();

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
            List<MoveCandidate> candidates = new LinkedList<>();
            for (Move m : moves) {
                Board boardCopy = board.fork();

                boardCopy.doMove(m);

                if (boardCopy.inCheck(team)) {
                    continue;
                }

                candidates.add(new MoveCandidate(m, heuristic.getScore(boardCopy, team)));
            }

            return ordered(candidates);
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
    }
}
