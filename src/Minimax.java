import java.util.ArrayList;


public class Minimax {
	
	// This variable is used to track the number of evaluations for benchmarking purposes.
	public static int evaluationCount = 0;
	// Board instance is responsible for board mechanics
	private Board board;
	// Win score should be greater than all possible board scores
	private static final int winScore = 100000000;
	
	// Constructor
	public Minimax(Board board) {
		this.board = board;
	}
	
	// Getter function for the winScore 
	public static int getWinScore() {
		return winScore;
	}

	// This function calculates the relative score of the white player against the black.
	// (i.e. how likely is white player to win the game before the black player)
	// This value will be used as the score in the Minimax algorithm.
	public static double evaluateBoardForWhite(Board board, boolean blacksTurn) {
		evaluationCount++; 
		
		// Get board score of both players.
		double blackScore = getScore(board, true, blacksTurn);
		double whiteScore = getScore(board, false, blacksTurn);
		
		if(blackScore == 0) blackScore = 1.0;
		
		// Calculate relative score of white against black
		return whiteScore / blackScore;
	}

	// This function calculates the board score of the specified player.
	// (i.e. How good a player's general standing on the board by considering how many 
	//  consecutive 2's, 3's, 4's it has, how many of them are blocked etc...)
	public static int getScore(Board board, boolean forBlack, boolean blacksTurn) {
		
		// Read the board
		int[][] boardMatrix = board.getBoardMatrix();

		// Calculate score for each of the 3 directions
		return evaluateHorizontal(boardMatrix, forBlack, blacksTurn) +
				evaluateVertical(boardMatrix, forBlack, blacksTurn) +
				evaluateDiagonal(boardMatrix, forBlack, blacksTurn);
	}
	
