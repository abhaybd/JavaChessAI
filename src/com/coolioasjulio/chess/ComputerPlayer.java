package com.coolioasjulio.chess;

import java.util.HashMap;
import java.util.List;

public class ComputerPlayer implements Player {
	private Board board;
	private int team;
	private double lastMaterialScore, lastOppMaterialScore;
	
	/**
	 * 
	 * @param board the board object that this opponent is playing on
	 * @param team which team is the opponent on?
	 */
	public ComputerPlayer(Board board, int team){
		this.board = board;
		this.team = team;
		this.lastMaterialScore = 0;
		this.lastOppMaterialScore = 0;
	}
	
	private int numAttackers(Move move) {
		List<Piece> before = board.saveState();
		board.doMove(move);
		int attackers = 0;
		for(int i = 0; i < board.pieces.size(); i++) {
			Piece p = board.pieces.get(i);
			if(p.getTeam() == team) continue;
			Move[] moves = p.getMoves();
			for(Move m:moves) {
				if(m.getEnd().equals(p.getSquare())) {
					attackers++;
				}
			}
		}
		board.restoreState(before);
		return attackers;
	}
	
	private int numDefenders(Move move) {
		List<Piece> before = board.saveState();
		board.getPieces().remove(move.getPiece());
		int defenders = 0;
		for(int i = 0; i < board.pieces.size(); i++) {
			Piece p = board.pieces.get(i);
			if(p.getTeam() != team || p == move.getPiece()) continue;
			Move[] moves = p.getMoves();
			for(Move m:moves) {
				if(m.getEnd().equals(move.getEnd())) {
					defenders++;
				}
			}
		}
		board.restoreState(before);
		return defenders;
	}
	
	private boolean safeMove(Move move) throws InvalidMoveException{
		Piece piece = move.getPiece();
		if(piece instanceof Pawn) return true;
		if(move.doesCapture()) {
			if(board.checkSquare(move.getEnd()).getVanillaValue() >= move.getPiece().getVanillaValue()) {
				return true;
			}
		}
		int numDefenders = numDefenders(move);
		int numAttackers = numAttackers(move);
		return numDefenders >= numAttackers;
	}
	
	public Board getBoard(){
		return board;
	}
	
	public int getTeam(){
		return team;
	}

	@Override
	public Move getMove() {
		HashMap<Double,Move> moves = new HashMap<>();
		for(int i = 0; i < board.getPieces().size(); i++){
			Piece p = board.pieces.get(i);
			if(p.getTeam() != team) continue;
			Move[] possible = p.getMoves();
			for(Move m:possible){
				List<Piece> before = board.saveState();
				double score = 0;
				if(!safeMove(m)) {
					score -= 4*m.getPiece().getVanillaValue();
				}
				board.doMove(m);
				score += board.getScore(team, lastMaterialScore, lastOppMaterialScore);
				if(board.inCheckMate(board.getKing(-team))) score += 99999;
				boolean check = board.inCheck(board.getKing(team));
				if(!check){
					moves.put(score,m);
				}
				board.restoreState(before);
			}
			lastMaterialScore = board.getMaterialScore(team);
			lastOppMaterialScore = board.getMaterialScore(-team);
		}
		double bestScore = moves.keySet().stream().reduce(Math::max).get();
		Move bestMove = moves.get(bestScore);
		System.out.println(bestMove.toString() + " - Score: " + bestScore);
		return bestMove;
	}
}
