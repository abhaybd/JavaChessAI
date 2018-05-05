package com.coolioasjulio.chess;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class Board{
	public static final Color BROWN = new Color(107,54,54);
	public static final Color TAN = new Color(203, 177, 154);
	public static final double spaceScore = 0.02;
	public ArrayList<Piece> pieces;
	public ArrayList<Piece> finalPieces;
	public Board(){
		pieces = new ArrayList<Piece>();
		finalPieces = new ArrayList<Piece>();
	}
	
	public List<Piece> getPieces(){
		return pieces;
	}
	
	public List<Piece> getPieces(int team) {
		return getPieces().stream()
				.filter(p -> p.team == team)
				.collect(Collectors.toList());
	}
	
	public Move[] getMoves(int team) {
		return getPieces(team).stream()
				.flatMap(p -> Arrays.asList(p.getMoves()).stream())
				.collect(Collectors.toList())
				.toArray(new Move[0]);
	}
	
	public boolean removePiece(int i) {
		synchronized(pieces) {
			return pieces.remove(i) != null;
		}
	}
	
	public boolean removePiece(Piece p) {
		synchronized(pieces) {
			for(Iterator<Piece> iter = pieces.iterator(); iter.hasNext();) {
				if(iter.next().equals(p)) {
					iter.remove();
					return true;
				}
			}
			return false;
		}
	}
	
	public Piece checkSquare(Square square){
		try{
			for(Piece p:pieces){
				if(p.getSquare().equals(square)){
					return p;
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean inStaleMate(int team) {
		if(inCheck(team)) return false;
		Move[] moves = getMoves(team);
		boolean stalemate = true;
		for(Move move:moves) {
			List<Piece> before = saveState();
			doMove(move);
			if(!inCheck(team)) stalemate = false;
			restoreState(before);
		}
		return stalemate;
	}
	
	public boolean inCheckMate(int team) throws InvalidMoveException {
		if(!inCheck(team)) return false;
		Move[] moves = getMoves(team);
		boolean checkMate = true;
		for(Move move:moves) {
			List<Piece> before = saveState();
			doMove(move);
			if(!inCheck(team)) checkMate = false;
			restoreState(before);
		}
		return checkMate;
	}
	
	public King getKing(int team) throws InvalidMoveException{
		for(Piece p:Collections.synchronizedList(getPieces())){
			if(p instanceof King && p.team == team){
				return (King)p;
			}
		}
		System.err.println(getPieces().stream().map(Piece::toString).collect(Collectors.toList()).toString());
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
	
	public boolean inCheck(int team) {
		return inCheck(getKing(team));
	}
	public boolean inCheck(King k) throws InvalidMoveException{
		if(k == null) throw new InvalidMoveException();
		Move[] moves = getMoves(-k.getTeam());
		for(Move m:moves){
			if(m.getEnd().equals(k.getSquare())){
				return true;
			}
		}
		return false;
	}
	
	public double getMaterialScore(int team){
		double material = 0;
		for(Piece p:pieces){
			if(p.getTeam() != team) continue;
			material += p.getValue();
		}
		return material;
	}
	
	public double getScore(int team){
		double space = (double)getMoves(team).length * spaceScore;
		double material = getMaterialScore(team);
		double oppMaterial = getMaterialScore(-team);
		return space + material - oppMaterial + (inCheckMate(-team)?1000:0);
	}
	
	public void doMove(Move m) {
		if(m.isCastle()) throw new IllegalArgumentException("Must castle manually!");
		
		Piece p = this.checkSquare(m.getStart());
		List<Square> endSquares = Arrays.asList(p.getMoves()).stream().map(Move::getEnd).collect(Collectors.toList());
		if(m.getPiece().getBoard() != this || !endSquares.contains(m.getEnd())) {
			throw new IllegalArgumentException("Invalid move!");
		}
		if(m.doesCapture()) {
			removePiece(checkSquare(m.getEnd()));
		}
		p.move(m.getEnd());
	}
	
	public void updatePieces() {
		synchronized (finalPieces) {
			synchronized(pieces) {
				finalPieces.clear();
				for(Piece p:pieces) {
					finalPieces.add(p.copy());
				}
			}
		}
	}
	
	public List<Piece> saveState() {
		ArrayList<Piece> copy = new ArrayList<>();
		synchronized(pieces) {
			for(Piece p:pieces) {
				copy.add(p.copy());
			}
		}
		return copy;
	}
	
	public void restoreState(List<Piece> state) {
		synchronized(pieces) {
			pieces.clear();
			for(Piece p:state) {
				pieces.add(p.copy());
			}
		}
	}
	
	void pawns() throws InvalidSquareException{
		for(int i = 0; i < 8; i++){
			Square w = new Square(i,2);
			Square b = new Square(i,7);
			Pawn white = new Pawn(w, Piece.WHITE, this);
			Pawn black = new Pawn(b, Piece.BLACK, this);
			pieces.add(white);
			pieces.add(black);
		}
	}
	void knights() throws InvalidSquareException{
		for(int i = 1; i < 8; i += 5){
			Square w = new Square(i,1);
			Square b = new Square(i,8);
			Knight white = new Knight(w, Piece.WHITE, this);
			Knight black = new Knight(b, Piece.BLACK, this);
			pieces.add(white);
			pieces.add(black);
		}
	}
	void rooks() throws InvalidSquareException{
		for(int i = 0; i < 8; i += 7){
			Square w = new Square(i,1);
			Square b = new Square(i,8);
			Rook white = new Rook(w, Piece.WHITE, this);
			Rook black = new Rook(b, Piece.BLACK, this);
			pieces.add(white);
			pieces.add(black);
		}
	}
	void bishops() throws InvalidSquareException{
		for(int i = 2; i < 8; i+=3){
			Square w = new Square(i,1);
			Square b = new Square(i,8);
			Bishop white = new Bishop(w, Piece.WHITE,this);
			Bishop black = new Bishop(b, Piece.BLACK, this);
			pieces.add(white);
			pieces.add(black);
		}
	}
	void queens() throws InvalidSquareException{
		Square w = new Square(3,1);
		Square b = new Square(3,8);
		Queen white = new Queen(w, Piece.WHITE, this);
		Queen black = new Queen(b, Piece.BLACK, this);
		pieces.add(white);
		pieces.add(black);
	}
	void kings() throws InvalidSquareException{
		Square w = new Square(4,1);
		Square b = new Square(4,8);
		King white = new King(w,Piece.WHITE,this);
		King black = new King(b,Piece.BLACK,this);
		pieces.add(white);
		pieces.add(black);
	}
	public void setup(){
		try{
			pawns();
			knights();
			rooks();
			bishops();
			queens();
			kings();
			updatePieces();
		}
		catch(InvalidSquareException e){
			System.out.println("Fatal Error!");
			System.exit(0);
		}
	}
}
