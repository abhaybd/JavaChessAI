package com.coolioasjulio.chess.players;

import java.util.HashMap;
import java.util.logging.Logger;

import com.coolioasjulio.chess.Board;
import com.coolioasjulio.chess.Move;
import com.coolioasjulio.chess.exceptions.InvalidMoveException;
import com.coolioasjulio.chess.heuristics.Heuristic;
import com.coolioasjulio.chess.heuristics.MaterialHeuristic;
import com.coolioasjulio.chess.pieceevaluators.PieceEvaluator;
import com.coolioasjulio.chess.pieceevaluators.PositionalPieceEvaluator;
import com.coolioasjulio.chess.pieces.Pawn;
import com.coolioasjulio.chess.pieces.Piece;

public class PositionalComputerPlayer extends Player {
    private static final double SPACE_SCORE = 0.02;

    private Heuristic heuristic;
    private PieceEvaluator pieceEvaluator;

    /**
     * @param board the board object that this opponent is playing on
     */
    public PositionalComputerPlayer(Board board) {
        super(board);
        heuristic = new MaterialHeuristic(SPACE_SCORE);
        pieceEvaluator = new PositionalPieceEvaluator();
    }

    private int numAttackers(Move move) {
        Board board = this.board.fork();
        board.doMove(move);
        int attackers = 0;
        for (int i = 0; i < board.getPieces().size(); i++) {
            Piece p = board.getPieces().get(i);
            if (p.getTeam() == team)
                continue;
            Move[] moves = p.getMoves();
            for (Move m : moves) {
                if (m.getEnd().equals(move.getEnd())) {
                    attackers++;
                }
            }
        }
        return attackers;
    }

    private int numDefenders(Move move) {
        Board board = this.board.fork();
        board.removePiece(move.getPiece());
        int defenders = 0;
        for (int i = 0; i < board.getPieces().size(); i++) {
            Piece p = board.getPieces().get(i);
            if (p.getTeam() != team)
                continue;
            Move[] moves = p.getMoves();
            for (Move m : moves) {
                if (m.getEnd().equals(move.getEnd())) {
                    defenders++;
                }
            }
        }
        return defenders;
    }

    private boolean safeMove(Move move) throws InvalidMoveException {
        Piece piece = move.getPiece();
        if (piece instanceof Pawn)
            return true;
        if (move.doesCapture()) {
            if (pieceEvaluator.getValue(board.checkSquare(move.getEnd())) >= pieceEvaluator.getValue(move.getPiece())) {
                return true;
            }
        }
        int numDefenders = numDefenders(move);
        int numAttackers = numAttackers(move);
        return numDefenders >= numAttackers;
    }

    @Override
    public Move getMove() {
        HashMap<Double, Move> moves = new HashMap<>();
        for (int i = 0; i < board.getPieces().size(); i++) {
            Piece p = board.getPieces().get(i);
            if (p.getTeam() != team)
                continue;
            Move[] possible = p.getMoves();
            for (Move m : possible) {
                Board board = this.board.fork();
                double score = 0;
                if (!safeMove(m)) {
                    score -= pieceEvaluator.getValue(m.getPiece());
                }
                board.doMove(m);
                score += heuristic.getScore(board, team);
                if (board.inCheckMate(-team))
                    score += 99999;
                boolean check = board.inCheck(board.getKing(team));
                if (!check) {
                    moves.put(score, m);
                }
            }
        }
        double bestScore = moves.keySet().stream().reduce(Math::max).orElseThrow(IllegalStateException::new);
        Move bestMove = moves.get(bestScore);
        Logger.getLogger("PositionalComputerPlayer").info(bestMove.toString() + " - Score: " + bestScore);
        return bestMove;
    }
}
