package com.coolioasjulio.chess;

import java.util.List;

public class BetterComputerPlayer implements Player {
	private static final int MINIMAX_DEPTH = 2; // Must be multiple of 2
	
	private Board board;
	private int team;
	public BetterComputerPlayer(Board board, int team) {
		this.board = board;
		this.team = team;
	}
	
	private MoveCandidate minimax(int depth, int team) {
		Move[] moves = board.getMoves(team);
		if(team == this.team) {
			MoveCandidate bestMove = null;
			for(Move m:moves) {
				List<Piece> before = board.saveState();
				board.doMove(m);
				if(!board.inCheck(team)) {
					double score = (depth <= 1)?board.getScore(team):minimax(depth-1, -team).getScore();
					if(bestMove == null || score < bestMove.getScore()) {
						bestMove = new MoveCandidate(m,score);
					}					
				}
				board.restoreState(before);
			}
			return bestMove;
		} else if(team == -this.team) {
			MoveCandidate bestMove = null;
			for(Move m:moves) {
				List<Piece> before = board.saveState();
				board.doMove(m);
				if(!board.inCheck(team)) {
					double score = depth <= 1?board.getScore(team):minimax(depth-1, -team).getScore();
					if(bestMove == null || score > bestMove.getScore()) {
						bestMove = new MoveCandidate(m,score);
					}					
				}
				board.restoreState(before);
			}
			return bestMove;
		} else {
			throw new IllegalArgumentException("team can only be -1 (black) or 1 (white)!");
		}
	}

	@Override
	public Move getMove() {
		MoveCandidate mc = minimax(MINIMAX_DEPTH, team);
		System.out.printf("%s - Score: %.2f\n", mc.getMove().toString(), mc.getScore());
		return mc.getMove();
	}

	@Override
	public int getTeam() {
		return team;
	}

	@Override
	public Board getBoard() {
		return board;
	}
}
