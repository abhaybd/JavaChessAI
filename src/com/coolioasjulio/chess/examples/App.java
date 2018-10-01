package com.coolioasjulio.chess.examples;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.coolioasjulio.chess.ChessGame;
import com.coolioasjulio.chess.Logger;
import com.coolioasjulio.chess.Piece;
import com.coolioasjulio.chess.Player;
import com.coolioasjulio.chess.players.HumanGUIPlayer;
import com.coolioasjulio.chess.players.MinimaxComputerPlayer;
import com.coolioasjulio.chess.players.PositionalComputerPlayer;

public class App {

    private static enum GameModeOptions {
        HumanVsHuman, HumanVsBotLvl1, HumanVsBotLvl2
    }

    public static void main(String[] args) {
        Logger.setGlobalLogger(new Logger(System.out));

        ChessGame game = new ChessGame(100);
        JFrame frame = new JFrame();
        frame.add(game);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        Player human = new HumanGUIPlayer(game);
        Player opponent = getOpponent(frame, game);

        int playerTeam = opponent instanceof HumanGUIPlayer ? Piece.WHITE : getTeamInput(frame);

        int winner;
        switch (playerTeam) {
            case Piece.BLACK:
                winner = game.playGame(opponent, human);
                break;

            case Piece.WHITE:
                winner = game.playGame(human, opponent);
                break;

            default:
                throw new IllegalArgumentException("Unrecognized team: " + playerTeam);
        }

        String message;
        switch (winner) {
            case Piece.BLACK:
                message = "Black team wins!";
                break;

            case Piece.WHITE:
                message = "White team wins!";
                break;

            default:
                message = "Stalemate! It's a tie!";
        }

        JOptionPane.showMessageDialog(frame, message);

        game.printMoves(System.out);
        frame.dispose();
    }

    private static Player getOpponent(JFrame frame, ChessGame game) {
        Object[] options = GameModeOptions.values();
        int choice = JOptionPane.showOptionDialog(frame, "What gamemode would you like to play?", "Game mode select",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        GameModeOptions gamemode = GameModeOptions.values()[choice];

        Player player;
        switch (gamemode) {
            case HumanVsHuman:
                player = new HumanGUIPlayer(game);
                break;

            case HumanVsBotLvl1:
                player = new PositionalComputerPlayer(game.getBoard());
                break;

            case HumanVsBotLvl2:
                player = new MinimaxComputerPlayer(game.getBoard());
                break;

            default:
                throw new IllegalStateException("Unrecognized player!");
        }

        return player;
    }

    private static int getTeamInput(JFrame frame) {
        Object[] options = new Object[] { "Black", "White" };
        int choice = JOptionPane.showOptionDialog(frame, "What team would you like to play as?", "Pick a team",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
        return 2 * choice - 1; // 1 -> 1, 0 -> -1
    }
}
