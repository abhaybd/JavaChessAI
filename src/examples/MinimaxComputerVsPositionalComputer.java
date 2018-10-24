package examples;

import javax.swing.JFrame;

import com.coolioasjulio.chess.Player;
import com.coolioasjulio.chess.players.MinimaxComputerPlayer;
import com.coolioasjulio.chess.players.PositionalComputerPlayer;
import com.coolioasjulio.chess.ui.ChessGameUI;

public class MinimaxComputerVsPositionalComputer {
    public static void main(String[] args) {
        ChessGameUI game = new ChessGameUI(100, App.LIGHT, App.DARK);
        JFrame frame = new JFrame();
        frame.add(game.getPanel());
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        Player betterComputer = new MinimaxComputerPlayer(game.getBoard());
        Player computer = new PositionalComputerPlayer(game.getBoard());
        game.playGame(betterComputer, computer);
    }
}
