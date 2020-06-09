package com.coolioasjulio.chess;

import java.util.Objects;

import com.coolioasjulio.chess.exceptions.InvalidSquareException;

public class Square {
    private String square;
    private int x, y;
    private int[] coords;

    public static boolean validSquare(int x, int y) {
        return between(x, 0, 7) && between(y, 1, 8);
    }

    private static boolean between(int toCheck, int bottom, int upper) {
        return bottom <= toCheck && toCheck <= upper;
    }

    /**
     * Create a square object for the given coordinates
     *
     * @param x In range [0,7]
     * @param y In range [1,8]. Yeah, ik I hate myself.
     * @throws InvalidSquareException
     */
    public Square(int x, int y) throws InvalidSquareException {
        if (!validSquare(x, y)) {
            throw new InvalidSquareException();
        }
        char alph = (char) (x + 'a');
        this.square = String.valueOf(alph) + (y);
        this.coords = new int[] { x, y };
        this.x = x;
        this.y = y;
    }

    public static Square parseString(String square) throws InvalidSquareException {
        char alph = square.charAt(0);
        int y = Integer.parseInt(String.valueOf(square.charAt(1)));
        int x = alph - 'a';
        return new Square(x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Square)) {
            return false;
        }
        Square s = (Square) o;
        return s.getX() == x && s.getY() == y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return square;
    }

    public int[] getCoords() {
        return coords;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getSquare() {
        return square;
    }
}
