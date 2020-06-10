package com.coolioasjulio.chess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.coolioasjulio.chess.exceptions.InvalidMoveException;
import com.coolioasjulio.chess.exceptions.InvalidSquareException;
import com.coolioasjulio.chess.pieces.Bishop;
import com.coolioasjulio.chess.pieces.King;
import com.coolioasjulio.chess.pieces.Knight;
import com.coolioasjulio.chess.pieces.Pawn;
import com.coolioasjulio.chess.pieces.Piece;
import com.coolioasjulio.chess.pieces.Queen;
import com.coolioasjulio.chess.pieces.Rook;

public class Board {
    private List<Piece> pieces;
    private TeamValue<Boolean> cachedCheckmate = new TeamValue<>();
    private TeamValue<Boolean> cachedStalemate = new TeamValue<>();
    private TeamValue<Boolean> cachedCheck = new TeamValue<>();
    private TeamValue<King> cachedKing = new TeamValue<>();
    private TeamValue<Move[]> cachedMoves = new TeamValue<>();
    private Map<Square, Piece> boardMap = new HashMap<>();

    /**
     * Create a new empty board.
     */
    public Board() {
        pieces = new ArrayList<>();
    }

    /**
     * Get all pieces on this board.
     *
     * @return A reference to the underlying pieces list.
     */
    public List<Piece> getPieces() {
        return pieces;
    }

    /**
     * Get all pieces from the specified team.
     *
     * @param team The team from which to get the pieces.
     * @return A list of all Pieces on this board from the specified team.
     */
    public List<Piece> getPieces(int team) {
        return pieces.stream().filter(p -> p.getTeam() == team).collect(Collectors.toList());
    }

    /**
     * Get the possible moves for the specified team, including castle moves.
     *
     * @param team The team to check for.
     * @return All possible moves for the given team. Not all of these moves may be legal, as they may discover a check on their own king.
     */
    public Move[] getMoves(int team) {
        if (cachedMoves.hasValue(team)) return cachedMoves.get(team);

        Move[] moves = getMoves(team, true);
        return cachedMoves.set(team, moves);
    }

    /**
     * Get the possible moves for the specified team, optionally including castle moves.
     *
     * @param team        The team to check for.
     * @param castleMoves If true, include castle moves.
     * @return All possible moves for the given team. Not all of these moves may be legal, as they may discover a check on their own king.
     */
    public Move[] getMoves(int team, boolean castleMoves) {
        List<Move> moves = getPieces(team)
                .stream()
                .flatMap(p -> Arrays.stream(p.getMoves()))
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

    /**
     * Remove the specified piece from the board.
     *
     * @param piece The piece to remove from the board.
     */
    public void removePiece(Piece piece) {
        pieces.remove(piece);
        clearCache();
    }

    /**
     * Get the piece at the specified square.
     *
     * @param square The square to check for a piece.
     * @return The Piece at the specified square, or null if the square is empty.
     */
    public Piece checkSquare(Square square) {
        if (boardMap.isEmpty()) {
            for (Piece p : pieces) {
                boardMap.put(p.getSquare(), p);
            }
        }
        return boardMap.get(square);
    }

    /**
     * Check if the specified king can make a legal kingside castle move.
     *
     * @param king The king to check for.
     * @return True if the king can make a legal kingside castle, false otherwise.
     */
    public boolean canKingSideCastle(King king) {
        if (king.hasMoved() || inCheck(king)) return false;
        Piece p = checkSquare(new Square(7, king.getSquare().getY()));
        if (!(p instanceof Rook) || p.getTeam() != king.getTeam()) return false;
        Rook r = (Rook) p;
        return !r.hasMoved() && clearCastlePath(king.getSquare(), r.getSquare(), king.getTeam());
    }

    /**
     * Check if the specified king can make a legal queenside castle move.
     *
     * @param king The king to check for.
     * @return True if the king can make a legal queenside castle, false otherwise.
     */
    public boolean canQueenSideCastle(King king) {
        if (king.hasMoved() || inCheck(king)) return false;
        Piece p = checkSquare(new Square(0, king.getSquare().getY()));
        if (!(p instanceof Rook) || p.getTeam() != king.getTeam()) return false;
        Rook r = (Rook) p;
        return !r.hasMoved() && clearCastlePath(r.getSquare(), king.getSquare(), king.getTeam());
    }

    /**
     * Check if the specified team has been stalemated.
     *
     * @param team The team to check.
     * @return True if the specified team has been stalemated, false otherwise.
     */
    public boolean inStaleMate(int team) {
        if (cachedStalemate.hasValue(team)) return cachedStalemate.get(team);

        boolean ret = true;
        if (inCheck(team)) {
            ret = false;
        } else {
            Move[] moves = getMoves(team);
            for (Move move : moves) {
                Board board = fork();
                board.doMove(move);
                if (!board.inCheck(team)) {
                    ret = false;
                    break;
                }
            }
        }

        return cachedStalemate.set(team, ret);
    }

    /**
     * Check if the specified team has been checkmated.
     *
     * @param team The team to check.
     * @return True if the specified team has been checkmated, false otherwise.
     */
    public boolean inCheckMate(int team) {
        if (cachedCheckmate.hasValue(team)) return cachedCheckmate.get(team);

        boolean ret = true;
        if (!inCheck(team)) {
            ret = false;
        } else {
            Move[] moves = getMoves(team);
            for (Move move : moves) {
                Board board = fork();
                board.doMove(move);
                if (!board.inCheck(team)) {
                    ret = false;
                    break;
                }
            }
        }
        return cachedCheckmate.set(team, ret);
    }

    /**
     * Get the King of the specified team.
     *
     * @param team The team of which to get the king.
     * @return The King of the specified team.
     */
    public King getKing(int team) {
        if (cachedKing.hasValue(team)) return cachedKing.get(team);
        King king = (King) pieces.stream()
                .filter(p -> p instanceof King && p.getTeam() == team)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("King not present on board!"));
        return cachedKing.set(team, king);
    }

