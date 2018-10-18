# JavaChessAI
A primitive AI which plays Chess.

This was designed modularly, so it should be fairly easy to customize. The Chess class is the main class which creates a Board and then plays the game between the two `Player`s.

The current implementation understands and enforces the rules of chess, with the exception on en passant. There is both a `HumanConsolePlayer` and `HumanGUIPlayer`. The `HumanConsolePlayer` allows the player to type in their move using long notation. `HumanGUIPlayer` lets the player use their mouse to move around the pieces. Click on the piece to move, and click on the square you want to move the piece to.

Moves are done with `board.doMove()` Also, pawn promotions are handled by using the `pawn.move(Move m, String promotion)` overload. The promotion parameter should represent the symbol of the piece to promote to. (e.g. "Q", "B", "R", "N")

There are two implementations of a computer AI.

- `ComputerPlayer` class. The AI works by checking all possible moves and calculating a score based on the resulting positions. The score is based on the amount of space controlled by the AI, and the material (pieces) that it has alive. It offsets piece value by individual positional value to encourage better positional play by the AI. It calculates a score for each possible move, and then picks the move that yields the highest score. The result is that it can play pseudo-positionally, but cannot respond to threats.

- `BetterComputerPlayer` class. This uses a minimax search algorithm to choose the best move. It searches 2 moves in, using a `ForkJoinTask` to parallelize the process. It randomly selects a move among the 3 best move with a probability proportional to its score.

A rough outline of the program is as follows:

A player (whether human or computer) should extend the Player class. It should return the selected move when `getMove()` is called. If any exceptions are thrown, it will try again.

All pieces have their own class, all of which extend the class `Piece`.
All pieces have the `getMoves()` function which returns a `Move` array representing all possible moves given it's position on the board, and the pieces around it. (Note that all moves returned by this function may not be legal)
`Piece`s have a square property, which represents which square they are on.
Instantiating a `Piece` does not automatically place it onto the Board.
Pieces have a `Board` property which says which board they are on.
Pieces have a `move()` function which moves the `Piece`, but does NOT capture the piece, if the move is a capturing move. It does not check if the supplied square is a legal move. This is for internal use only.
The `Move` object has the starting `Square`, ending `Square`, the `Piece` which does the move, and a boolean denoting whether it captures or not. Additionally, it can also represent castles. To do so, use the alternate `new Move(boolean kingSideCastle)` constructor. Pass `true` to represent a king side castle, and `false` for a queen side castle.

The `Square` object represents a square on the chess board. Instead of a 2D array representing the board, the `Board` object simply has an ArrayList of Pieces, each of which have a Square property.

The `Board` holds all the pieces, and has a `checkSquare()` function, which checks the `Square` given to it as a parameter and returns the `Piece` which is on that square. If no piece occupies that square, it returns null.
