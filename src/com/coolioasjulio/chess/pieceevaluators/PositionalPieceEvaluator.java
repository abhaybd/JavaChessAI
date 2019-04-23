package com.coolioasjulio.chess.pieceevaluators;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.coolioasjulio.chess.Board;
import com.coolioasjulio.chess.Square;
import com.coolioasjulio.chess.endgameevaluators.EndgameEvaluator;
import com.coolioasjulio.chess.endgameevaluators.SpeelmanEndgameEvaluator;
import com.coolioasjulio.chess.pieces.Piece;

public class PositionalPieceEvaluator implements PieceEvaluator {
    
    private static final double[][] kingTable = new double[][] {
        {-0.3,-0.4,-0.4,-0.5,-0.5,-0.4,-0.4,-0.3},
        {-0.3,-0.4,-0.4,-0.5,-0.5,-0.4,-0.4,-0.3},
        {-0.3,-0.4,-0.4,-0.5,-0.5,-0.4,-0.4,-0.3},
        {-0.3,-0.4,-0.4,-0.5,-0.5,-0.4,-0.4,-0.3},
        {-0.2,-0.3,-0.3,-0.4,-0.4,-0.3,-0.3,-0.2},
        {-0.1,-0.2,-0.2,-0.2,-0.2,-0.2,-0.2,-0.1},
        { -0.05, -0.05, -0.05,-0.1,-0.1, -0.05, -0.05, -0.05},
        { 0.2, 0.3, 0.3,  0,  0,  0, 0.3, 0.2}
    };
    
    private static final double[][] kingEndgameTable = new double[][] {
        {-.50,-.40,-.30,-.20,-.20,-.30,-.40,-.50},
        {-.30,-.20,-.10,   0,   0,-.10,-.20,-.30},
        {-.30,-.10, .20, .30, .30, .20,-.10,-.30},
        {-.30,-.10, .30, .40, .40, .30,-.10,-.30},
        {-.30,-.10, .30, .40, .40, .30,-.10,-.30},
        {-.30,-.10, .20, .30, .30, .20,-.10,-.30},
        {-.30,-.30,   0,   0,   0,   0,-.30,-.30},
        {-.50,-.30,-.30,-.30,-.30,-.30,-.30,-.50},
    };
    
    private static final double[][] queenTable = new double[][] {
        {-0.2,-0.1,-0.1, -0.05, -0.05,-0.1,-0.1,-0.2},
        {-0.1,  0,  0,  0,  0,  0,  0,-0.1},
        {-0.1,  0,  0.05,  0.05,  0.05,  0.05,  0,-0.1},
        { -0.05,  0,  0.05,  0.05,  0.05,  0.05,  0, -0.05},
        {  0,  0,  0.05,  0.05,  0.05,  0.05,  0, -0.05},
        {-0.1,  0.05,  0.05,  0.05,  0.05,  0.05,  0,-0.1},
        {-0.1,  0,  0.05,  0,  0,  0,  0,-0.1},
        {-0.2,-0.1,-0.1, -0.05, -0.05,-0.1,-0.1,-0.2}
    };
    
    private static final double[][] bishopTable = new double[][] {
        {-0.2,-0.1,-0.1,-0.1,-0.1,-0.1,-0.1,-0.2},
        {-0.1,  0,  0,  0,  0,  0,  0,-0.1},
        {-0.1,  0,  0.05, 0.1, 0.1,  0.05,  0,-0.1},
        {-0.1,  0.05,  0.05, 0.1, 0.1,  0.05,  0.05,-0.1},
        {-0.1,  0, 0.1, 0.1, 0.1, 0.1,  0,-0.1},
        {-0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1,-0.1},
        {-0.1,  0.05,  0,  0,  0,  0,  0.05,-0.1},
        {-0.2,-0.1,-0.1,-0.1,-0.1,-0.1,-0.1,-0.2}
    };
    
    private static final double[][] knightTable = new double[][] {
        {-0.5,-0.4,-0.3,-0.3,-0.3,-0.3,-0.4,-0.5},
        {-0.4,-0.2,   0,  0,  0,  0,-0.2,-0.4},
        {-0.3,   0, 0.1, 0.15, 0.15, 0.1,  0,-0.3},
        {-0.3, .05,  0.15, 0.2, 0.2, 0.15,  0.05,-0.3},
        {-0.3,   0, 0.15, 0.2, 0.2, 0.15,  0,-0.3},
        {-0.3, .05, 0.1, 0.15, 0.15, 0.1,  0.05,-0.3},
        {-0.4,-0.2,  0,  0.05,  0.05,  0,-0.2,-0.4},
        {-0.5,-0.4,-0.3,-0.3,-0.3,-0.3,-0.4,-0.5}
    };
    
