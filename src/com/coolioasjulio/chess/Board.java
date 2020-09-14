package com.coolioasjulio.chess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final List<Piece> pieces;
    private final TeamValue<Boolean> cachedCheckmate = new TeamValue<>();
    private final TeamValue<Boolean> cachedStalemate = new TeamValue<>();
    private final TeamValue<Boolean> cachedCheck = new TeamValue<>();
    private final TeamValue<King> cachedKing = new TeamValue<>();
    private final TeamValue<Move[]> cachedMoves = new TeamValue<>();
    private final Map<Square, Piece> boardMap = new HashMap<>();
    private final List<Move> moveHistory = new ArrayList<>();
    private final Map<PositionFingerprint, Integer> positionCount = new HashMap<>();

    /**
     * Create a new empty board.
     */
    public Board() {
        pieces = new ArrayList<>();
    }

    public boolean isDrawByThreeFoldRepetition() {
        return positionCount.getOrDefault(new PositionFingerprint(this), 0) >= 3;
    }

    /**
     * Get all pieces on this board.
     *
     * @return A reference to the underlying pieces list.
     */
    public List<Piece> getPieces() {
        return pieces;
    }

    public List<Move> getMoveHistory() {
        return moveHistory;
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
                moves.add(new KingSideCastle(k));
            }
            if (canQueenSideCastle(k)) {
                moves.add(new QueenSideCastle(k));
            }
        }
        return moves.toArray(new Move[0]);
    }

    public String getFEN() {
        StringBuilder sb = new StringBuilder();
        for (int y = 8; y > 0; y--) {
            int accum = 0;
            for (int x = 0; x < 8; x++) {
                Piece p = checkSquare(new Square(x, y));
                if (p != null) {
                    if (accum > 0) {
                        sb.append(accum);
                        accum = 0;
                    }
                    String type = p.getType();
                    String symbol = type.length() == 0 ? "p" : type;
                    sb.append(p.getTeam() == Piece.WHITE ? symbol.toUpperCase() : symbol.toLowerCase());
                } else {
                    accum++;
                }
            }
            if (accum > 0) {
                sb.append(accum);
            }
            if (y > 1) {
                sb.append("/");
            }
        }

        sb.append(" ");

        if (moveHistory.isEmpty()) {
            sb.append("w");
        } else {
            sb.append(moveHistory.get(moveHistory.size() - 1).team == Piece.WHITE ? "b" : "w");
        }

        sb.append(" - - 0 ");
        sb.append((moveHistory.size() / 2) + 1);

        return sb.toString();
    }

    public void addPiece(Piece piece) {
        pieces.add(piece);
        clearCache();
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
        if (king.hasMoved()) return false;
        Piece p = checkSquare(new Square(7, king.getSquare().getY()));
        if (!(p instanceof Rook) || p.getTeam() != king.getTeam()) return false;
        Rook r = (Rook) p;
        return !r.hasMoved() && !inCheck(king) && clearCastlePath(king.getSquare(), r.getSquare(), king.getTeam());
    }

    /**
     * Check if the specified king can make a legal queenside castle move.
     *
     * @param king The king to check for.
     * @return True if the king can make a legal queenside castle, false otherwise.
     */
    public boolean canQueenSideCastle(King king) {
        if (king.hasMoved()) return false;
        Piece p = checkSquare(new Square(0, king.getSquare().getY()));
        if (!(p instanceof Rook) || p.getTeam() != king.getTeam()) return false;
        Rook r = (Rook) p;
        return !r.hasMoved() && !inCheck(king) && clearCastlePath(r.getSquare(), king.getSquare(), king.getTeam());
    }

    /**
     * Check if the specified team has been stalemated.
     *
     * @param team The team to check.
     * @return True if the specified team has been stalemated, false otherwise.
     */
    public boolean inStaleMate(int team) {
        if (cachedStalemate.hasValue(team)) return cachedStalemate.get(team);

        boolean stalemate = isDrawByThreeFoldRepetition();

        if (!stalemate && !inCheck(team)) {
            stalemate = true;
            Move[] moves = getMoves(team);
            for (Move move : moves) {
                Board board = fork();
                board.doMove(move);
                if (!board.inCheck(team)) {
                    stalemate = false;
                    break;
                }
            }
        }

        return cachedStalemate.set(team, stalemate);
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
        return inCheck(king, false);
    }

    public boolean inCheck(King king, boolean ignoreCache) {
        if (king == null) throw new IllegalArgumentException("King must be non-null!");

        int team = king.getTeam();
        if (!ignoreCache && cachedCheck.hasValue(team)) return cachedCheck.get(team);

        Square square = king.getSquare();
        Piece[] pieces = {
                new Queen(square, team, this),
                new Rook(square, team, this),
                new Bishop(square, team, this),
                new Knight(square, team, this),
                new Pawn(square, team, this),
                new King(square, team, this)
        };

        boolean ret = false;
        for (Piece piece : pieces) {
            if (Arrays.stream(piece.getMoves()).filter(Move::isCapture).anyMatch(m -> checkSquare(m.getEnd()).getType().equals(piece.getType()))) {
                ret = true;
                break;
            }
        }

        return ignoreCache ? ret : cachedCheck.set(team, ret);
    }

    /**
     * Do the specified move on the board.
     *
     * @param move The move to do.
     * @throws InvalidMoveException If the move is invalid for any reason.
     */
    public void doMove(Move move) {
        // This is handled in the move class so special moves can override this behavior
        move.doMove(this);

        moveHistory.add(move);
        PositionFingerprint fingerprint = new PositionFingerprint(this);
        positionCount.put(fingerprint, 1 + positionCount.getOrDefault(fingerprint, 0));
        clearCache();
    }

    /**
     * Fork the board. This creates a deep copy that can be modified without modifying this board.
     *
     * @return A deep copy of this board.
     */
    public Board fork() {
        Board copy = new Board();
        copy.restoreState(saveState());
        return copy;
    }

    /**
     * Create a deep copy of the piece representation, to store as a frozen state.
     *
     * @return The frozen state of the current state of the board.
     */
    public BoardState saveState() {
        List<Piece> p = pieces.stream().map(Piece::copy).collect(Collectors.toList());
        return new BoardState(new ArrayList<>(moveHistory), p, new HashMap<>(positionCount));
    }

    /**
     * Revert the state of the board to the specified state.
     *
     * @param state The sparse piece representation to revert to.
     */
    public void restoreState(BoardState state) {
        pieces.clear();
        pieces.addAll(state.pieces);
        pieces.forEach(p -> p.setBoard(this));
        moveHistory.clear();
        moveHistory.addAll(state.moveHistory);
        positionCount.clear();
        positionCount.putAll(state.positionCount);
        clearCache();
    }

    public void clearCache() {
        cachedCheck.clear();
        cachedCheckmate.clear();
        cachedStalemate.clear();
        cachedMoves.clear();
        cachedKing.clear();
        boardMap.clear();
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
            if (checkSquare(check) != null || inCheck(k, true)) {
                return false;
            }
        }
        return true;
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

    public static class BoardState {
        public final List<Move> moveHistory;
        public final Map<PositionFingerprint,Integer> positionCount;
        public final List<Piece> pieces;

        public BoardState(List<Move> moveHistory, List<Piece> pieces, Map<PositionFingerprint, Integer> positionCount) {
            this.moveHistory = moveHistory;
            this.pieces = pieces;
            this.positionCount = positionCount;
        }
    }

    public static class PositionFingerprint {
        private final int teamToMove;
        private final TeamValue<long[]> bitboards;

        public PositionFingerprint(Board board) {
            if (board.moveHistory.size() == 0) {
                teamToMove = Piece.WHITE;
            } else {
                teamToMove = -board.moveHistory.get(board.moveHistory.size() - 1).getTeam();
            }
            bitboards = new TeamValue<>(new long[6], new long[6]);
            for (Piece piece : board.getPieces()) {
                int i = pieceIndex(piece.getType());
                Square square = piece.getSquare();
                int pos = square.getX() + (square.getY() - 1) * 8;
                bitboards.get(piece.getTeam())[i] |= 1L << pos;
            }
        }

        private int pieceIndex(String type) {
            return "_NBRQK".indexOf(type); // indexOf("") returns 0, _ at beginning so rest of pieces start at 1
        }

        @Override
        public int hashCode() {
            return Objects.hash(teamToMove, Arrays.hashCode(bitboards.get(Piece.WHITE)), Arrays.hashCode(bitboards.get(Piece.BLACK)));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PositionFingerprint other = (PositionFingerprint) o;
            if (teamToMove != other.teamToMove) return false;
            for (int team : new int[]{Piece.WHITE,Piece.BLACK}) {
                if (!Arrays.equals(bitboards.get(team), other.bitboards.get(team))) return false;
            }
            return true;
        }
    }
}
