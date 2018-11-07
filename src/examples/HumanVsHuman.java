package examples;

import javax.swing.JFrame;

import com.coolioasjulio.chess.players.HumanGUIPlayer;
import com.coolioasjulio.chess.players.Player;
import com.coolioasjulio.chess.ui.ChessGameUI;
import com.coolioasjulio.jchess.App;

public class HumanVsHuman {
    public static void main(String[] args) {
        ChessGameUI game = new ChessGameUI(100, App.LIGHT, App.DARK);
        JFrame frame = new JFrame();
        frame.add(game.getPanel());
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        Player human1 = new HumanGUIPlayer(game, game.getPanel());
        Player human2 = new HumanGUIPlayer(game, game.getPanel());
        game.playGameAsync(human1, human2);
    }
}
