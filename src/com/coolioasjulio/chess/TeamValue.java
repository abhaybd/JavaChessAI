package com.coolioasjulio.chess;

import com.coolioasjulio.chess.pieces.Piece;

public class TeamValue<T> {
    private T whiteVal;
    private T blackVal;

    public TeamValue() {
    }

    public TeamValue(T whiteVal, T blackVal) {
        this.whiteVal = whiteVal;
        this.blackVal = blackVal;
    }

    public T get(int team) {
        if (team == Piece.WHITE) return whiteVal;
        else if (team == Piece.BLACK) return blackVal;
        else throw new IllegalArgumentException("Invalid team: " + team);
    }

    public boolean hasValue(int team) {
        return get(team) != null;
    }

    public T set(int team, T val) {
        if (team == Piece.WHITE) return whiteVal = val;
        else if (team == Piece.BLACK) return blackVal = val;
        else throw new IllegalArgumentException("Invalid team: " + team);
    }

    public void clear() {
        whiteVal = blackVal = null;
    }
}
