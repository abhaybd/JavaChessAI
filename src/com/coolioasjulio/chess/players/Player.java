package com.coolioasjulio.chess.players;

import com.coolioasjulio.chess.Board;
import com.coolioasjulio.chess.Move;

public abstract class Player {

    protected Board board;
    protected int team;

    public Player(Board board) {
        this.board = board;
    }

    public void setTeam(int team) {
        this.team = team;
    }

    public int getTeam() {
        return team;
    }

    public abstract Move getMove();
}
