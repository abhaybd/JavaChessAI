package com.coolioasjulio.chess;
import java.util.ArrayList;

public class Knight extends Piece {

	public Knight(Square square, int team, Board board) {
		super(square, team, board);
	}
	
	public Knight(String square, int team, Board board) throws InvalidSquareException{
		super(square, team, board);
	}

	public double getRawValue() {
		return Piece.KNIGHT_VALUE;
	}	
	
	public double getVanillaValue() {
		return Piece.VANILLA_KNIGHT_VALUE;
	}
	
	
	public Move[] getMoves(){
		int team = super.getTeam();
		Square square = super.getSquare();
		ArrayList<Move> moves = new ArrayList<Move>();
		for(int i = -2; i <= 2; i++){
			if(i == 0||i+square.getX() < 0 || i+square.getX() > 7) {
				continue;
			}
			for(int j = -2; j <= 2; j++){
				if(j == 0 ||Math.abs(j) == Math.abs(i) || j + square.getY() < 1 || j+square.getY() > 8){
					continue;
				}
				else{
					try{
						Square toCheck = new Square(i+square.getX(),j+square.getY());
						Piece p = board.checkSquare(toCheck);
						if(p == null||(p != null && p.team != team)){
							boolean capture = (p != null && p.team != team);
							moves.add(new Move(this,square,toCheck,capture));
						}
					}
					catch(InvalidSquareException e){
						continue;
					}
				}
			}
		}
		return moves.toArray(new Move[0]);
	}
	
	public Knight copy() {
		return new Knight(this.square, this.team, this.board);
	}
}
