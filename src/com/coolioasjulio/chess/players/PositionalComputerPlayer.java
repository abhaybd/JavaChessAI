package com.coolioasjulio.chess.players;

import java.util.HashMap;
import java.util.List;

import com.coolioasjulio.chess.Board;
import com.coolioasjulio.chess.InvalidMoveException;
import com.coolioasjulio.chess.Move;
import com.coolioasjulio.chess.Pawn;
import com.coolioasjulio.chess.Piece;
import com.coolioasjulio.chess.Player;

public class PositionalComputerPlayer extends Player {

    /**
     * 
     * @param board the board object that this opponent is playing on
     */
    public PositionalComputerPlayer(Board board) {
        super(board);
    }

    private int numAttackers(Move move) {
        List<Piece> before = board.saveState();
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
        board.restoreState(before);
        return attackers;
    }

    private int numDefenders(Move move) {
        List<Piece> before = board.saveState();
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
        board.restoreState(before);
        return defenders;
    }

    private boolean safeMove(Move move) throws InvalidMoveException {
        Piece piece = move.getPiece();
        if (piece instanceof Pawn)
            return true;
        if (move.doesCapture()) {
            if (board.checkSquare(move.getEnd()).getValue() >= move.getPiece().getValue()) {
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
                List<Piece> before = board.saveState();
                double score = 0;
                if (!safeMove(m)) {
                    score -= m.getPiece().getValue();
                }
                board.doMove(m);
                score += board.getScore(team);
                if (board.inCheckMate(-team))
                    score += 99999;
                boolean check = board.inCheck(board.getKing(team));
                if (!check) {
                    moves.put(score, m);
                }
                board.restoreState(before);
            }
        }
        double bestScore = moves.keySet().stream().reduce(Math::max).get();
        Move bestMove = moves.get(bestScore);
        System.out.println();
        System.out.println(bestMove.toString() + " - Score: " + bestScore);
        return bestMove;
    }
}
