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
		runGame();
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
	
	private void runGame(){
		Runnable run = new Runnable(){
			@Override
			public void run() {
				Scanner input = new Scanner(System.in);
				System.out.println("What team do you want to be? w/b");
				char t = input.nextLine().toLowerCase().charAt(0);
				int playerTeam = (t=='b')?-1:1;
				HumanPlayer human = new HumanPlayer(board, playerTeam, System.in);
				ComputerPlayer opp = new ComputerPlayer(board, -playerTeam);
				int team = 1;
				while(true){
					board.updatePieces();
					List<Piece> beforeState = board.saveState();
					try{
						try {
							if(team == opp.getTeam()){
								Move m = opp.getMove();
								board.doMove(m);
								Piece p = m.getPiece();
								if(p instanceof Pawn && p.getSquare().getY() == ((p.getTeam() == Piece.white)?8:1)){
									((Pawn)p).promote("Queen");
								}
								continue;
							}
							
							if(team == playerTeam) {
								Move m = human.getMove();
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
										King k =board.getKing(team);
										Piece p = board.checkSquare(new Square(0,k.getSquare().getY()));
										if(!(p instanceof Rook)) throw new InvalidMoveException();
										Rook r = (Rook)p;
										if(!board.freePath(k.getSquare(),r.getSquare(),k.getTeam()) || r.hasMoved()) throw new InvalidMoveException();
										k.move(new Square(2,k.getSquare().getY()));
										r.move(new Square(3,r.getSquare().getY()));
									}
								} else {
									board.doMove(m);
									moves.add(m);
								}
							}
						} finally {
							team *= -1;
							King k = board.getKing(team);
							if(board.checkMate(k)) {
								System.out.println("Checkmate!");
								break;
							}
							else if(board.inCheck(k)){
								System.out.println("Check!");
							}
						}
					}
					catch(Exception e){
						if(e instanceof InvalidMoveException) {
							System.err.println("Invalid! Try again!");
							board.restoreState(beforeState);
						} else {
							e.printStackTrace();
						}
						continue;
					}
				}
				input.close();
			}
		};
		Thread t = new Thread(run);
		t.start();
	}
	
	@Override
	public void paintComponent(Graphics g)  {
		try{
			drawBoard(g);
			drawPieces(g);			
		}
		catch(Exception e){
			
		}
	}
}
