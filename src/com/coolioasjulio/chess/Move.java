package com.coolioasjulio.chess;

import java.util.Collections;
import java.util.Objects;

public class Move {
	private Piece piece;
	private Square start, end;
	private String type;
	private boolean capture;
	private boolean kingSideCastle;
	private boolean queenSideCastle;
	public Move(Piece piece, Square start, Square end){
		this.piece = piece;
		this.start = start;
		this.end = end;
		this.type = Piece.getType(piece);
	}
	
	/**
	 * Done use a regular Move object. In this case, if true, castle king side. Else, castle queen side.
	 * @param kingSideCastle
	 */
	public Move(boolean kingSideCastle) {
		this.kingSideCastle = kingSideCastle;
		this.queenSideCastle = !kingSideCastle;
	}
	
	public Move(Piece piece, Square start, Square end, boolean capture){
		this.piece = piece;
		this.start = start;
		this.end = end;
		this.type = Piece.getType(piece);
		this.capture = capture;
	}
	
	public boolean isCastle() {
		return kingSideCastle || queenSideCastle;
	}
	
	public boolean isKingSideCastle() {
		return kingSideCastle;
	}
	
	public boolean isQueenSideCastle() {
		return queenSideCastle;
	}
	
	@Override
	public boolean equals(Object o){
		if(!(o instanceof Move)) {
			return false;
		}
		Move move = (Move)o;
		return this.piece.equals(move.piece) && 
				this.start.equals(move.start) && 
				this.end.equals(move.end) && 
				this.capture == move.capture;
	}
	
	@Override
	public int hashCode(){
		return Objects.hash(piece,start,end,capture);
	}
	
	public void setPiece(Piece piece){
		this.piece = piece;
	}
	public Piece getPiece(){
		return piece;
	}
	public Square getStart(){
		return start;
	}
	public Square getEnd(){
		return end;
	}
	public boolean doesCapture(){
		return capture;
	}
	public String shortNotation(){
		String aux = "";
		for(Piece p:Collections.synchronizedList(piece.getBoard().getPieces())){
			Move move = new Move(p,p.getSquare(),end);
			if(p.hasMove(move)){
				if(!p.equals(piece)){
					if(p.getSquare().getY() == piece.getSquare().getY()) aux = String.valueOf(p.getSquare().toString().charAt(0));
					else if(p.getSquare().getX() == piece.getSquare().getX()) aux = String.valueOf(p.getSquare().toString().charAt(1));
				}
			}
		}
		return type + aux + (capture?"x":"") + end.toString();
	}
	
	public boolean equals(Move m){
		return m.toString().equals(this.toString());
	}
	
	@Override
	public String toString(){
		String action = "-";
		if(capture){
			action = "x";
		}
		return type + start.toString() + action + end.toString();
	}
}