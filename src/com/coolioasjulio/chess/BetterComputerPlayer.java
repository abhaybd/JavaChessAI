package com.coolioasjulio.chess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class BetterComputerPlayer implements Player {
	private static final long TIMEOUT_MILLIS = 2000;
	private static final int KEEP_MOVES = 2;
	
	private Board board;
	private int team;
	private long expiredTime;
	private Random random;
	public BetterComputerPlayer(Board board, int team) {
		this.board = board;
		this.team = team;
		random = new Random();
	}
	
	private MoveCandidate[] minimax(int depth, int team, double alpha, double beta) {
		Move[] moves = board.getMoves(team);
		if(team == this.team) {
			List<MoveCandidate> bestMoves = new ArrayList<MoveCandidate>();
			for(Move m:moves) {
				List<Piece> before = board.saveState();
				try {
					board.doMove(m);
					if(!board.inCheck(team)) {
						double score;
						if(depth <= 1){
							score = board.getScore(team);
						} else {
							MoveCandidate[] possibleMoves = minimax(depth-1, -team, alpha, beta);
							if(possibleMoves == null || possibleMoves.length == 0) continue;
							MoveCandidate mc = possibleMoves[0];
							score = mc.getScore();
						}
						alpha = Math.min(alpha, score);
						bestMoves.add(new MoveCandidate(m,score));
					}
				} finally {
					board.restoreState(before);
					if(bestMoves.size() > 0 && (System.currentTimeMillis() > expiredTime || beta >= alpha)){
						break;
					}
				}
			}
			int toKeep = Math.min(KEEP_MOVES, bestMoves.size());
			return bestMoves.stream()
					.sorted(Comparator.comparing(MoveCandidate::getScore))
					.collect(Collectors.toList())
					.subList(0, toKeep)
					.toArray(new MoveCandidate[0]);
		} else if(team == -this.team) {
			List<MoveCandidate> bestMoves = new ArrayList<MoveCandidate>();
			for(Move m:moves) {
				List<Piece> before = board.saveState();
				try {
					board.doMove(m);
					if(!board.inCheck(team)) {
						double score;
						if(depth <= 1){
							score = board.getScore(team);
						} else {
							MoveCandidate[] possibleMoves = minimax(depth-1, -team, alpha, beta);
							if(possibleMoves == null || possibleMoves.length == 0) continue;
							MoveCandidate mc = possibleMoves[0];
							score = mc.getScore();
						}
						beta = Math.max(beta, score);
						bestMoves.add(new MoveCandidate(m,score));
					}					
				} finally {
					board.restoreState(before);
					if(bestMoves.size() > 0 && (System.currentTimeMillis() > expiredTime || beta >= alpha)) {
						break;
					}					
				}
			}
			int toKeep = Math.min(KEEP_MOVES, bestMoves.size());
			return bestMoves.stream()
					.sorted(Comparator.comparing(MoveCandidate::getScore).reversed())
					.collect(Collectors.toList())
					.subList(0, toKeep)
					.toArray(new MoveCandidate[0]);
		} else {
			throw new IllegalArgumentException("team can only be -1 (black) or 1 (white)!");
		}
	}

	@Override
	public Move getMove() {
		List<MoveCandidate> bestMoves = new ArrayList<MoveCandidate>();
		expiredTime = System.currentTimeMillis() + TIMEOUT_MILLIS;
		for(int depth = 2; System.currentTimeMillis() <= expiredTime; depth += 2) {
			System.out.println("Searching with depth: " + depth);
			MoveCandidate[] moves = minimax(depth, team, Double.MAX_VALUE, Double.MIN_VALUE);
			if(System.currentTimeMillis() <= expiredTime) {
				bestMoves.addAll(Arrays.asList(moves));
			}
		}
		int toKeep = Math.min(KEEP_MOVES, bestMoves.size());
		System.out.println(bestMoves.toString());
		MoveCandidate bestMove = bestMoves.stream()
				.distinct()
				.sorted(Comparator.comparing(MoveCandidate::getScore))
				.collect(Collectors.toList()).get(random.nextInt(toKeep));
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
