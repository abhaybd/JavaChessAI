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
	
	private MoveCandidate minimax(int depth, int team) {
		Move[] moves = board.getMoves(team);
		if(team == this.team) {
			MoveCandidate bestMove = null;
			for(Move m:moves) {
				List<Piece> before = board.saveState();
				board.doMove(m);
				if(!board.inCheck(team)) {
					double score;
					if(depth <= 1){
						score = board.getScore(team);
					} else {
						score = minimax(depth-1, -team).getScore();
					}
					if(depth > 1) {
						if(bestMove == null || score < bestMove.getScore()) {
							bestMove = new MoveCandidate(m,score);
						}
					} else {
						if(bestMove == null || score > bestMove.getScore()) {
							bestMove = new MoveCandidate(m,score);
						}
					}
				}
				board.restoreState(before);
				if(System.currentTimeMillis() > expiredTime){
					break;
				}
			}
			return bestMove;
		} else if(team == -this.team) {
			MoveCandidate bestMove = null;
			for(Move m:moves) {
				List<Piece> before = board.saveState();
				board.doMove(m);
				if(!board.inCheck(team)) {
					double score;
					if(depth <= 1){
						score = board.getScore(team);
					} else {
						score = minimax(depth-1, -team).getScore();
					}
					if(bestMove == null || score > bestMove.getScore()) {
						bestMove = new MoveCandidate(m,score);
					}			
					
				}
				board.restoreState(before);
				if(System.currentTimeMillis() > expiredTime) {
					break;
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
		for(int depth = 2; System.currentTimeMillis() <= expiredTime; depth++) {
			System.out.println("Searching with depth: " + depth);
			MoveCandidate mc = minimax(depth, team);
			if(bestMove == null || mc.getScore() < bestMove.getScore()) {
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
