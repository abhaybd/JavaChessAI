package com.coolioasjulio.chess.players;

import com.coolioasjulio.chess.*;
import com.coolioasjulio.chess.exceptions.InvalidMoveException;
import com.coolioasjulio.chess.pieces.Piece;

import javax.swing.JComponent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;

public class HumanGUIPlayer extends Player implements MouseListener {

    private final ChessGame chess;
    private final JComponent component;
    private Square fromSquare, toSquare;
    private final Object lock = new Object();
    private volatile boolean clicked = false;

    public HumanGUIPlayer(ChessGame chess, JComponent component) {
        super(chess.getBoard());
        this.chess = chess;
        this.component = component;
        component.addMouseListener(this);
    }

    private void waitForClick() throws InterruptedException {
        synchronized (lock) {
            while (!clicked) {
                lock.wait();
            }
            clicked = false;
        }
    }

    @Override
    public Move getMove() {
        synchronized (lock) {
            try {
                fromSquare = null;
                toSquare = null;
                // Wait for the first mouse click
                // fromSquare will be set in mouseClicked event
                waitForClick();
                chess.addHighlightedSquare(fromSquare);
                chess.draw();
                // Wait for the second mouse click
                // toSquare will be set in mouseClicked event
                waitForClick();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Piece piece = board.checkSquare(fromSquare);
        if (piece.getTeam() != team) {
            throw new InvalidMoveException("Choose your own piece!");
        }

        return Arrays.stream(board.getMoves(team))
                .filter(m ->
                        m.getType().equals(piece.getType()) &&
                                m.getStart().equals(fromSquare) &&
                                m.getEnd().equals(toSquare))
                .findFirst()
                .orElseThrow(() -> new InvalidMoveException("Invalid move!"));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int squareX = e.getX() / chess.getTileSize();
        int squareY = (component.getWidth() - e.getY()) / chess.getTileSize() + 1;
        Square square = new Square(squareX, squareY);
        synchronized (lock) {
            if (fromSquare == null) {
                Piece p = board.checkSquare(square);
                if (p != null && p.getTeam() == team) {
                    fromSquare = square;
                    clicked = true;
                    lock.notifyAll();
                }
            } else if (toSquare == null) {
                toSquare = square;
                lock.notifyAll();
                clicked = true;
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
