import java.util.Collections;
import java.util.HashMap;

public class Opponent {
	private Board board;
	private int team;
	private int lastScore;
	/**
	 * 
	 * @param board the board object that this opponent is playing on
	 * @param team which team is the opponent on?
	 */
	public Opponent(Board board, int team){
		this.board = board;
		this.team = team;
	}
	
	private boolean safeMove(Move move) throws InvalidMoveException{
		Piece piece = move.getPiece();
		if(piece instanceof Pawn) return true;
		Piece captured = null;
		Square before = piece.getSquare();
		if(move.doesCapture()) {
			captured = board.checkSquare(move.getEnd());
			board.getPieces().remove(captured);
		}
		piece.move(move.getEnd());
		int attackers = 0;
		for(int i = 0; i < board.getPieces().size(); i++){
			Piece p = Collections.synchronizedList(board.getPieces()).get(i);
			if(p.getTeam() == piece.getTeam()) continue;
			Move[] moves = p.getMoves();
			for(Move m:moves){
				if(m.getEnd().equals(piece.getSquare())){
					attackers++;
				}
			}
		}
		piece.move(before);
		if(captured != null) board.getPieces().add(captured);
		
		board.getPieces().remove(piece);
		//Piece opp = piece;
		//opp.team *= -1;
		
		int defenders = 0;
		for(int i = 0; i < board.getPieces().size(); i++){
			Piece p = Collections.synchronizedList(board.getPieces()).get(i);
			if(p.getTeam() != piece.getTeam()) continue;
			Move[] moves = p.getMoves();
			for(Move m:moves){
				if(m.getEnd().equals(piece.getSquare())){
					defenders++;
				}
			}
		}
		//board.getPieces().remove(opp);
		board.getPieces().add(piece);
		System.out.println("defenders: " + defenders + " - Attackers: " + attackers);
		return defenders >= attackers;
	}
	
	public Move move() throws InvalidMoveException {
		HashMap<Integer,Move> moves = new HashMap<Integer,Move>();
		lastScore = board.getScore(team,0);
		for(int i = 0; i < board.getPieces().size(); i++){
			Piece p = Collections.synchronizedList(board.getPieces()).get(i);
			if(p.getTeam() != team) continue;
			Move[] possible = p.getMoves();
			Square before = p.getSquare();
			for(Move m:possible){
				int score = 0;
				if(!safeMove(m)) {
					score -= 10;
				}
				Piece captured = null;
				if(m.doesCapture()) {
					captured = board.checkSquare(m.getEnd());
					board.getPieces().remove(board.checkSquare(m.getEnd()));
				}
				p.move(m.getEnd());
				score += board.getScore(team,lastScore);
				if(board.checkMate(board.getKing(team*-1))) score += 99999;
				boolean check = board.inCheck(board.getKing(team));
				if(!check){
					moves.put(score,m);
				}
				p.move(before);
				if(m.doesCapture()) board.getPieces().add(captured);
				if(p instanceof Pawn && p.getSquare().getY() == ((p.getTeam() == Piece.white)?8:1)){
					Pawn pawn = (Pawn)p;
					pawn.promote("Queen");
				}
			}
			lastScore = board.getScore(team,lastScore);
		}
		return moves.get(max(moves.keySet().toArray(new Integer[0])));
	}
	public Integer max(Integer[] nums){
		Integer max = nums[0];
		for(Integer i:nums){
			if(i > max) max = i;
		}
		return max;
	}
	
	public Board getBoard(){
		return board;
	}
	
	public int getTeam(){
		return team;
	}
}
