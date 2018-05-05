package com.coolioasjulio.chess;

import java.util.List;

public class BetterComputerPlayer implements Player {
	private static final long TIMEOUT_MILLIS = 10000;
	
	private Board board;
	private int team;
	private long expiredTime;
	public BetterComputerPlayer(Board board, int team) {
		this.board = board;
		this.team = team;
	}
	
	private MoveCandidate minimax(int depth, int team, double alpha, double beta) {
		Move[] moves = board.getMoves(team);
		if(team == this.team) {
			MoveCandidate bestMove = null;
			for(Move m:moves) {
				List<Piece> before = board.saveState();
				try {
					board.doMove(m);
					if(!board.inCheck(team)) {
						double score;
						if(depth <= 1){
							score = board.getScore(team);
						} else {
							MoveCandidate mc = minimax(depth-1, -team, alpha, beta);
							if(mc == null) continue;
							score = mc.getScore();
						}
						if(depth > 1) {
							if(bestMove == null || score < bestMove.getScore()) {
								bestMove = new MoveCandidate(m,score);
								alpha = Math.min(alpha, score);
							}
						} else {
							if(bestMove == null || score > bestMove.getScore()) {
								bestMove = new MoveCandidate(m,score);
							}
						}
					}
				} finally {
					board.restoreState(before);
					if(bestMove != null && (System.currentTimeMillis() > expiredTime || beta >= alpha)){
						break;
					}
				}
			}
			return bestMove;
		} else if(team == -this.team) {
			MoveCandidate bestMove = null;
			for(Move m:moves) {
				List<Piece> before = board.saveState();
				try {
					board.doMove(m);
					if(!board.inCheck(team)) {
						double score;
						if(depth <= 1){
							score = board.getScore(team);
						} else {
							MoveCandidate mc = minimax(depth-1, -team, alpha, beta);
							if(mc == null) continue;
							score = mc.getScore();
						}
						if(bestMove == null || score > bestMove.getScore()) {
							bestMove = new MoveCandidate(m,score);
							beta = Math.max(beta, score);
						}
					}					
				} finally {
					board.restoreState(before);
					if(bestMove != null && (System.currentTimeMillis() > expiredTime || beta >= alpha)) {
						break;
					}					
				}
			}
			return bestMove;
		} else {
			throw new IllegalArgumentException("team can only be -1 (black) or 1 (white)!");
		}
	}

	@Override
	public Move getMove() {
		MoveCandidate bestMove = null;
		expiredTime = System.currentTimeMillis() + TIMEOUT_MILLIS;
		for(int depth = 2; System.currentTimeMillis() <= expiredTime; depth += 2) {
			System.out.println("Searching with depth: " + depth);
			MoveCandidate mc = minimax(depth, team, Double.MAX_VALUE, Double.MIN_VALUE);
			if((bestMove == null || mc.getScore() < bestMove.getScore()) && System.currentTimeMillis() <= expiredTime) {
				System.out.printf("Depth %d is better!\n", depth);
				bestMove = mc;
			}
		}
		System.out.printf("%s - Score: %.2f\n", bestMove.getMove().toString(), bestMove.getScore());
		return bestMove.getMove();
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
