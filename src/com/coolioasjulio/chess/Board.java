package com.coolioasjulio.chess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.coolioasjulio.chess.exceptions.InvalidMoveException;
import com.coolioasjulio.chess.exceptions.InvalidSquareException;
import com.coolioasjulio.chess.pieceevaluators.VanillaPieceEvaluator;
import com.coolioasjulio.chess.pieces.Bishop;
import com.coolioasjulio.chess.pieces.King;
import com.coolioasjulio.chess.pieces.Knight;
import com.coolioasjulio.chess.pieces.Pawn;
import com.coolioasjulio.chess.pieces.Piece;
import com.coolioasjulio.chess.pieces.Queen;
import com.coolioasjulio.chess.pieces.Rook;

public class Board {
    private List<Piece> pieces;

    public Board() {
        pieces = new ArrayList<>();
    }

    public List<Piece> getPieces() {
        return pieces;
    }

    public List<Piece> getPieces(int team) {
        return pieces.stream().filter(p -> p.getTeam() == team).collect(Collectors.toList());
    }

    public Move[] getMoves(int team) {
        return getMoves(team, true);
    }

    public Move[] getMoves(int team, boolean castleMoves) {
        List<Move> moves = getPieces(team).stream().flatMap(p -> Arrays.asList(p.getMoves()).stream())
                .collect(Collectors.toList());
        if (castleMoves) {
            King k = getKing(team);
            if (canKingSideCastle(k)) {
                moves.add(new Move(team, true));
            }
            if (canQueenSideCastle(k)) {
                moves.add(new Move(team, false));
            }
        }
        return moves.toArray(new Move[0]);
    }

    public boolean removePiece(int i) {
        return pieces.remove(i) != null;
    }

    public boolean removePiece(Piece p) {
        return pieces.remove(p);
    }

