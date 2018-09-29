package com.coolioasjulio.chess.players;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import com.coolioasjulio.chess.ChessGame;
import com.coolioasjulio.chess.InvalidMoveException;
import com.coolioasjulio.chess.King;
import com.coolioasjulio.chess.Move;
import com.coolioasjulio.chess.Piece;
import com.coolioasjulio.chess.Player;
import com.coolioasjulio.chess.Square;

public class HumanGUIPlayer extends Player implements MouseListener {

    private ChessGame chess;
    private Square fromSquare, toSquare;
    private final Object lock = new Object();

    public HumanGUIPlayer(ChessGame chess) {
        super(chess.getBoard());
        this.board = chess.getBoard();
        this.chess = chess;
        chess.addMouseListener(this);
    }

    @Override
    public Move getMove() {
        // Wait for the mouse click
        synchronized (lock) {
            try {
                fromSquare = null;
                toSquare = null;
                System.out.println("It is " + ((team == Piece.WHITE) ? "white" : "black") + "'s turn!");
                System.out.println("Click the piece you want to move!");
                lock.wait();
                chess.addHighlightedSquare(fromSquare);
                chess.repaint();
                System.out.println("Click the square you want to move to!");
                lock.wait();
                System.out.println();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Piece piece = board.checkSquare(fromSquare);
        if (piece.getTeam() != team) {
            throw new InvalidMoveException("Choose your own piece!");
        }

        Move move;

        if (piece instanceof King) {
            if (toSquare.getX() == 2 && board.canQueenSideCastle((King) piece)) {
                move = new Move(team, false);
            } else if (toSquare.getX() == 6 && board.canKingSideCastle((King) piece)) {
                move = new Move(team, true);
            } else {
                move = new Move(piece, fromSquare, toSquare, board.checkSquare(toSquare) != null);
            }
        } else {
            move = new Move(piece, fromSquare, toSquare, board.checkSquare(toSquare) != null);
        }

        return move;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int squareX = e.getX() / chess.getTileSize();
        int squareY = (chess.getWidth() - e.getY()) / chess.getTileSize() + 1;
        Square square = new Square(squareX, squareY);
        synchronized (lock) {
            if (fromSquare == null) {
                Piece p = board.checkSquare(square);
                if (p != null && p.getTeam() == team) {
                    fromSquare = square;
                    lock.notifyAll();
                }
            } else if (toSquare == null) {
                toSquare = square;
                lock.notifyAll();
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

}