	// This function is used to get the next intelligent move to make for the AI.
	public int[] calculateNextMove(int depth) {
		// Block the board for AI to make a decision.
		board.thinkingStarted();

		int[] move = new int[2];

		// Used for benchmarking purposes only.
		long startTime = System.currentTimeMillis();

		// Check if any available move can finish the game to make sure the AI always
		// takes the opportunity to finish the game.
		Object[] bestMove = searchWinningMove(board);

		if(bestMove != null ) {
			// Finishing move is found.
			move[0] = (Integer)(bestMove[1]);
			move[1] = (Integer)(bestMove[2]);
			
		} else {
			// If there is no such move, search the minimax tree with specified depth.
			bestMove = minimaxSearchAB(depth, board, true, -1.0, getWinScore());
			if(bestMove[1] == null) {
				move = null;
			} else {
				move[0] = (Integer)(bestMove[1]);
				move[1] = (Integer)(bestMove[2]);
			}
		}
		System.out.println("Cases calculated: " + evaluationCount + " Calculation time: " + (System.currentTimeMillis() - startTime) + " ms");
		board.thinkingFinished();
		
		evaluationCount=0;
		
		return move;
	}
	
	
	/*
	 * alpha : Best AI Move (Max)
	 * beta : Best Player Move (Min)
	 * returns: {score, move[0], move[1]}
	 * */
	private static Object[] minimaxSearchAB(int depth, Board board, boolean max, double alpha, double beta) {

		// Last depth (terminal node), evaluate the current board score.
		if(depth == 0) {
			Object[] x = {evaluateBoardForWhite(board, !max), null, null};
			return x;
		}
		
		// Generate all possible moves from this node of the Minimax Tree
		/*
		 *                  (Move 1)
		 *	               /
		 *  (Current Node) --- (Move 2)
		 *				   \   ...
		 *                  (Move N)
		 */
		ArrayList<int[]> allPossibleMoves = board.generateMoves();
		
		// If there is no possible move left, treat this node as a terminal node and return the score.
		if(allPossibleMoves.size() == 0) {
			Object[] x = {evaluateBoardForWhite(board, !max), null, null};
			return x;
		}
		
		Object[] bestMove = new Object[3];
		
		// Generate Minimax Tree and calculate node scores.
		if(max) {
			// Initialize the starting best move with -infinity.
			bestMove[0] = -1.0;
			// Iterate for all possible moves that can be made.
			for(int[] move : allPossibleMoves) {
				// Create a temporary board that is equivalent to the current board
				Board dummyBoard = new Board(board);

				// Play the move on that temporary board without drawing anything
				dummyBoard.addStoneNoGUI(move[1], move[0], false);
				
				// Call the minimax function for the next depth, to look for a minimum score.
				// This function recursively generates new Minimax trees branching from this node 
				// (if the depth > 0) and searches for the minimum white score in each of the sub trees.
				// We will find the maximum score of this depth, among the minimum scores found in the
				// lower depth.
				Object[] tempMove = minimaxSearchAB(depth-1, dummyBoard, !max, alpha, beta);
				
				// Updating alpha (alpha value holds the maximum score)
				// When searching for the minimum, if the score of a node is lower than the alpha 
				// (max score of uncle nodes from one upper level) the whole subtree originating
				// from that node will be discarded, since the maximizing player will choose the 
				// alpha node over any node with a score lower than the alpha. 
				if((Double)(tempMove[0]) > alpha) {
					alpha = (Double)(tempMove[0]);
				}
				// Pruning with beta
				// Beta value holds the minimum score among the uncle nodes from one upper level.
				// We need to find a score lower than this beta score, because any score higher than
				// beta will be eliminated by the minimizing player (upper level). If the score is
				// higher than (or equal to) beta, break out of loop discarding any remaining nodes 
				// and/or subtrees and return the last move.
				if((Double)(tempMove[0]) >= beta) {
					return tempMove;
				}

				// Find the move with the maximum score.
				if((Double)tempMove[0] > (Double)bestMove[0]) {
					bestMove = tempMove;
					bestMove[1] = move[0];
					bestMove[2] = move[1];
				}
			}
		}
		else {
			// Initialize the starting best move using the first move in the list and +infinity score.
			bestMove[0] = 100000000.0;
			bestMove[1] = allPossibleMoves.get(0)[0];
			bestMove[2] = allPossibleMoves.get(0)[1];
			
			// Iterate for all possible moves that can be made.
			for(int[] move : allPossibleMoves) {
				// Create a temporary board that is equivalent to the current board
				Board dummyBoard = new Board(board);

				// Play the move on that temporary board without drawing anything
				dummyBoard.addStoneNoGUI(move[1], move[0], true);
				
				// Call the minimax function for the next depth, to look for a maximum score.
				// This function recursively generates new Minimax trees branching from this node 
				// (if the depth > 0) and searches for the maximum white score in each of the sub trees.
				// We will find the minimum score of this depth, among the maximum scores found in the
				// lower depth.
				Object[] tempMove = minimaxSearchAB(depth-1, dummyBoard, !max, alpha, beta);
				
				// Updating beta (beta value holds the minimum score)
				// When searching for the maximum, if the score of a node is higher than the beta 
				// (min score of uncle nodes from one upper level) the whole subtree originating
				// from that node will be discarded, since the minimizing player will choose the 
				// beta node over any node with a score higher than the beta. 
				if(((Double)tempMove[0]) < beta) {
					beta = (Double)(tempMove[0]);
				}
				// Pruning with alpha
				// Alpha value holds the maximum score among the uncle nodes from one upper level.
				// We need to find a score higher than this alpha score, because any score lower than
				// alpha will be eliminated by the maximizing player (upper level). If the score is
				// lower than (or equal to) alpha, break out of loop discarding any remaining nodes 
				// and/or subtrees and return the last move.
				if((Double)(tempMove[0]) <= alpha) {
					return tempMove;
				}
				
				// Find the move with the minimum score.
				if((Double)tempMove[0] < (Double)bestMove[0]) {
					bestMove = tempMove;
					bestMove[1] = move[0];
					bestMove[2] = move[1];
				}
			}
		}

		// Return the best move found in this depth
		return bestMove;
	}
	
	// This function looks for a move that can instantly win the game.
	private static Object[] searchWinningMove(Board board) {
		ArrayList<int[]> allPossibleMoves = board.generateMoves();
		Object[] winningMove = new Object[3];
		
		// Iterate for all possible moves
		for(int[] move : allPossibleMoves) {
			evaluationCount++;
			// Create a temporary board that is equivalent to the current board
			Board dummyBoard = new Board(board);
			// Play the move on that temporary board without drawing anything
			dummyBoard.addStoneNoGUI(move[1], move[0], false);
			
			// If the white player has a winning score in that temporary board, return the move.
			if(getScore(dummyBoard,false,false) >= winScore) {
				winningMove[1] = move[0];
				winningMove[2] = move[1];
				return winningMove;
			}
		}
		return null;
	}