    private static final double[][] rookTable = new double[][] {
        {     0,  0,  0,  0,  0,  0,  0,     0},
        {  0.05, .1, .1, .1, .1, .1, .1,  0.05},
        { -0.05,  0,  0,  0,  0,  0,  0, -0.05},
        { -0.05,  0,  0,  0,  0,  0,  0, -0.05},
        { -0.05,  0,  0,  0,  0,  0,  0, -0.05},
        { -0.05,  0,  0,  0,  0,  0,  0, -0.05},
        { -0.05,  0,  0,  0,  0,  0,  0, -0.05},
        {     0,  0,  0,.05,.05,  0,  0,     0}
    };
    
    private static final double[][] pawnTable = new double[][] {
        {   1.0,   1.0,   1.0,   1.0,   1.0,   1.0,   1.0,   1.0 },
        {   0.5,   0.5,   0.5,   0.5,   0.5,   0.5,   0.5,   0.5 },
        {   0.1,   0.1,   0.2,   0.3,   0.3,   0.2,   0.1,   0.1 },
        {  0.05,  0.05,   0.1,  0.25,  0.25,   0.1,  0.05,  0.05 },
        {     0,     0,     0,   0.2,   0.2,     0,     0,     0 },
        {  0.05, -0.05,  -0.1,     0,     0,  -0.1, -0.05,  0.05 },
        {  0.05,   0.1,   0.1,  -0.3,  -0.3,   0.1,   0.1,  0.05 },
        {     0,     0,     0,     0,     0,     0,     0,     0 }
    };

    private HashMap<String, double[][]> pieceSquareTables = new HashMap<>();
    private EndgameEvaluator endgameEvaluator = new SpeelmanEndgameEvaluator();

    /**
     * Create a positional piece evaluator using the default piece square tables.
     */
    public PositionalPieceEvaluator() {
        pieceSquareTables.put("wk", flipTable(kingTable));
        pieceSquareTables.put("wq", flipTable(queenTable));
        pieceSquareTables.put("wb", flipTable(bishopTable));
        pieceSquareTables.put("wn", flipTable(knightTable));
        pieceSquareTables.put("wr", flipTable(rookTable));
        pieceSquareTables.put("wp", flipTable(pawnTable));
        pieceSquareTables.put("wke", flipTable(kingEndgameTable));

        pieceSquareTables.put("bk", kingTable);
        pieceSquareTables.put("bq", queenTable);
        pieceSquareTables.put("bb", bishopTable);
        pieceSquareTables.put("bn", knightTable);
        pieceSquareTables.put("br", rookTable);
        pieceSquareTables.put("bp", pawnTable);
        pieceSquareTables.put("bke", kingEndgameTable);
    }

    /**
     * Create a positional piece evaluator using the supplied tables. There must be 12 tables, one per piece per team.
     * The key for each table is the first letter of the color and the notation letter for the piece, in lowercase.
     * Pawn is p. Ex: wk, wq, wn, wb, wp, bk, etc.
     * 
     * The tables MUST be in row x col format. The first dimension is row, the second is column.
     * 
     * @param tables The tables to use. Must be 12 entries in the map, in row x column format.
     */
    public PositionalPieceEvaluator(Map<String, double[][]> tables) {
        if (tables.size() < 12) {
            throw new IllegalArgumentException("Must have at least 12 tables! One per team per piece!");
        }

        for (String team : Arrays.asList("w", "b")) {
            for (String piece : Arrays.asList("p", "b", "n", "r", "k", "q")) {
                String name = team + piece;
                if (!tables.containsKey(name)) {
                    throw new IllegalArgumentException("Table not found: " + name);
                }
            }
        }
        this.pieceSquareTables = new HashMap<>(tables);
    }
    
    public void setEndgameEvaluator(EndgameEvaluator endgameEvaluator) {
        this.endgameEvaluator = endgameEvaluator;
    }

    @Override
    public double getValue(Piece piece) {
        String name = piece.getName();
        if (piece.getTeam() == Piece.WHITE) {
            name = "w" + name;
        } else if (piece.getTeam() == Piece.BLACK) {
            name = "b" + name;
        }

        double[][] table = pieceSquareTables.get(name);

        if (table == null) {
            throw new IllegalStateException("Table file not found for piece: " + piece.toString());
        }

        Square square = piece.getSquare();
        return piece.getRawValue() + table[square.getY() - 1][square.getX()]; // it's row col not x y.
    }
    
    @Override
    public double getMaterialValue(Board board, int team) {
        List<Piece> pieces = new LinkedList<>(board.getPieces(team));
        double score = 0;
        if(endgameEvaluator.inEndgame(board)) {
            Piece king = board.getKing(team);
            pieces.remove(king);
            String name = team == Piece.WHITE ? "wke" : "bke";
            double[][] table = pieceSquareTables.get(name);
            if(table != null) {
                score += king.getRawValue() + table[king.getSquare().getY()-1][king.getSquare().getX()];
            }
        }
        return score + pieces.stream().mapToDouble(this::getValue).sum();
    }

    private static double[][] flipTable(double[][] table) {
        double[][] flipped = new double[table.length][table[0].length];
        for (int row = 0; row < table.length; row++) {
            for (int col = 0; col < table[row].length; col++) {
                flipped[row][col] = table[table.length - 1 - row][col];
            }
        }
        return flipped;
    }
}
