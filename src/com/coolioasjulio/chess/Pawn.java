package com.coolioasjulio.chess;
import java.util.ArrayList;
import java.util.Scanner;

public class Pawn extends Piece{
	
	public Pawn(String square, int team, Board board) throws InvalidSquareException {
		super(square,team, board);
	}
	
	public Pawn(Square square, int team, Board board){
		super(square, team, board);
	}

	ArrayList<Move> addMove(ArrayList<Move> moves, Square end, boolean capture){
		try{
			Piece p = board.checkSquare(end);
			if(p == null && capture){
				return moves;
			}
			if((p == null && !capture) || (capture && p != null && p.team != team)){
				Move move = new Move(this,super.getSquare(),end,capture);
				moves.add(move);
			}
		}
		catch(Exception e){e.printStackTrace();}
		return moves;
	}
	
	public void promote(String type) throws InvalidMoveException{
		if(type.equals("Queen")){
			Queen q = new Queen(square,team,board);
			board.getPieces().add(q);
		}
		else if(type.equals("Rook")){
			Rook r = new Rook(square, team, board);
			board.getPieces().add(r);
		}
		else if(type.equals("Bishop")){
			Bishop b = new Bishop(square,team,board);
			board.getPieces().add(b);
		}
		else if(type.equals("Knight")){
			Knight n = new Knight(square, team, board);
			board.getPieces().add(n);
		}
		else throw new InvalidMoveException();
		board.removePiece(this);
	}
	
	@Override
	public void move(Square move, Scanner in) throws InvalidMoveException{
		super.move(move, in);
		if((move.getY() == 1 && team == Piece.BLACK) || (move.getY() == 8 && team == Piece.WHITE)){
			boolean done = false;
			while(!done){
				System.out.println("What would you like to promote your pawn to? queen, rook, bishop, or knight?");
				String promotion = in.nextLine().toLowerCase();
				promotion = String.valueOf(promotion.charAt(0)).toUpperCase() + promotion.substring(1);
				try {
					Class.forName(promotion);
					promote(promotion);
				} catch (ClassNotFoundException e) {
					System.out.println("Invalid piece!");
					continue;
				}
				done = true;
			}
		}
	}

	public double getRawValue() {
		return Piece.PAWN_VALUE;
	}
	
	public double getVanillaValue() {
		return Piece.VANILLA_PAWN_VALUE;
	}
	
	@Override
	public Move[] getMoves(){
		Square square = super.getSquare();
		int team = super.getTeam();
		int x = square.getX();
		boolean extra = (team == Piece.WHITE && square.getY() == 2) || (team == Piece.BLACK && square.getY() == 7);
		ArrayList<Move> moves = new ArrayList<Move>();
		try{
			moves = addMove(moves, new Square(x,square.getY()+team), false);
			if(extra){
				moves = addMove(moves,new Square(x,square.getY()+(team*2)),false);
			}
			try{
				moves = addMove(moves, new Square(square.getX()+1, square.getY() + team), true);				
			}
			catch(Exception e){}
			try{
				moves = addMove(moves, new Square(square.getX()-1, square.getY() + team), true);
			}
			catch(Exception e){}
		}
		catch(InvalidSquareException e){
			
		}
		return moves.toArray(new Move[0]);
	}
	
	public String toString(){
		return super.getSquare().toString();
	}
	
	public Pawn copy() {
		return new Pawn(this.square, this.team, this.board);
	}
}

