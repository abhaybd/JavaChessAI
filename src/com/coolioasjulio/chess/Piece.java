package com.coolioasjulio.chess;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;

import javax.imageio.ImageIO;

public abstract class Piece {
	public static final int QUEEN_VALUE = 9;
	public static final int ROOK_VALUE = 5;
	public static final int BISHOP_VALUE = 3;
	public static final int KNIGHT_VALUE = 3;
	public static final int PAWN_VALUE = 1;
	
	public static final int white = 1;
	public static final int black = -1;
	public static String getType(Piece p){
		if(p instanceof Pawn){
			return "";
		}
		if(p instanceof Knight){
			return "N";
		}
		if(p instanceof Rook){
			return "R";
		}
		if(p instanceof Bishop){
			return "B";
		}
		if(p instanceof Queen){
			return "Q";
		}
		if(p instanceof King){
			return "K";
		}
		return null;
	}
	
	
	public static Image getImage(Piece p) throws IOException {
		String type = getType(p).toLowerCase();
		String name = type;
		if(type.length() == 0){
			name = "p";
		}
		String folder = "";
		if(p.getTeam() == Piece.white){
			name = "w"+ name;
			folder = "white/";
		}
		else{
			name = "b" + name;
			folder = "black/";
		}
		return ImageIO.read(new File(folder+name+".png")).getScaledInstance(100, 100, Image.SCALE_FAST);
	}
	public abstract Move[] getMoves();
	public abstract int getValue();
	Square square;
	int team;
	Board board;
	public Piece(String square,int team, Board board) throws InvalidSquareException{
		if(square.length() != 2 || Math.abs(team) != 1){
			throw new InvalidSquareException();
		}
		this.team = team;
		this.square = Square.parseString(square);
		this.board = board;
	}
	public Piece(Square square, int team, Board board){
		this.square = square;
		this.team = team;
		this.board = board;
	}
	@Override
	public boolean equals(Object o){
		return o.hashCode() == this.hashCode();
	}
	@Override
	public int hashCode(){
		return Objects.hash(team,square,board);
	}
	public void move(Square move) throws InvalidMoveException{
		move(move,null);
	}
	public void move(Square move,Scanner in) throws InvalidMoveException{
		Piece p = board.checkSquare(move);
		if(p != null && p.team == team){
			throw new InvalidMoveException();
		}
		square = move;
	}
	
	public abstract Piece copy();
	
	public boolean hasMove(Move move){
		Move[] moves = getMoves();
		for(Move m:moves){
			if(m.toString().equals(move.toString())) return true;
		}
		return false;
	}
	
	public Image getImage() throws IOException {
		return Piece.getImage(this);
	}
	
	public String getType() {
		return Piece.getType(this);
	}
	
	public Board getBoard(){
		return board;
	}
	public int getTeam(){
		return team;
	}
	public Square getSquare(){
		return square;
	}
	public String toString(){
		return Piece.getType(this) + square.getSquare();
	}
}
