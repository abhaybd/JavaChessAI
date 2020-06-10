package com.coolioasjulio.chess;

import java.util.Objects;

import com.coolioasjulio.chess.exceptions.InvalidSquareException;

public class Square {
    /**
     * Checks if the supplied coordinates point to a valid square on a chessboard.
     *
     * @param x The x coordinate in range [0,7]
     * @param y The y coordinate in range [1,8]
     * @return True if the square is valid, false otherwise.
     */
    public static boolean validSquare(int x, int y) {
        return inRange(x, 0, 7) && inRange(y, 1, 8);
    }

    /**
     * Return a Square object representing the given square on a chessboard.
     *
     * @param square The string representation of the square.
     * @return A Square object representing that square.
     * @throws InvalidSquareException If the notation for the square is incorrect.
     */
    public static Square parseString(String square) throws InvalidSquareException {
        if (square.length() != 2 || !Character.isLowerCase(square.charAt(0)) || !Character.isDigit(square.charAt(1))) {
            throw new InvalidSquareException("Invalid notation for square: " + square);
        }
        int x = square.charAt(0) - 'a';
        int y = square.charAt(1) - '0';
        return new Square(x, y);
    }

    private static boolean inRange(int num, int low, int high) {
        return low <= num && num <= high;
    }

    private final int x, y;

    /**
     * Create a square object for the given coordinates
     *
     * @param x In range [0,7]
     * @param y In range [1,8]. Yeah, ik I hate myself.
     * @throws InvalidSquareException If the coordinates are invalid.
     */
    public Square(int x, int y) throws InvalidSquareException {
        if (!validSquare(x, y)) {
            throw new InvalidSquareException();
        }
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
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
        return String.valueOf((char)(x + 'a')) + y;
    }
}
