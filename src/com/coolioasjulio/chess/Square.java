package com.coolioasjulio.chess;
import java.util.Objects;

public class Square {
	private String square;
	private int x, y;
	private int[] coords;
	boolean between(int toCheck, int bottom, int upper){
		return bottom <= toCheck && toCheck <= upper;
	}
	public Square(int x, int  y) throws InvalidSquareException{
		if(!between(x,0,7) || !between(y,1,8)){
			throw new InvalidSquareException();
		}
		char alph = (char) (x + 97);
		this.square = String.valueOf(alph) + (y);
		this.coords = new int[]{x, y};
		this.x = x;
		this.y = y;
	}
	public static Square parseString(String square) throws InvalidSquareException{
		char alph = square.charAt(0);
		int y = Integer.parseInt(String.valueOf(square.charAt(1)));
		int x = alph - 97;
		Square s = new Square(x,y);
		return s;
	}
	public boolean equals(Object o){
		if(!(o instanceof Square)) {
			return false;
		}
		Square s = (Square)o;
		return s.getX() == x && s.getY() == y;
	}
	public int hashCode(){
		return Objects.hash(x,y,square,coords);
	}
	@Override
	public String toString(){
		return square;
	}
	public int[] getCoords(){
		return coords;
	}
	public int getX(){
		return x;
	}
	public int getY(){
		return y;
	}
	public String getSquare(){
		return square;
	}
}