	// This function calculates the score by evaluating the stone positions in horizontal direction
	public static int evaluateHorizontal(int[][] boardMatrix, boolean forBlack, boolean playersTurn ) {
		
		int consecutive = 0;
		// blocks variable is used to check if a consecutive stone set is blocked by the opponent or
		// the board border. If the both sides of a consecutive set is blocked, blocks variable will be 2
		// If only a single side is blocked, blocks variable will be 1, and if both sides of the consecutive
		// set is free, blocks count will be 0.
		// By default, first cell in a row is blocked by the left border of the board.
		// If the first cell is empty, block count will be decremented by 1.
		// If there is another empty cell after a consecutive stones set, block count will again be 
		// decremented by 1.
		int blocks = 2;
		int score = 0;
		
		// Iterate over all rows
		for(int i=0; i<boardMatrix.length; i++) {
			// Iterate over all cells in a row
			for(int j=0; j<boardMatrix[0].length; j++) {
				// Check if the selected player has a stone in the current cell
				if(boardMatrix[i][j] == (forBlack ? 2 : 1)) {
					// Increment consecutive stones count
					consecutive++;
				}
				// Check if cell is empty
				else if(boardMatrix[i][j] == 0) {
					// Check if there were any consecutive stones before this empty cell
					if(consecutive > 0) {
						// Consecutive set is not blocked by opponent, decrement block count
						blocks--;
						// Get consecutive set score
						score += getConsecutiveSetScore(consecutive, blocks, forBlack == playersTurn);
						// Reset consecutive stone count
						consecutive = 0;
						// Current cell is empty, next consecutive set will have at most 1 blocked side.
						blocks = 1;
					}
					else {
						// No consecutive stones.
						// Current cell is empty, next consecutive set will have at most 1 blocked side.
						blocks = 1;
					}
				}
				// Cell is occupied by opponent
				// Check if there were any consecutive stones before this empty cell
				else if(consecutive > 0) {
					// Get consecutive set score
					score += getConsecutiveSetScore(consecutive, blocks, forBlack == playersTurn);
					// Reset consecutive stone count
					consecutive = 0;
					// Current cell is occupied by opponent, next consecutive set may have 2 blocked sides
					blocks = 2;
				}
				else {
					// Current cell is occupied by opponent, next consecutive set may have 2 blocked sides
					blocks = 2;
				}
			}
			// End of row, check if there were any consecutive stones before we reached right border
			if(consecutive > 0) {
				score += getConsecutiveSetScore(consecutive, blocks, forBlack == playersTurn);
			}
			// Reset consecutive stone and blocks count
			consecutive = 0;
			blocks = 2;
		}

		return score;
	}
	
	// This function calculates the score by evaluating the stone positions in vertical direction
	// The procedure is the exact same of the horizontal one.
	public static  int evaluateVertical(int[][] boardMatrix, boolean forBlack, boolean playersTurn ) {
		
		int consecutive = 0;
		int blocks = 2;
		int score = 0;
		
		for(int j=0; j<boardMatrix[0].length; j++) {
			for(int i=0; i<boardMatrix.length; i++) {
				if(boardMatrix[i][j] == (forBlack ? 2 : 1)) {
					consecutive++;
				}
				else if(boardMatrix[i][j] == 0) {
					if(consecutive > 0) {
						blocks--;
						score += getConsecutiveSetScore(consecutive, blocks, forBlack == playersTurn);
						consecutive = 0;
						blocks = 1;
					}
					else {
						blocks = 1;
					}
				}
				else if(consecutive > 0) {
					score += getConsecutiveSetScore(consecutive, blocks, forBlack == playersTurn);
					consecutive = 0;
					blocks = 2;
				}
				else {
					blocks = 2;
				}
			}
			if(consecutive > 0) {
				score += getConsecutiveSetScore(consecutive, blocks, forBlack == playersTurn);
				
			}
			consecutive = 0;
			blocks = 2;
			
		}
		return score;
	}

