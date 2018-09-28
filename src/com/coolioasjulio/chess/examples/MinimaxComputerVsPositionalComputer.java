package com.coolioasjulio.chess.examples;

import javax.swing.JFrame;

import com.coolioasjulio.chess.ChessGame;
import com.coolioasjulio.chess.Piece;
import com.coolioasjulio.chess.Player;
import com.coolioasjulio.chess.players.MinimaxComputerPlayer;
import com.coolioasjulio.chess.players.PositionalComputerPlayer;

public class MinimaxComputerVsPositionalComputer {
    public static void main(String[] args) {
        ChessGame game = new ChessGame(100);
        JFrame frame = new JFrame();
        frame.add(game);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        Player betterComputer = new MinimaxComputerPlayer(game.getBoard(), Piece.WHITE);
        Player computer = new PositionalComputerPlayer(game.getBoard(), Piece.BLACK);
        game.runGame(betterComputer, computer);
    }
}
