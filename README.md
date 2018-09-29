# JavaChessAI
A primitive AI which plays Chess.

This was designed modularly, so it should be fairly easy to customize. The Chess class is the main class which creates a Board and then plays the game between the two `Player`s.

The current implementation understands and enforces the rules of chess, with the exception on en passant. ~~It is not touch screen, and chess moves must be entered as text through the console in long notation. (ex: Nb1-c3)~~ Now there is both a `HumanConsolePlayer` and `HumanGUIPlayer`. The `HumanConsolePlayer` allows the player to type in their move using long notation. `HumanGUIPlayer` lets the player use their mouse to move around the pieces. Click on the piece to move, and click on the square you want to move the piece to.

Some things are a bit wonky, however. Non-castle moves are one call using the `doMove()` method of the `Board` object. Castle moves must be manually done by checking the path and moving the king and rook. Also, pawn promotions are handled by passing a Scanner to the move function of the Pawn, and it will query the console for the promotion choice. This will be improved upon in the future.

There are two implementations of a computer AI.

- `ComputerPlayer` class. The AI works by checking all possible moves and calculating a score based on the resulting positions. The score is based on the amount of space controlled by the AI, and the material (pieces) that it has alive. It offsets piece value by individual positional value to encourage better positional play by the AI. It calculates a score for each possible move, and then picks the move that yields the highest score. The result is that it can play pseudo-positionally, but cannot respond to threats. Because of it's inability to look ahead, it will frequently make stupid captures. However, the opponent has a safeMove() function which is being improved upon, which determines if they will lose the piece depending on their move, and should help protect against stupid captures.

- `BetterComputerPlayer` class. This uses a minimax search algorithm to choose the best move. It uses iterative deepining depth first search, so that when it times out it will have have some moves already selected. Instead of evaluating to a specified depth, it calculates for a certain amount of time, and then returns the best move it found. It uses the same cost function as `ComputerPlayer` to evaluate positions. It also uses positional piece evaluation using piece position tables. It used to use Alpha-Beta tree pruning to improve performance, but it wasn't working, so it's temporarily disabled for now.

A rough outline of the program is as follows:

A player (whether human or computer) should extend the Player class. It should return the selected move when `getMove()` is called. If any exceptions are thrown, it will try again.

All pieces have their own class, all of which extend the class `Piece`.
All pieces have the `getMoves()` function which returns a `Move` array representing all possible moves given it's position on the board, and the pieces around it. (Note that all moves returned by this function may not be legal)
`Piece`s have a square property, which represents which square they are on.
Instantiating a `Piece` does not automatically place it onto the Board.
Pieces have a `Board` property which says which board they are on.
Pieces have a `move()` function which moves the `Piece`, but does NOT capture the piece, if the move is a capturing move. It does not check if the supplied square is a legal move.
The `Move` object has the starting `Square`, ending `Square`, the `Piece` which does the move, and a boolean denoting whether it captures or not.

The `Square` object represents a square on the chess board. Instead of a 2D array representing the board, the `Board` object simply has an ArrayList of Pieces, each of which have a Square property.

The `Board` holds all the pieces, and has a `checkSquare()` function, which checks the `Square` given to it as a parameter and returns the `Piece` which is on that square. If no piece occupies that square, it returns null.
The `Board` also has a `checkScore(int team)` function which checks the score of the team passed as a parameter. The team can be `Board.BLACK(-1)` or `Board.WHITE(1)`.