    /**
     * Check if the specified team is being checked.
     *
     * @param team The team to check if it's being checked.
     * @return True if this team is being checked, false otherwise.
     */
    public boolean inCheck(int team) {
        return cachedCheck.hasValue(team) ? cachedCheck.get(team) : inCheck(getKing(team));
    }

    /**
     * Check if the specified king is being attacked.
     *
     * @param king The king to check.
     * @return True if this king is being attacked by any opponent piece on the board, false otherwise.
     */
    public boolean inCheck(King king) {
        if (king == null) throw new IllegalArgumentException("King must be non-null!");

        int team = king.getTeam();
        if (cachedCheck.hasValue(team)) return cachedCheck.get(team);

        Move[] moves = getMoves(-team, false);
        boolean ret = false;
        for (Move m : moves) {
            if (m.getEnd().equals(king.getSquare())) {
                ret = true;
                break;
            }
        }
        return cachedCheck.set(team, ret);
    }

    /**
     * Do the specified move on the board.
     *
     * @param move The move to do.
     * @throws InvalidMoveException If the move is invalid for any reason.
     */
    public void doMove(Move move) {
        if (move.isCastle()) {
            King k = getKing(move.getTeam());
            if (move.isKingSideCastle() && canKingSideCastle(k)) {
                Rook r = (Rook) checkSquare(new Square(7, k.getSquare().getY()));
                k.move(new Square(6, k.getSquare().getY()));
                r.move(new Square(5, r.getSquare().getY()));
            } else if (move.isQueenSideCastle() && canQueenSideCastle(k)) {
                Rook r = (Rook) checkSquare(new Square(0, k.getSquare().getY()));
                k.move(new Square(2, k.getSquare().getY()));
                r.move(new Square(3, r.getSquare().getY()));
            } else {
                throw new InvalidMoveException("Cannot castle!");
            }
        } else {
            Piece p = this.checkSquare(move.getStart());
            List<Square> endSquares = Arrays.stream(p.getMoves()).map(Move::getEnd)
                    .collect(Collectors.toList());
            if (!endSquares.contains(move.getEnd())) {
                throw new InvalidMoveException("Invalid move or end square!");
            }
            if (move.doesCapture()) {
                Piece toCapture = checkSquare(move.getEnd());
                if (toCapture.getTeam() != p.getTeam()) {
                    removePiece(toCapture);
                }
            }
            p.move(move.getEnd());
        }

        clearCache();
    }

    /**
     * Fork the board. This creates a deep copy that can be modified without modifying this board.
     *
     * @return A deep copy of this board.
     */
    public Board fork() {
        Board copy = new Board();
        copy.pieces = saveState();
        copy.pieces.forEach(p -> p.setBoard(copy));
        return copy;
    }

    /**
     * Create a deep copy of the piece representation, to store as a frozen state.
     *
     * @return The frozen state of the current state of the board.
     */
    public List<Piece> saveState() {
        return pieces.stream().map(Piece::copy).collect(Collectors.toList());
    }

    /**
     * Revert the state of the board to the specified state.
     *
     * @param state The sparse piece representation to revert to.
     */
    public void restoreState(List<Piece> state) {
        pieces.clear();
        pieces.addAll(state.stream().map(Piece::copy).collect(Collectors.toList()));
        clearCache();
    }

    @Override
    public int hashCode() {
        return pieces.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Board)) return false;
        Board b = (Board) o;
        if (b.pieces.size() != pieces.size()) return false;
        for (int i = 0; i < pieces.size(); i++) {
            if (!b.pieces.get(i).equals(pieces.get(i))) return false;
        }
        return true;
    }

    private boolean clearCastlePath(Square start, Square end, int team) {
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

    private void clearCache() {
        cachedCheck.clear();
        cachedCheckmate.clear();
        cachedStalemate.clear();
        cachedMoves.clear();
        cachedKing.clear();
        boardMap.clear();
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
        for (int i : new int[]{1, 6}) {
            Square w = new Square(i, 1);
            Square b = new Square(i, 8);
            Knight white = new Knight(w, Piece.WHITE, this);
            Knight black = new Knight(b, Piece.BLACK, this);
            pieces.add(white);
            pieces.add(black);
        }
    }

    private void rooks() {
        for (int i : new int[]{0, 7}) {
            Square w = new Square(i, 1);
            Square b = new Square(i, 8);
            Rook white = new Rook(w, Piece.WHITE, this);
            Rook black = new Rook(b, Piece.BLACK, this);
            pieces.add(white);
            pieces.add(black);
        }
    }

    private void bishops() {
        for (int i : new int[]{2, 5}) {
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

    void setup() {
        pawns();
        knights();
        rooks();
        bishops();
        queens();
        kings();
    }
}