    public Piece checkSquare(Square square) {
        try {
            for (Piece p : pieces) {
                if (p.getSquare().equals(square)) {
                    return p;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean canKingSideCastle(King k) {
        if (k.hasMoved() || inCheck(k))
            return false;
        Board board = k.getBoard();
        Piece p = board.checkSquare(new Square(7, k.getSquare().getY()));
        if (!(p instanceof Rook) || p.getTeam() != k.getTeam())
            return false;
        Rook r = (Rook) p;
        return !r.hasMoved() && freePath(k.getSquare(), r.getSquare(), k.getTeam());
    }

    public boolean canQueenSideCastle(King k) {
        if (k.hasMoved() || inCheck(k))
            return false;
        Board board = k.getBoard();
        Piece p = board.checkSquare(new Square(0, k.getSquare().getY()));
        if (!(p instanceof Rook) || p.getTeam() != k.getTeam())
            return false;
        Rook r = (Rook) p;
        return !r.hasMoved() && freePath(r.getSquare(), k.getSquare(), k.getTeam());
    }

    public boolean inStaleMate(int team) {
        if (inCheck(team)) {
            return false;
        }
        Move[] moves = getMoves(team);
        for (Move move : moves) {
            List<Piece> before = saveState();
            try {
                doMove(move);
                if (!inCheck(team)) {
                    return false;
                }
            } finally {
                restoreState(before);
            }
        }
        return true;
    }

    public boolean inCheckMate(int team) throws InvalidMoveException {
        if (!inCheck(team))
            return false;
        Move[] moves = getMoves(team);
        boolean checkMate = true;
        for (Move move : moves) {
            List<Piece> before = saveState();
            doMove(move);
            if (!inCheck(team))
                checkMate = false;
            restoreState(before);
        }
        return checkMate;
    }

    public King getKing(int team) {
        Optional<Piece> king = pieces.stream().filter(p -> p instanceof King && p.getTeam() == team).findFirst();
        if (king.isPresent())
            return (King) king.get();

        throw new InvalidMoveException("King not present on board!");
    }

    public boolean freePath(Square start, Square end, int team) {
        if (start.getY() != end.getY())
            throw new InvalidSquareException("Invalid castle path!");
        for (int i = start.getX() + 1; i < end.getX(); i++) {
            Square check = new Square(i, start.getY());
            King k = new King(check, team, this);
            if (checkSquare(check) != null || inCheck(k)) {
                return false;
            }
        }
        return true;
    }

    public boolean inCheck(int team) {
        return inCheck(getKing(team));
    }

    public boolean inCheck(King k) throws InvalidMoveException {
        if (k == null)
            throw new InvalidMoveException("Invalid king!");
        Move[] moves = getMoves(-k.getTeam(), false);
        for (Move m : moves) {
            if (m.getEnd().equals(k.getSquare())) {
                return true;
            }
        }
        return false;
    }

    public double getMaterialScore(int team) {
        return new VanillaPieceEvaluator().getMaterialValue(this, team);
    }

    public void doMove(Move m) {
        if (m.isCastle()) {
            King k = getKing(m.getTeam());
            if (m.isKingSideCastle() && canKingSideCastle(k)) {
                Rook r = (Rook) checkSquare(new Square(7, k.getSquare().getY()));
                k.move(new Square(6, k.getSquare().getY()));
                r.move(new Square(5, r.getSquare().getY()));
            } else if (m.isQueenSideCastle() && canQueenSideCastle(k)) {
                Rook r = (Rook) checkSquare(new Square(0, k.getSquare().getY()));
                k.move(new Square(2, k.getSquare().getY()));
                r.move(new Square(3, r.getSquare().getY()));
            } else {
                throw new InvalidMoveException("Cannot castle!");
            }
        } else {
            Piece p = this.checkSquare(m.getStart());
            List<Square> endSquares = Arrays.stream(p.getMoves()).map(Move::getEnd)
                    .collect(Collectors.toList());
            if (!endSquares.contains(m.getEnd())) {
                throw new InvalidMoveException("Invalid move or end square!");
            }
            if (m.doesCapture()) {
                removePiece(checkSquare(m.getEnd()));
            }
            p.move(m.getEnd());
        }
    }

    /*
     * Deep copy of the board.
     */
    public Board copy() {
        Board copy = new Board();
        copy.pieces = saveState();
        copy.pieces.forEach(p -> p.setBoard(copy));
        return copy;
    }

    /**
     * Deep copy of the pieces.
     *
     * @return List with a deep copy of the pieces.
     */
    public List<Piece> saveState() {
        return pieces.stream().map(Piece::copy).collect(Collectors.toList());
    }

    public void restoreState(List<Piece> state) {
        pieces.clear();
        pieces.addAll(state.stream().map(Piece::copy).collect(Collectors.toList()));
    }

    private void pawns() {
        for (int i = 0; i < 8; i++) {
            Square w = new Square(i, 2);
            Square b = new Square(i, 7);
            Pawn white = new Pawn(w, Piece.WHITE, this);
            Pawn black = new Pawn(b, Piece.BLACK, this);
            pieces.add(white);
            pieces.add(black);
        }
    }

    private void knights() {
        for (int i : new int[] { 1, 6 }) {
            Square w = new Square(i, 1);
            Square b = new Square(i, 8);
            Knight white = new Knight(w, Piece.WHITE, this);
            Knight black = new Knight(b, Piece.BLACK, this);
            pieces.add(white);
            pieces.add(black);
        }
    }

    private void rooks() {
        for (int i : new int[] { 0, 7 }) {
            Square w = new Square(i, 1);
            Square b = new Square(i, 8);
            Rook white = new Rook(w, Piece.WHITE, this);
            Rook black = new Rook(b, Piece.BLACK, this);
            pieces.add(white);
            pieces.add(black);
        }
    }

    private void bishops() {
        for (int i : new int[] { 2, 5 }) {
            Square w = new Square(i, 1);
            Square b = new Square(i, 8);
            Bishop white = new Bishop(w, Piece.WHITE, this);
            Bishop black = new Bishop(b, Piece.BLACK, this);
            pieces.add(white);
            pieces.add(black);
        }
    }

    private void queens() {
        Square w = new Square(3, 1);
        Square b = new Square(3, 8);
        Queen white = new Queen(w, Piece.WHITE, this);
        Queen black = new Queen(b, Piece.BLACK, this);
        pieces.add(white);
        pieces.add(black);
    }

    private void kings() {
        Square w = new Square(4, 1);
        Square b = new Square(4, 8);
        King white = new King(w, Piece.WHITE, this);
        King black = new King(b, Piece.BLACK, this);
        pieces.add(white);
        pieces.add(black);
    }

    public void setup() {
        try {
            pawns();
            knights();
            rooks();
            bishops();
            queens();
            kings();
        } catch (InvalidSquareException e) {
            System.out.println("Fatal Error!");
            System.exit(0);
        }
    }
}
