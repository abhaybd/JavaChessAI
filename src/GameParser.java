import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class GameParser {
	private File dir;
	private File[] files;
	private HashMap<Move,HashSet<Move>> moves;
	private Board board;
	/**
	 * 
	 * @param dir path of the folder which contains the pgn files
	 */
	public GameParser(String dir,Board board){
		this.dir = new File(dir);
		files = this.dir.listFiles();
		this.board = board;
	}

	public void load(){
		for(File f:files){
			if(!f.getName().endsWith(".pgn")) continue;
			try {
				parse(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void parse(File file) throws FileNotFoundException{
		Scanner in = new Scanner(file);
		in.useDelimiter(Pattern.compile("((1-0|0-1|1\\/2-1\\/2)(?![\"\\]]))"));
		String content = "";
		while(in.hasNext()){
			content = in.next();
			Pattern p = Pattern.compile("(([A-Z])?([a-z])?(x|-)?[a-z]\\d|O-O(-O)?");
			Matcher m = p.matcher(content);
			int team = 1;
			ArrayList<String> moves = new ArrayList<String>();
			while(m.find()){
				if(parseMove(m.group(1), team)) moves.add(m.group(1));
				team *= -1;
			}
		}
		in.close();
	}
	
	void println(Object o){System.out.println(o);}
	
	private boolean canCastle(String response, int team) throws InvalidMoveException, InvalidSquareException{
		response = response.toLowerCase();
		King k = board.getKing(team);
		if((!response.equals("o-o") && !response.equals("o-o-o")) || k.hasMoved()) return false;
		Rook r;
		if(response.equals("o-o")){
			println(new Square(7,k.getSquare().getY()).toString());
			Piece p = board.checkSquare(new Square(7,k.getSquare().getY()));
			if(!(p instanceof Rook)) return false;
			r = (Rook)p;
			if(!board.freePath(k.getSquare(),r.getSquare(),k.getTeam()) || r.hasMoved()) return false;
		}
		else if(response.equals("o-o-o")){
			println(new Square(7,k.getSquare().getY()).toString());
			Piece p = board.checkSquare(new Square(0,k.getSquare().getY()));
			if(!(p instanceof Rook)) return false;
			r = (Rook)p;
			if(!board.freePath(k.getSquare(),r.getSquare(),k.getTeam()) || r.hasMoved()) return false;
		}
		else return false;
		if(board.inCheck(k)){
			return false;
		}
		return true;
	}
	
	private boolean parseMove(String response, int team){
		try{
			if(Pattern.matches("^((o-)?o-o)$", response.toLowerCase())){
				return canCastle(response, team);
			}
			String type = String.valueOf(response.charAt(0));
			if(!type.toLowerCase().equals(type)) type = "";
			char action = (response.indexOf("x")>=0)?'x':'-';
			Square end = Square.parseString(response.substring(response.length()-2));
			for(Piece p:Collections.synchronizedList(board.getPieces())){
				if(!Piece.getType(p).equals(type)) continue;
				Move move = new Move(p, p.getSquare(), end, action=='x');
				if(p.hasMove(move)){
					return true;
				}
			}
			return false;
		}
		catch(Exception e){
			return false;
		}
		
		
	}
}
