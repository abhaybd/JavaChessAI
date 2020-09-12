package examples;

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

        PrunedMinimaxComputerPlayer p1 = new PrunedMinimaxComputerPlayer(game.getBoard());
        PrunedMinimaxComputerPlayer p2 = new PrunedMinimaxComputerPlayer(game.getBoard());
        p1.setSearchDepth(2);
        p2.setSearchDepth(2);
        game.playGame(p1, p2);
    }
}
