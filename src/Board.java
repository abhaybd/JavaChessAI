import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.newdawn.slick.Color;

public class Board{
	public static final Color BROWN = new Color(107,54,54);
	public static final Color TAN = new Color(203, 177, 154);
	public static final double spaceScore = 0.5;
	public ArrayList<Piece> pieces;
	public Board(){
		pieces = new ArrayList<Piece>();
	}
	public List<Piece> getPieces(){
		return Collections.synchronizedList(pieces);
	}
	public Piece checkSquare(Square square){
		try{
		for(Piece p:Collections.synchronizedList(pieces)){
			if(p.getSquare().getX() == square.getX() && p.getSquare().getY() == square.getY()){
				return p;
			}
		}
		return null;
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	public boolean checkMate(King k) throws InvalidMoveException{
		Move[] Kmoves = k.getMoves();
		if(!inCheck(k)) return false;
		Square Kbefore = k.getSquare();
		for(int i = 0; i < Kmoves.length; i++){
			Move m = Kmoves[i];
			Piece captured = null;
			if(m.doesCapture()){
				captured = checkSquare(m.getEnd());
				getPieces().remove(captured);
			}
			//King toTest = new King(m.getEnd(),k.getTeam(),k.getBoard());
			k.move(m.getEnd());
			if(!inCheck(k)){
				k.move(Kbefore);
				if(m.doesCapture())	getPieces().add(captured);
				return false;
			}
			k.move(Kbefore);
			if(m.doesCapture())	getPieces().add(captured);
		}
		
		for(int i = 0; i < getPieces().size(); i++){
			Piece p = getPieces().get(i);
			if(p.getTeam() != k.getTeam()) continue;
			Move[] moves = p.getMoves();
			for(Move m:moves){
				Square before = p.getSquare();
				Piece captured = null;
				if(m.doesCapture()){
					captured = checkSquare(m.getEnd());
					getPieces().remove(captured);
				}
				p.move(m.getEnd());
				if(!inCheck(k)){
					p.move(before);
					if(m.doesCapture()) getPieces().add(captured);
					return false;
				}
				else {
					p.move(before);
					if(m.doesCapture()) getPieces().add(captured);
				}
			}
		}
		return true;
	}
	
	public King getKing(int team) throws InvalidMoveException{
		for(Piece p:Collections.synchronizedList(getPieces())){
			if(p instanceof King && p.team == team){
				return (King)p;
			}
		}
		throw new InvalidMoveException();
	}
	
	public boolean freePath(Square start, Square end, int team) throws InvalidSquareException, InvalidMoveException{
		if(start.getY() != end.getY()) throw new InvalidSquareException();
		for(int i = start.getX() + 1; i < end.getX(); i++){
			Square check = new Square(i,start.getY());
			King k = new King(check,team,this);
			if(checkSquare(check) != null || inCheck(k)){
				return false;
			}
		}
		return true;
	}
	
	public boolean inCheck(King k) throws InvalidMoveException{
		if(k == null) throw new InvalidMoveException();
		for(Piece p:Collections.synchronizedList(getPieces())){
			if(p.team == k.getTeam()) continue;
			Move[] moves = p.getMoves();
			for(Move m:moves){
				if(m.getEnd().equals(k.getSquare())){
					return true;
				}
			}
		}
		return false;
	}
	
	private int material(int team){
		int material = 0;
		for(Piece p:Collections.synchronizedList(getPieces())){
			if(p.getTeam() != team) continue;
			material += p.getValue();
		}
		return material;
	}
	
	public int getScore(int team, int lastMaterial){		
		int space = 0;
		int material = material(team) - lastMaterial;
		for(Piece p:Collections.synchronizedList(getPieces())){
			if(p.getTeam() != team) continue;
			space += p.getMoves().length * spaceScore;
		}
		return space + material;
	}
	
	void pawns() throws InvalidSquareException{
		for(int i = 0; i < 8; i++){
			Square w = new Square(i,2);
			Square b = new Square(i,7);
			Pawn white = new Pawn(w, Piece.white, this);
			Pawn black = new Pawn(b, Piece.black, this);
			System.out.println("place: " + black.toString());
			pieces.add(white);
			pieces.add(black);
			Move[] moves = black.getMoves();
			System.out.println("length: " + moves.length);
			for(Move m:moves){
				System.out.println("move: " + m.toString());
			}
		}
	}
	void knights() throws InvalidSquareException{
		for(int i = 1; i < 8; i += 5){
			Square w = new Square(i,1);
			Square b = new Square(i,8);
			Knight white = new Knight(w, Piece.white, this);
			Knight black = new Knight(b, Piece.black, this);
			System.out.println("place: " + black.toString());
			pieces.add(white);
			pieces.add(black);
			Move[] moves = black.getMoves();
			System.out.println("length: " + moves.length);
			for(Move m:moves){
				System.out.println("move: " + m.toString());
			}
		}
	}
	void rooks() throws InvalidSquareException{
		for(int i = 0; i < 8; i += 7){
			Square w = new Square(i,1);
			Square b = new Square(i,8);
			Rook white = new Rook(w, Piece.white, this);
			Rook black = new Rook(b, Piece.black, this);
			System.out.println("place: " + black.toString());
			pieces.add(white);
			pieces.add(black);
			Move[] moves = black.getMoves();
			System.out.println("length: " + moves.length);
			for(Move m:moves){
				System.out.println("move: " + m.toString());
			}
		}
	}
	void bishops() throws InvalidSquareException{
		for(int i = 2; i < 8; i+=3){
			Square w = new Square(i,1);
			Square b = new Square(i,8);
			Bishop white = new Bishop(w, Piece.white,this);
			Bishop black = new Bishop(b, Piece.black, this);
			System.out.println("place: " + black.toString());
			pieces.add(white);
			pieces.add(black);
			Move[] moves = black.getMoves();
			System.out.println("length: " + moves.length);
			for(Move m:moves){
				System.out.println("move: " + m.toString());
			}
		}
	}
	void queens() throws InvalidSquareException{
		Square w = new Square(3,1);
		Square b = new Square(3,8);
		Queen white = new Queen(w, Piece.white, this);
		Queen black = new Queen(b, Piece.black, this);
		pieces.add(white);
		pieces.add(black);
		Move[] moves = black.getMoves();
		System.out.println("length: " + moves.length);
		for(Move m:moves){
			System.out.println("move: " + m.toString());
		}
	}
	void kings() throws InvalidSquareException{
		Square w = new Square(4,1);
		Square b = new Square(4,8);
		King white = new King(w,Piece.white,this);
		King black = new King(b,Piece.black,this);
		pieces.add(white);
		pieces.add(black);
		Move[] moves = black.getMoves();
		System.out.println("length: " + moves.length);
		for(Move m:moves){
			System.out.println("move: " + m.toString());
		}
	}
	public void setup(){
		try{
			pawns();
			knights();
			rooks();
			bishops();
			queens();
			kings();
		}
		catch(InvalidSquareException e){
			System.out.println("Fatal Error!");
			System.exit(0);
		}
	}
}