	// This function calculates the score by evaluating the stone positions in diagonal directions
	// The procedure is the exact same of the horizontal calculation.
	public static  int evaluateDiagonal(int[][] boardMatrix, boolean forBlack, boolean playersTurn ) {
		
		int consecutive = 0;
		int blocks = 2;
		int score = 0;
		// From bottom-left to top-right diagonally
		for (int k = 0; k <= 2 * (boardMatrix.length - 1); k++) {
		    int iStart = Math.max(0, k - boardMatrix.length + 1);
		    int iEnd = Math.min(boardMatrix.length - 1, k);
		    for (int i = iStart; i <= iEnd; ++i) {
		        int j = k - i;
		        
		        if(boardMatrix[i][j] == (forBlack ? 2 : 1)) {
					consecutive++;
				}
				else if(boardMatrix[i][j] == 0) {
					if(consecutive > 0) {
						blocks--;
						score += getConsecutiveSetScore(consecutive, blocks, forBlack == playersTurn);
						consecutive = 0;
						blocks = 1;
					}
					else {
						blocks = 1;
					}
				}
				else if(consecutive > 0) {
					score += getConsecutiveSetScore(consecutive, blocks, forBlack == playersTurn);
					consecutive = 0;
					blocks = 2;
				}
				else {
					blocks = 2;
				}
		        
		    }
		    if(consecutive > 0) {
				score += getConsecutiveSetScore(consecutive, blocks, forBlack == playersTurn);
				
			}
			consecutive = 0;
			blocks = 2;
		}
		// From top-left to bottom-right diagonally
		for (int k = 1-boardMatrix.length; k < boardMatrix.length; k++) {
		    int iStart = Math.max(0, k);
		    int iEnd = Math.min(boardMatrix.length + k - 1, boardMatrix.length-1);
		    for (int i = iStart; i <= iEnd; ++i) {
		        int j = i - k;
		        
		        if(boardMatrix[i][j] == (forBlack ? 2 : 1)) {
					consecutive++;
				}
				else if(boardMatrix[i][j] == 0) {
					if(consecutive > 0) {
						blocks--;
						score += getConsecutiveSetScore(consecutive, blocks, forBlack == playersTurn);
						consecutive = 0;
						blocks = 1;
					}
					else {
						blocks = 1;
					}
				}
				else if(consecutive > 0) {
					score += getConsecutiveSetScore(consecutive, blocks, forBlack == playersTurn);
					consecutive = 0;
					blocks = 2;
				}
				else {
					blocks = 2;
				}
		        
		    }
		    if(consecutive > 0) {
				score += getConsecutiveSetScore(consecutive, blocks, forBlack == playersTurn);
				
			}
			consecutive = 0;
			blocks = 2;
		}
		return score;
	}

	// This function returns the score of a given consecutive stone set.
	// count: Number of consecutive stones in the set
	// blocks: Number of blocked sides of the set (2: both sides blocked, 1: single side blocked, 0: both sides free)
	public static  int getConsecutiveSetScore(int count, int blocks, boolean currentTurn) {
		final int winGuarantee = 1000000;
		// If both sides of a set is blocked, this set is worthless return 0 points.
		if(blocks == 2 && count < 5) return 0;

		switch(count) {
		case 5: {
			// 5 consecutive wins the game
			return winScore;
		}
		case 4: {
			// 4 consecutive stones in the user's turn guarantees a win.
			// (User can win the game by placing the 5th stone after the set)
			if(currentTurn) return winGuarantee;
			else {
				// Opponent's turn
				// If neither side is blocked, 4 consecutive stones guarantees a win in the next turn.
				if(blocks == 0) return winGuarantee/4;
				// If only a single side is blocked, 4 consecutive stones limits the opponents move
				// (Opponent can only place a stone that will block the remaining side, otherwise the game is lost
				// in the next turn). So a relatively high score is given for this set.
				else return 200;
			}
		}
		case 3: {
			// 3 consecutive stones
			if(blocks == 0) {
				// Neither side is blocked.
				// If it's the current player's turn, a win is guaranteed in the next 2 turns.
				// (User places another stone to make the set 4 consecutive, opponent can only block one side)
				// However the opponent may win the game in the next turn therefore this score is lower than win
				// guaranteed scores but still a very high score.
				if(currentTurn) return 50000;
				// If it's the opponent's turn, this set forces opponent to block one of the sides of the set.
				// So a relatively high score is given for this set.
				else return 200;
			}
			else {
				// One of the sides is blocked.
				// Playmaker scores
				if(currentTurn) return 10;
				else return 5;
			}
		}
		case 2: {
			// 2 consecutive stones
			// Playmaker scores
			if(blocks == 0) {
				if(currentTurn) return 7;
				else return 5;
			}
			else {
				return 3;
			}
		}
		case 1: {
			return 1;
		}
		}

		// More than 5 consecutive stones? 
		return winScore*2;
	}
}
