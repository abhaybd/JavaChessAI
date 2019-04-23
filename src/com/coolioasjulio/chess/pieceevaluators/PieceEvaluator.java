package com.coolioasjulio.chess.pieceevaluators;

import com.coolioasjulio.chess.Board;
import com.coolioasjulio.chess.pieces.Piece;

public interface PieceEvaluator {
    
    /**
     * Get the individual value of a piece. The piece is considered individually.
     * 
     * @param piece The piece to get the value of.
     * @return The value of the piece.
     */
    double getValue(Piece piece);

    /**
     * Get the material value for a team. This may not be equal to the sum of all individual piece values.
     * 
     * @param board The board to look at.
     * @param team The team to return the score for.
     * @return The material score for the team.
     */
    default double getMaterialValue(Board board, int team) {
        return board.getPieces().stream().filter(e -> e.getTeam() == team).mapToDouble(this::getValue).sum();
    }
}
