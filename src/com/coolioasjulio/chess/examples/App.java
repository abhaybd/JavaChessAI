package com.coolioasjulio.chess.examples;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.coolioasjulio.chess.Logger;
import com.coolioasjulio.chess.Piece;
import com.coolioasjulio.chess.Player;
import com.coolioasjulio.chess.players.HumanGUIPlayer;
import com.coolioasjulio.chess.players.MinimaxComputerPlayer;
import com.coolioasjulio.chess.players.PositionalComputerPlayer;
import com.coolioasjulio.chess.ui.ChessGameUI;
import com.coolioasjulio.chess.ui.ChessAxisLabel;
import com.coolioasjulio.chess.ui.ChessAxisLabel.Axis;

public class App {

    private static enum GameModeOptions {
        HumanVsHuman, HumanVsBotLvl1, HumanVsBotLvl2
    }

    private static final int TILE_SIZE = 100;
    public static final Color BROWN = new Color(107, 54, 54);
    public static final Color TAN = new Color(203, 177, 154);
    public static final Color BG_COLOR = new Color(67, 34, 34);

    private static void configConstraints(GridBagConstraints c, int x, int y, int width, int height) {
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = width;
        c.gridheight = height;
    }

    public static void main(String[] args) {
        Logger.setGlobalLogger(new Logger(System.out));
        Logger.getGlobalLogger().setLoggingEnabled(true);

        ChessGameUI game = new ChessGameUI(TILE_SIZE, TAN, BROWN);
        JFrame frame = new JFrame();
        frame.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        frame.getContentPane().setBackground(BG_COLOR);

        configConstraints(c, 1, 0, 8, 1);
        frame.add(new ChessAxisLabel(Axis.Horizontal, game.getTileSize(), BG_COLOR, TAN), c);

        configConstraints(c, 0, 1, 1, 8);
        frame.add(new ChessAxisLabel(Axis.Vertical, game.getTileSize(), BG_COLOR, TAN), c);

        configConstraints(c, 1, 1, 8, 8);
        frame.add(game.getPanel(), c);

        configConstraints(c, 1, 9, 8, 1);
        frame.add(new ChessAxisLabel(Axis.Horizontal, game.getTileSize(), BG_COLOR, TAN), c);

        configConstraints(c, 9, 1, 1, 8);
        frame.add(new ChessAxisLabel(Axis.Vertical, game.getTileSize(), BG_COLOR, TAN), c);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);

        Player human = new HumanGUIPlayer(game, game.getPanel());
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

    private static Player getOpponent(JFrame frame, ChessGameUI game) {
        Object[] options = GameModeOptions.values();
        int choice = JOptionPane.showOptionDialog(frame, "What gamemode would you like to play?", "Game mode select",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        GameModeOptions gamemode = GameModeOptions.values()[choice];

        Player player;
        switch (gamemode) {
            case HumanVsHuman:
                player = new HumanGUIPlayer(game, game.getPanel());
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
