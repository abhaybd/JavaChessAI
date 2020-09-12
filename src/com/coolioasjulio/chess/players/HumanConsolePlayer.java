package com.coolioasjulio.chess.players;

import java.io.InputStream;
import java.util.Scanner;
import java.util.logging.Logger;

import com.coolioasjulio.chess.*;
import com.coolioasjulio.chess.exceptions.InvalidMoveException;
import com.coolioasjulio.chess.pieces.Piece;

public class HumanConsolePlayer extends Player {
    private final Board board;
    private final Scanner input;

    public HumanConsolePlayer(Board board, InputStream is) {
        super(board);
        this.board = board;
        input = new Scanner(is);
    }

    @Override
    public Move getMove() {
        Board board = this.board.fork();
        Logger.getLogger("HumanConsolePlayer").info("It is " + ((team == Piece.WHITE) ? "white" : "black")
                + "'s turn! Input move in long notation. Ex: Nb1-c3");
        String response = input.nextLine();
        Square start;
        Square end;
        char action;
        String type;

        if (response.equalsIgnoreCase("o-o")) {
            return new KingSideCastle(board.getKing(team));
        } else if (response.equalsIgnoreCase("o-o-o")) {
            return new QueenSideCastle(board.getKing(team));
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
        String pieceType = Piece.getType(p);
        if (p == null || pieceType == null || !pieceType.equalsIgnoreCase(type) || p.getTeam() != team)
            throw new InvalidMoveException();

        Move[] possibleMoves = p.getMoves();
        Move move = new Move(p, end, action == 'x');

        if (hasMove(possibleMoves, move)) {
            board.doMove(move);
        } else {
            throw new InvalidMoveException();
        }

        if (board.inCheck(team) || board.inCheckMate(team)) {
            throw new InvalidMoveException();
        }

        return move;
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
