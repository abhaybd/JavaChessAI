package examples;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import com.coolioasjulio.chess.Logger;
import com.coolioasjulio.chess.Piece;
import com.coolioasjulio.chess.Player;
import com.coolioasjulio.chess.players.HumanGUIPlayer;
import com.coolioasjulio.chess.players.MinimaxComputerPlayer;
import com.coolioasjulio.chess.players.PositionalComputerPlayer;
import com.coolioasjulio.chess.ui.ChessGameUI;
import com.coolioasjulio.chess.ui.ChessAxisLabel;
import com.coolioasjulio.chess.ui.ChessAxisLabel.Axis;

public class App extends JFrame {
    private static final long serialVersionUID = 1L;

    private static enum GameModeOptions {
        HumanVsHuman, HumanVsBotLvl1, HumanVsBotLvl2
    }

    private static final int TILE_SIZE = 100;
    public static final Color DARK = new Color(83, 124, 73);
    public static final Color LIGHT = new Color(255, 233, 175);
    public static final Color BG_COLOR = new Color(20, 40, 20);

    public static void main(String[] args) {
        Logger.setGlobalLogger(new Logger(System.out));
        Logger.getGlobalLogger().setLoggingEnabled(true);

        App app = new App(BG_COLOR, LIGHT, DARK, TILE_SIZE);
        app.playGame();
        app.dispose();
    }

    private ChessGameUI game;

    public App(Color bgColor, Color lightTile, Color darkTile, int tileSize) {
        game = new ChessGameUI(tileSize, LIGHT, DARK);

        try {
            this.setIconImage(ImageIO.read(App.class.getClassLoader().getResourceAsStream("icon.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        this.getContentPane().setBackground(bgColor);

        configConstraints(c, 0, 0, 10, 1);
        JLabel turnIndicator = new JLabel();
        game.setTurnIndicator(turnIndicator);
        turnIndicator.setBackground(bgColor);
        turnIndicator.setFont(new Font("Segoe Print", Font.PLAIN, tileSize / 2));
        turnIndicator.setForeground(lightTile);
        turnIndicator.setPreferredSize(new Dimension(tileSize * 8, tileSize));
        turnIndicator.setHorizontalAlignment(SwingConstants.CENTER);
        c.anchor = GridBagConstraints.CENTER;
        this.add(turnIndicator, c);

        configConstraints(c, 1, 1, 8, 1);
        this.add(new ChessAxisLabel(Axis.Horizontal, game.getTileSize(), bgColor, lightTile), c);

        configConstraints(c, 0, 2, 1, 8);
        this.add(new ChessAxisLabel(Axis.Vertical, game.getTileSize(), bgColor, lightTile), c);

        configConstraints(c, 1, 2, 8, 8);
        this.add(game.getPanel(), c);

        configConstraints(c, 1, 10, 8, 1);
        this.add(new ChessAxisLabel(Axis.Horizontal, game.getTileSize(), bgColor, lightTile), c);

        configConstraints(c, 9, 2, 1, 8);
        this.add(new ChessAxisLabel(Axis.Vertical, game.getTileSize(), bgColor, lightTile), c);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.pack();
        this.setVisible(true);
    }

    public void playGame() {
        Player human = new HumanGUIPlayer(game, game.getPanel());
        Player opponent = getOpponent();

        int playerTeam = opponent instanceof HumanGUIPlayer ? Piece.WHITE : getTeamInput();

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

        JOptionPane.showMessageDialog(this, message);
    }

    private void configConstraints(GridBagConstraints c, int x, int y, int width, int height) {
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = width;
        c.gridheight = height;
    }

    private Player getOpponent() {
        Object[] options = GameModeOptions.values();
        int choice;
        do {
            choice = JOptionPane.showOptionDialog(this, "What gamemode would you like to play?", "Game mode select",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        } while (choice == -1);

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

    private int getTeamInput() {
        Object[] options = new Object[] { "Black", "White" };
        int choice;
        do {
            choice = JOptionPane.showOptionDialog(this, "What team would you like to play as?", "Pick a team",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
        } while (choice == -1);

        return 2 * choice - 1; // 1 -> 1, 0 -> -1
    }
}
