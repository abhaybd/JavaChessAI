package com.coolioasjulio.chess;
import java.util.ArrayList;

public class King extends Piece{

	public King(Square square, int team, Board board) {
		super(square, team, board);
	}

	public double getRawValue() {
		return Piece.KING_VALUE;
	}
	
	public double getVanillaValue() {
		return Piece.VANILLA_KING_VALUE;
	}

	@Override
	public Move[] getMoves() {
		Square square = super.getSquare();
		int team = super.getTeam();
		ArrayList<Move> moves = new ArrayList<Move>();
		for(int x = -1; x <= 1; x++){
			for(int y = -1; y <= 1; y++){
				if(x == 0 && y == 0) continue;
				try{
					Square s = new Square(x+square.getX(),y+square.getY());
					Piece p = board.checkSquare(s);
					boolean capture = (p != null && p.team != team);
					if(p == null || capture){
						Move move = new Move(this,square,s,capture);
						moves.add(move);
					}
				}
				catch(InvalidSquareException e){
					continue;
				}
			}
		}
		return moves.toArray(new Move[0]);
	}

	public King copy() {
		King k = new King(this.square, this.team, this.board);
		k.moved = this.moved;
		return k;
	}
}
