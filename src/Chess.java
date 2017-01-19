import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Scanner;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;

public class Chess extends BasicGame {
	private Board board;
	private ArrayList<Move> moves;
	public Chess(String title) {
		super(title);
	}

	public static void main(String[] args){
		try
        {
            AppGameContainer app = new AppGameContainer(new Chess("Chess"));
            app.setDisplayMode(800, 800, false);
            app.setTargetFrameRate(5);							            
            app.start();
            
        }
        catch (SlickException e)
        {
            e.printStackTrace();
        }
	}
	
	private boolean isEven(int i){
		int half = i/2;
		return half*2==i;
	}
	
	private void drawPieces(GameContainer gc, Graphics g) throws SlickException, ConcurrentModificationException {
		final Iterator<Piece> iter = Collections.synchronizedList(board.getPieces()).iterator();
		while(iter.hasNext()){
			Piece p = iter.next();
			Square square = p.getSquare();
			int x = square.getX()*100;
			int y = gc.getHeight() - square.getY()*100;
			g.drawImage(Piece.getImage(p), x, y);
		}
	}
	
	private void drawBoard(Graphics g){
		for(int i = 0; i < 8; i++){
			for(int j = 0; j < 8; j++){
				int x = i*100;
				int y = j*100;
				g.setColor(Board.TAN);
				if(isEven(i+j)) g.setColor(Board.BROWN);
				g.fillRect(x, y, 100, 100);
			}
		}
	}
	
	void println(Object o){System.out.println(o);}
	
	private void getInput(){
		Runnable run = new Runnable(){
			@Override
			public void run() {
				Scanner input = new Scanner(System.in);
				println("What team do you want to be? w/b");
				char t = input.nextLine().toLowerCase().charAt(0);
				int playerTeam = (t=='b')?-1:1;
				Opponent opp = new Opponent(board, playerTeam*-1);
				int team = 1;
				while(true){
					try{
						if(team == opp.getTeam()){
							Move m = opp.move();
							if(m.doesCapture()) board.getPieces().remove(board.checkSquare(m.getEnd()));
							m.getPiece().move(m.getEnd());
							team *= -1;
							King k = board.getKing(team);
							if(board.checkMate(k)) {
								System.out.println("Checkmate!");
								break;
							}
							else if(board.inCheck(k)){
								System.out.println("Check!");
							}
							continue;
						}
						println("It is " + ((team==Piece.white)?"white":"black") + "'s turn! Input move in long notation. Ex: Nb1-c3");
						String response = input.nextLine();
						Square start = null;
						Square end = null;
						char action = '-';
						String type = "";
						if(response.toLowerCase().contains("o-o")){
							response = response.toLowerCase();
							King k = board.getKing(team);
							if((!response.equals("o-o") && !response.equals("o-o-o")) || k.hasMoved()) throw new InvalidMoveException();
							Square kBefore, rBefore;
							Rook r;
							if(response.equals("o-o")){
								println(new Square(7,k.getSquare().getY()).toString());
								Piece p = board.checkSquare(new Square(7,k.getSquare().getY()));
								if(!(p instanceof Rook)) throw new InvalidMoveException();
								r = (Rook)p;
								if(!board.freePath(k.getSquare(),r.getSquare(),k.getTeam()) || r.hasMoved()) throw new InvalidMoveException();
								kBefore = k.getSquare();
								rBefore = r.getSquare();
								k.move(new Square(6,k.getSquare().getY()));
								r.move(new Square(5,r.getSquare().getY()));
							}
							else if(response.equals("o-o-o")){
								println(new Square(7,k.getSquare().getY()).toString());
								Piece p = board.checkSquare(new Square(0,k.getSquare().getY()));
								if(!(p instanceof Rook)) throw new InvalidMoveException();
								r = (Rook)p;
								if(!board.freePath(k.getSquare(),r.getSquare(),k.getTeam()) || r.hasMoved()) throw new InvalidMoveException();
								kBefore = k.getSquare();
								rBefore = r.getSquare();
								k.move(new Square(2,k.getSquare().getY()));
								r.move(new Square(3,r.getSquare().getY()));
							}
							else throw new InvalidMoveException();
							if(board.inCheck(k)){
								k.move(kBefore);
								r.move(rBefore);
								throw new InvalidMoveException();
							}
							team *= -1;
							King toCheck = board.getKing(team);
							if(board.checkMate(toCheck)){
								println("Checkmate!");
								break;
							}
							else if(board.inCheck(toCheck)){
								println("Check!");
							}
							continue;
						}
						if(response.length() == 5 && !response.contains("o-o")){
							start = Square.parseString(response.substring(0,2));
							end = Square.parseString(response.substring(3));
							action = response.charAt(2);
							type = "";
						}
						else if(response.length() == 6 && !response.contains("o-o")){
							start = Square.parseString(response.substring(1,3));
							end = Square.parseString(response.substring(4));
							action = response.charAt(3);
							type = String.valueOf(response.charAt(0));
						}
						else throw new InvalidMoveException();
						if(action != 'x' && action != '-') throw new InvalidMoveException();
						Piece p = board.checkSquare(start);
						if(!Piece.getType(p).equals(type) || p.team != team){throw new InvalidMoveException();}
						Move[] possibleMoves = p.getMoves();
						Move move = new Move(p,start,end,action=='x');
						Square before = p.getSquare();
						Piece captured = null;
						if(hasMove(possibleMoves,move)){
							moves.add(move);
							if(action=='x'){
								captured = board.checkSquare(end);
								Collections.synchronizedList(board.getPieces()).remove(Collections.synchronizedList(board.getPieces()).indexOf(board.checkSquare(end)));
							}
							p.move(end,input);
						}
						else throw new InvalidMoveException();
						King k = board.getKing(team);
						if(board.inCheck(k)||board.checkMate(k)) {
							p.move(before, input);
							println("replace");
							if(captured != null){
								Collections.synchronizedList(board.getPieces()).add(captured);
							}
							throw new InvalidMoveException();
						}
						team *= -1;
						k = board.getKing(team);
						if(board.checkMate(k)) {
							System.out.println("Checkmate!");
							break;
						}
						else if(board.inCheck(k)){
							System.out.println("Check!");
						}
					}
					catch(Exception e){
						println("Invalid! Try again!");
						e.printStackTrace();
						continue;
					}
				}
			}
		};
		Thread t = new Thread(run);
		t.start();
	}
	
	@Override
	public void render(GameContainer gc, Graphics g) throws SlickException {
		drawBoard(g);
		try{
			drawPieces(gc,g);			
		}
		catch(Exception e){
			
		}
	}

	@Override
	public void init(GameContainer gc) throws SlickException {
		board = new Board();
		board.setup();
		gc.setAlwaysRender(true);
		moves = new ArrayList<Move>();
		getInput();
	}
	
	private boolean hasMove(Move[] moves, Move move){
		for(Move m:moves){
			if(m.toString().equals(move.toString())){
				return true;
			}
		}
		return false;
	}

	@Override
	public void update(GameContainer gc, int frame) throws SlickException {
		
	}
}
