package com.coolioasjulio.chess;
import java.util.ArrayList;
import java.util.Arrays;

public class Queen extends Piece{
	public Queen(Square square, int team, Board board) {
		super(square, team, board);
	}
	
	public double getRawValue() {
		return Piece.QUEEN_VALUE;
	}
	
	public double getVanillaValue() {
		return Piece.VANILLA_QUEEN_VALUE;
	}

	@Override
	public Move[] getMoves() {
		ArrayList<Move> moves = new ArrayList<Move>();
		Square square = super.getSquare();
		int team = super.getTeam();
		Bishop b = new Bishop(square,team,board);
		moves.addAll(Arrays.asList(b.getMoves()));
		Rook r = new Rook(square,team,board);
		moves.addAll(Arrays.asList(r.getMoves()));
		for(int i = 0; i < moves.size(); i++){
			Move m = moves.get(i);
			moves.set(i,new Move(this, m.getStart(), m.getEnd(), m.doesCapture()));
		}
		return moves.toArray(new Move[0]);
	}

	public Queen copy() {
		return new Queen(this.square, this.team, this.board);
	}
}
