package com.coolioasjulio.chess.heuristics;

import com.coolioasjulio.chess.Board;

public interface Heuristic {
    double getScore(Board board, int team);
}
