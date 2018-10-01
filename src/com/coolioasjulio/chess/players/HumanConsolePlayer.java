package com.coolioasjulio.chess.players;

import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import com.coolioasjulio.chess.Board;
import com.coolioasjulio.chess.InvalidMoveException;
import com.coolioasjulio.chess.Move;
import com.coolioasjulio.chess.Piece;
import com.coolioasjulio.chess.Player;
import com.coolioasjulio.chess.Square;

public class HumanConsolePlayer extends Player {
    private Board board;
    private Scanner input;

    public HumanConsolePlayer(Board board, InputStream is) {
        super(board);
        this.board = board;
        input = new Scanner(is);
    }

    @Override
    public Move getMove() {
        List<Piece> beforeState = board.saveState();
        try {
            System.out.println("It is " + ((team == Piece.WHITE) ? "white" : "black")
                    + "'s turn! Input move in long notation. Ex: Nb1-c3");
            String response = input.nextLine();
            Square start = null;
            Square end = null;
            char action = '-';
            String type = "";

            if (response.equalsIgnoreCase("o-o")) {
                return new Move(team, true);
            } else if (response.equalsIgnoreCase("o-o-o")) {
                return new Move(team, false);
            } else if (response.length() == 5) {
                start = Square.parseString(response.substring(0, 2));
                end = Square.parseString(response.substring(3));
                action = response.charAt(2);
                type = "";
            } else if (response.length() == 6) {
                start = Square.parseString(response.substring(1, 3));
                end = Square.parseString(response.substring(4));
                action = response.charAt(3);
                type = String.valueOf(response.charAt(0));
            } else
                throw new InvalidMoveException();

            if (action != 'x' && action != '-')
                throw new InvalidMoveException();

            Piece p = board.checkSquare(start);
            if (p == null || !Piece.getType(p).equalsIgnoreCase(type) || p.getTeam() != team)
                throw new InvalidMoveException();

            Move[] possibleMoves = p.getMoves();
            Move move = new Move(p, start, end, action == 'x');

            if (hasMove(possibleMoves, move)) {
                board.doMove(move);
            } else {
                throw new InvalidMoveException();
            }

            if (board.inCheck(team) || board.inCheckMate(team)) {
                throw new InvalidMoveException();
            }

            return move;
        } finally {
            board.restoreState(beforeState);
        }
    }

    private boolean hasMove(Move[] moves, Move move) {
        for (Move m : moves) {
            if (m.toString().equals(move.toString())) {
                return true;
            }
        }
        return false;
    }
}