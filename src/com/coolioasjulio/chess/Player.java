package com.coolioasjulio.chess;

public interface Player {
	public Move getMove();
	
	public int getTeam();
	
	public Board getBoard();
}
