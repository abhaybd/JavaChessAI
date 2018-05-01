# JavaChessAI
A primitive AI which plays Chess.

This was designed modularly, so it should be fairly easy to customize. The Chess class is the main class which creates a Board and then plays the game. It also creates an Opponent object and pits the player against it.

The current implementation understands and enforces the rules of chess, with the exception on en passant. It is not touch screen, and chess moves must be entered as text through the console in long notation. (ex: Nb1-c3)

Some things are a bit wonky, however. Capture moves are not one method call. Instead, the program must remove the piece being captured, and then move the capturing piece. Also, pawn promotions are handled by passing a Scanner to the move function of the Pawn, and it will query the console for the promotion choice. This will be improved upon in the future.

The AI works by checking all possible moves and calculating a score based on the resulting positions. The score is based on the amount of space controlled by the AI, and the material (pieces) that it has alive. It calculates a score for each possible move, and then picks the move that yields the highest score. The result is that it can play pseudo-positionally, but cannot respond to threats. Because of it's inability to look ahead, it will frequently make stupid captures. However, the opponent has a safeMove() function which is being improved upon, which determines if they will lose the piece depending on their move. The safeMove() function currently is not implemented.

A rough outline of the program is as follows:

All pieces have their own class, all of which extend the class Piece.
All pieces have the getMoves() function which returns a Move array representing all possible moves given it's position on the board, and the pieces around it.
Pieces have a square property, which represents which square they are on.
Instantiating a Piece does not automatically place it onto the Board.
Pieces have a Board property which says which board they are on.
Pieces have a move() function which moves the Piece, but does NOT capture the piece, if the move is a capturing move.

The Move object has the starting Square, ending Square, the Piece which does the move, and a boolean denoting whether it captures or not.

The Square object represents a square on the chess board. Instead of a 2D array representing the board, the Board object simply has an ArrayList of Pieces, each of which have a Square property.

The Board holds all the pieces, and has a checkSquare() function, which checks the Square given to it as a parameter and returns the Piece which is on that square. If no piece occupies that square, it returns null.
The Board also has a checkScore(int team) function which checks the score of the team passed as a parameter. The team can be Board.BLACK(-1) or Board.WHITE(1).

The Opponent class is an AI to play against. The move() function is used to make the opponent move.