package com.coolioasjulio.chess.examples;

import java.util.Scanner;

import javax.swing.JFrame;

import com.coolioasjulio.chess.ChessGame;
import com.coolioasjulio.chess.Piece;
import com.coolioasjulio.chess.Player;
import com.coolioasjulio.chess.players.HumanGUIPlayer;
import com.coolioasjulio.chess.players.PositionalComputerPlayer;

public class HumanVsPositionalComputer {
    public static void main(String[] args) {
        ChessGame game = new ChessGame(100);
        JFrame frame = new JFrame();
        frame.add(game);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        char t;
        try (Scanner input = new Scanner(System.in)) {
            System.out.println("What team do you want to be? w/b");
            t = input.nextLine().toLowerCase().charAt(0);
        }

        int playerTeam = (t == 'b') ? Piece.BLACK : Piece.WHITE;
        Player human = new HumanGUIPlayer(game);
        Player computer = new PositionalComputerPlayer(game.getBoard());

        switch (playerTeam) {
            case Piece.BLACK:
                game.runGame(computer, human);
                break;

            case Piece.WHITE:
                game.runGame(human, computer);
                break;
        }
    }
}
