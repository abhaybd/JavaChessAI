import java.util.ArrayList;

public class King extends Piece{
	private boolean moved = false;

	public King(Square square, int team, Board board) {
		super(square, team, board);
	}
	
	public boolean hasMoved(){
		return moved;
	}
	
	public int getValue(){ return 0; }
	
	public boolean inCheck(){
		for(Piece p:board.getPieces()){
			if(p.team != team){
				Move[] moves = p.getMoves();
				for(Move m:moves){
					if(m.getEnd().equals(square)){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	@Override
	public void move(Square move) throws InvalidMoveException{
		moved = true;
		super.move(move);
	}

	@Override
	public Move[] getMoves() {
		Square square = super.getSquare();
		int team = super.getTeam();
		ArrayList<Move> moves = new ArrayList<Move>();
		for(int x = -1; x <= 1; x++){
			for(int y = -1; y <= 1; y++){
				if(x == 0 && y == 0) continue;
				try{
					Square s = new Square(x+square.getX(),y+square.getY());
					Piece p = board.checkSquare(s);
					boolean capture = (p != null && p.team != team);
					if(p == null || capture){
						moves.add(new Move(this,square,s,capture));
					}
				}
				catch(InvalidSquareException e){
					continue;
				}
			}
		}
		return moves.toArray(new Move[0]);
	}

}
