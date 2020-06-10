package com.coolioasjulio.chess;

import com.coolioasjulio.chess.pieces.Piece;

/**
 * Represents a certain attribute that has different values for each player.
 * Useful to prevent code duplication and to cache values for each player.
 *
 * @param <T> The generic type of the value to store
 */
public class TeamValue<T> {
    private T whiteVal;
    private T blackVal;

    /**
     * Create a new TeamValue object, with both white and black values uninitialized.
     */
    public TeamValue() {
    }

    /**
     * Create a new TeamValue object, initializing both white and black.
     *
     * @param whiteVal The white value.
     * @param blackVal The black value.
     */
    public TeamValue(T whiteVal, T blackVal) {
        this.whiteVal = whiteVal;
        this.blackVal = blackVal;
    }

    /**
     * Get the value for the specified team.
     *
     * @param team The team to get the value for,
     *             as defined in the {@link com.coolioasjulio.chess.pieces.Piece Piece} class.
     * @return The value for the specified team.
     */
    public T get(int team) {
        if (team == Piece.WHITE) return whiteVal;
        else if (team == Piece.BLACK) return blackVal;
        else throw new IllegalArgumentException("Invalid team: " + team);
    }

    /**
     * Check if there is a stored value for the specified team.
     *
     * @param team The team to get the value for,
     *             as defined in the {@link com.coolioasjulio.chess.pieces.Piece Piece} class.
     * @return True if there is a stored value, false otherwise.
     */
    public boolean hasValue(int team) {
        return get(team) != null;
    }

    /**
     * Sets the value for a team.
     *
     * @param team The team to set the value for,
     *             as defined in the {@link com.coolioasjulio.chess.pieces.Piece Piece} class.
     * @param val  The value to set for the specified team.
     * @return The value that was just set. This is what was passed to val.
     */
    public T set(int team, T val) {
        if (team == Piece.WHITE) return whiteVal = val;
        else if (team == Piece.BLACK) return blackVal = val;
        else throw new IllegalArgumentException("Invalid team: " + team);
    }

    /**
     * Remove the stored value for both teams.
     */
    public void clear() {
        whiteVal = blackVal = null;
    }
}
