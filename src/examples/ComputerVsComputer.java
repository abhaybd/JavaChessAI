package examples;

import com.coolioasjulio.chess.players.MinimaxComputerPlayer;
import com.coolioasjulio.chess.players.Player;
import com.coolioasjulio.chess.players.PrunedMinimaxComputerPlayer;
import com.coolioasjulio.chess.ui.ChessGameUI;
import com.coolioasjulio.jchess.App;

import javax.swing.*;

public class ComputerVsComputer {
    public static void main(String[] args) {
        ChessGameUI game = new ChessGameUI(100, App.LIGHT, App.DARK);
        game.setShowPopUps(false);
        JFrame frame = new JFrame();
        frame.add(game.getPanel());
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        Player p1 = new PrunedMinimaxComputerPlayer(game.getBoard());
        Player p2 = new MinimaxComputerPlayer(game.getBoard());
        game.playGame(p1, p2);
    }
}
