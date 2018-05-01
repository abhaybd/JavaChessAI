package com.coolioasjulio.chess;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;

import javax.imageio.ImageIO;

public abstract class Piece {
	public static final double KING_VALUE = 200;
	public static final double QUEEN_VALUE = 9;
	public static final double ROOK_VALUE = 5;
	public static final double BISHOP_VALUE = 3.3;
	public static final double KNIGHT_VALUE = 3.2;
	public static final double PAWN_VALUE = 1;
	
	public static final int VANILLA_KING_VALUE = Integer.MAX_VALUE;
	public static final int VANILLA_QUEEN_VALUE = 9;
	public static final int VANILLA_ROOK_VALUE = 5;
	public static final int VANILLA_BISHOP_VALUE = 3;
	public static final int VANILLA_KNIGHT_VALUE = 3;
	public static final int VANILLA_PAWN_VALUE = 1;
	
	public static final int WHITE = 1;
	public static final int BLACK = -1;
	
	private static HashMap<String,Image> pieceImages = new HashMap<>();
	private static HashMap<String,double[][]> pieceSquareTables = new HashMap<>();	
	
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
	
	public static double getValue(Piece p) {
		String name = p.getName();
		
		double[][] table;
		if(pieceSquareTables.containsKey(name)) {
			table = pieceSquareTables.get(name);
		} else {
			table = loadPieceSquareTable(name + ".table");
			if(p.getTeam() == Piece.WHITE) {
				name = "w" + name;
			} else if(p.getTeam() == Piece.BLACK) {
				name = "b" + name;
				table = flipTable(table);
			}
			pieceSquareTables.put(name, table);
		}
		
		Square square = p.getSquare();
		return p.getRawValue() + table[square.getX()][square.getY()-1];
	}
	
	private static double[][] flipTable(double[][] table) {
		double[][] flipped = new double[table.length][table[0].length];
		for(int y = 0; y < table[0].length; y++){
			for(int x = 0; x < table.length; x++){
				flipped[x][y] = table[x][table[0].length-y-1];
			}
		}
		return flipped;
	}
	
	private static double[][] loadPieceSquareTable(String file) {
		try(BufferedReader in = new BufferedReader(new FileReader(file))){
			ArrayList<double[]> tableList = new ArrayList<>();
			
			String line;
			while((line = in.readLine()) != null){
				String[] numbers = line.replaceAll("\\s", "").split(",");
				double[] row = new double[numbers.length];
				for(int i = 0; i < row.length; i++){
					row[i] = Double.parseDouble(numbers[i])/100;
				}
				if(row.length != 8) throw new IllegalArgumentException("Invalid Piece-Square table file!");
				tableList.add(row);
			}
			if(tableList.size() != 8) throw new IllegalArgumentException("Invalid Piece-Square table file!");
			
			double[][] table = new double[tableList.get(0).length][tableList.size()];
			for(int y = 0; y < table.length; y++) {
				for(int x = 0; x < table[0].length; x++) {
					table[x][y] = tableList.get(y)[x];
				}
			}
			return table;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Image getImage(Piece p) throws IOException {
		String name = p.getName();
		String folder = "";
		if(p.getTeam() == Piece.WHITE) {
			name = "w"+ name;
			folder = "white/";
		} else {
			name = "b" + name;
			folder = "black/";
		}
		
		if(pieceImages.containsKey(name)) {
			return pieceImages.get(name);
		}
		
		Image img = ImageIO.read(new File(folder+name+".png")).getScaledInstance(100, 100, Image.SCALE_FAST);
		pieceImages.put(name, img);
		return img;
	}
	
	Square square;
	int team;
	Board board;
	
	public Piece(String square, int team, Board board) throws InvalidSquareException{
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
	
	public abstract Move[] getMoves();
	
	public abstract Piece copy();
	
	/**
	 * Mathematical value of the piece
	 * @return
	 */
	public abstract double getRawValue();
	
	/**
	 * Chess value of the piece
	 * @return
	 */
	public abstract double getVanillaValue();
	
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
	
	public boolean hasMove(Move move){
		Move[] moves = getMoves();
		for(Move m:moves){
			if(m.toString().equals(move.toString())) return true;
		}
		return false;
	}
	
	/**
	 * Mathematical value of the piece offset by positional value
	 * @return
	 */
	public double getValue() {
		return Piece.getValue(this);
	}
	
	public Image getImage() throws IOException {
		return Piece.getImage(this);
	}
	
	public String getType() {
		return Piece.getType(this);
	}
	
	public String getName() {
		String name = getType().toLowerCase();
		if(name.length() == 0) {
			name = "p";
		}
		return name;
	}
	
	public Board getBoard() {
		return board;
	}
	
	public int getTeam() {
		return team;
	}
	
	public Square getSquare() {
		return square;
	}
	
	public String toString() {
		return Piece.getType(this) + square.getSquare();
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Piece)) {
			return false;
		}
		Piece p = (Piece)o;
		return p.square.equals(this.square) &&
				p.getType().equals(this.getType()) &&
				p.team == this.team &&
				p.board == this.board;
	}
	
	@Override
	public int hashCode(){
		return Objects.hash(team,square,board);
	}
}
