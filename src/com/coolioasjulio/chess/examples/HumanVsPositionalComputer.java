package com.coolioasjulio.chess.examples;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

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

        int playerTeam = getTeamInput(frame);
        Player human = new HumanGUIPlayer(game);
        Player computer = new PositionalComputerPlayer(game.getBoard());

        switch (playerTeam) {
            case Piece.BLACK:
                game.playGameAsync(computer, human);
                break;

            case Piece.WHITE:
                game.playGameAsync(human, computer);
                break;
        }
    }

    private static int getTeamInput(JFrame frame) {
        Object[] options = new Object[] { "Black", "White" };
        int choice = JOptionPane.showOptionDialog(frame, "What team would you like to play as?", "Pick a team",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
        return 2 * choice - 1; // 1 -> 1, 0 -> -1
    }
}
