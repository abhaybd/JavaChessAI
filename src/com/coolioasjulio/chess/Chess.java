package com.coolioasjulio.chess;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Chess extends JPanel{
	private static final long serialVersionUID = 1L;
	private Board board;
	private ArrayList<Move> moves;
	public Chess() {
		board = new Board();
		board.setup();
		moves = new ArrayList<Move>();
		this.setPreferredSize(new Dimension(800,800));
		@SuppressWarnings("resource")
		Scanner input = new Scanner(System.in);
		System.out.println("What team do you want to be? w/b");
		char t = input.nextLine().toLowerCase().charAt(0);
		int playerTeam = (t=='b')?Piece.BLACK:Piece.WHITE;
		Player human = new HumanPlayer(board, playerTeam, System.in);
		Player opp = new BetterComputerPlayer(board, -playerTeam);
		runGame(human, opp);
	}

	public static void main(String[] args){
		Chess chess = new Chess();
		JFrame frame = new JFrame();
		frame.add(chess);
		frame.setResizable(false);
		frame.pack();
		frame.setVisible(true);
		
		new Thread(()->{
			while(!Thread.interrupted()) {
				SwingUtilities.updateComponentTreeUI(chess);
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	private boolean isEven(int i){
		return i % 2 == 0;
	}
	
	private void drawPieces(Graphics g) throws IOException {
		synchronized(board.finalPieces) {
			for(Piece p:board.finalPieces){
				Square square = p.getSquare();
				int x = square.getX()*100;
				int y = this.getHeight() - square.getY()*100;
				g.drawImage(Piece.getImage(p), x, y, null);
			}		
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
	
	private void runGame(final Player player1, final Player player2){
		Runnable run = new Runnable(){
			@Override
			public void run() {
				int team = 1;
				while(!Thread.interrupted()){
					board.updatePieces();
					List<Piece> beforeState = board.saveState();
					try{
						Player toMove = team==player1.getTeam()?player1:player2;
						Move m = toMove.getMove();
						if(m.isCastle()) {
							if(m.isKingSideCastle()) {
								King k = board.getKing(team);
								Piece p = board.checkSquare(new Square(7,k.getSquare().getY()));
								if(!(p instanceof Rook)) throw new InvalidMoveException();
								Rook r = (Rook)p;
								if(!board.freePath(k.getSquare(),r.getSquare(),k.getTeam()) || r.hasMoved()) throw new InvalidMoveException();
								k.move(new Square(6,k.getSquare().getY()));
								r.move(new Square(5,r.getSquare().getY()));
							} else if(m.isQueenSideCastle()) {
								King k = board.getKing(team);
								Piece p = board.checkSquare(new Square(0,k.getSquare().getY()));
								if(!(p instanceof Rook)) throw new InvalidMoveException();
								Rook r = (Rook)p;
								if(!board.freePath(k.getSquare(),r.getSquare(),k.getTeam()) || r.hasMoved()) throw new InvalidMoveException();
								k.move(new Square(2,k.getSquare().getY()));
								r.move(new Square(3,r.getSquare().getY()));
							}
						} else {
							board.doMove(m);
						}
						moves.add(m);
						team *= -1;
						if(board.inCheckMate(team)) {
							System.out.println("Checkmate!");
							System.out.println(team==Piece.WHITE?"0-1":"1-0");
							break;
						} else if(board.inStaleMate(team)) {
							System.out.println("Stalemate!");
							System.out.println("1/2-1/2");
							break;
						} else if(board.inCheck(team)){
							System.out.println("Check!");
						}
					} catch(InvalidMoveException e){
						System.err.println("Invalid! Try again!");
						board.restoreState(beforeState);
					}  catch(Exception e) {
						e.printStackTrace();
						board.restoreState(beforeState);
					}
				}
				board.updatePieces();
				printMoves();
			}
		};
		Thread t = new Thread(run);
		t.start();
	}
	
	private void printMoves() {
		System.out.println();
		for(int i = 0; i < moves.size(); i+=2) {
			int moveNum = (i/2)+1;
			String whiteMove = moves.get(i).toString();
			if(i == moves.size()-1) {
				System.out.println(String.format("%02d. %2$6s 1-0", moveNum, whiteMove));
			} else if(i == moves.size()-2) {
				System.out.println(String.format("%02d. %2$6s, %3$6s 0-1", moveNum, whiteMove, moves.get(i+1).toString()));
			} else {
				System.out.println(String.format("%02d. %2$6s, %3$6s", moveNum, whiteMove, moves.get(i+1).toString()));
			}
		}
	}
	
	@Override
	public void paintComponent(Graphics g)  {
		try{
			drawBoard(g);
			drawPieces(g);			
		}
		catch(Exception e){} // Fail silently
	}
}
