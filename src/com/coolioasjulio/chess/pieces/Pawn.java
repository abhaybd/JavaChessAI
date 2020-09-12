package com.coolioasjulio.chess.pieces;

import java.util.ArrayList;
import java.util.List;

import com.coolioasjulio.chess.Board;
import com.coolioasjulio.chess.Move;
import com.coolioasjulio.chess.Square;
import com.coolioasjulio.chess.exceptions.InvalidMoveException;

public class Pawn extends Piece {

    public Pawn(Square square, int team, Board board) {
        super(square, team, board);
    }

    public void promote(String type) throws InvalidMoveException {
        switch (type) {
            case "Queen":
                Queen q = new Queen(square, team, board);
                board.getPieces().add(q);
                break;
            case "Rook":
                Rook r = new Rook(square, team, board);
                board.getPieces().add(r);
                break;
            case "Bishop":
                Bishop b = new Bishop(square, team, board);
                board.getPieces().add(b);
                break;
            case "Knight":
                Knight n = new Knight(square, team, board);
                board.getPieces().add(n);
                break;
            default:
                throw new InvalidMoveException("Unrecognized piece!");
        }
        board.removePiece(this);
    }

    @Override
    public void move(Square move, String promotion) {
        super.move(move, promotion);
        if ((move.getY() == 1 && team == Piece.BLACK) || (move.getY() == 8 && team == Piece.WHITE)) {
            promotion = promotion == null ? "queen" : promotion;
            promotion = String.valueOf(promotion.charAt(0)).toUpperCase() + promotion.toLowerCase().substring(1);
            try {
                Class.forName(String.format("%s.%s", Pawn.class.getPackage().getName(), promotion));
                promote(promotion);
            } catch (ClassNotFoundException e) {
                throw new InvalidMoveException("Invalid piece for promotion: " + promotion);
            }
        }
    }

    public double getRawValue() {
        return Piece.PAWN_VALUE;
    }

    public double getVanillaValue() {
        return Piece.VANILLA_PAWN_VALUE;
    }

    private boolean between(int a, int min, int max) {
        return min <= a && a <= max;
    }

    @Override
    public Move[] getMoves() {
        int team = super.getTeam();
        int x = square.getX();
        int y = square.getY();
        List<Move> moves = new ArrayList<>();
        if (board.checkSquare(new Square(x, y + team)) == null) {
            moves.add(new Move(this, square, new Square(x, y + team)));
            boolean extra = (team == Piece.WHITE && square.getY() == 2) || (team == Piece.BLACK && square.getY() == 7);
            if (extra && board.checkSquare(new Square(x, y + 2 * team)) == null) {
                moves.add(new Move(this, square, new Square(x, y + 2 * team)));
            }
        }

        if (between(x - 1, 0, 7)) {
            Square end = new Square(x - 1, y + team);
            Piece p = board.checkSquare(end);
            if (p != null && p.team != team)
                moves.add(new Move(this, square, end, true));
        }
        if (between(x + 1, 0, 7)) {
            Square end = new Square(x + 1, y + team);
            Piece p = board.checkSquare(end);
            if (p != null && p.team != team)
                moves.add(new Move(this, square, end, true));
        }
        return moves.toArray(new Move[0]);
    }

    public String toString() {
        return super.getSquare().toString();
    }

    public Pawn copy() {
        return new Pawn(this.square, this.team, this.board);
    }
}