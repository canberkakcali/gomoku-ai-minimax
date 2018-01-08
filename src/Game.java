import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


public class Game {

	private Board board;
	private boolean isPlayersTurn = true;
	private boolean gameFinished = false;
	private int minimaxDepth = 3;
	private boolean aiStarts = true; // AI makes the first move
	private Minimax ai;
	public static final String cacheFile = "score_cache.ser";
	private int winner; // 0: There is no winner yet, 1: AI Wins, 2: Human Wins
	
	
	public Game(Board board) {
		this.board = board;
		ai = new Minimax(board);
		
		winner = 0;
	}
	/*
	 * 	Loads the cache and starts the game, enabling human player interactions.
	 */
	public void start() {
		
		
		// If the AI is making the first move, place a white stone in the middle of the board.
		if(aiStarts) playMove(board.getBoardSize()/2, board.getBoardSize()/2, false);
		// Now it's human player's turn.
		
		// Make the board start listening for mouse clicks.
		board.startListening(new MouseListener() {

			public void mouseClicked(MouseEvent arg0) {
				if(isPlayersTurn) {
					isPlayersTurn = false;
					// Handle the mouse click in another thread, so that we do not held the event dispatch thread busy.
					Thread mouseClickThread = new Thread(new MouseClickHandler(arg0));
					mouseClickThread.start();
					
					
				}
			}

			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}
	/*
	 * 	Sets the depth of the minimax tree. (i.e. how many moves ahead should the AI calculate.)
	 */
	public void setAIDepth(int depth) {
		this.minimaxDepth = depth;
		
	}
	public void setAIStarts(boolean aiStarts) {
		this.aiStarts = aiStarts;
	}
	public class MouseClickHandler implements Runnable{
		MouseEvent e;
		public MouseClickHandler(MouseEvent e) {
			this.e = e;
		}
		public void run() {
			if(gameFinished) return;
			
			// Find out which cell of the board do the clicked coordinates belong to.
			 
			int posX = board.getRelativePos( e.getX() );
			int posY = board.getRelativePos( e.getY() );
			
			// Place a black stone to that cell.
			if(!playMove(posX, posY, true)) {
				// If the cell is already populated, do nothing.
				isPlayersTurn = true;
				return;
			}
			
			// Check if the last move ends the game.
			winner = checkWinner();
			
			if(winner == 2) {
				System.out.println("Player WON!");
				board.printWinner(winner);
				gameFinished = true;
				return;
			}
			
			// Make the AI instance calculate a move.
			int[] aiMove = ai.calculateNextMove(minimaxDepth);
			
			if(aiMove == null) {
				System.out.println("No possible moves left. Game Over.");
				board.printWinner(0); // Prints "TIED!"
				gameFinished = true;
				return;
			}
			
			
			// Place a black stone to the found cell.
			playMove(aiMove[1], aiMove[0], false);
			
			System.out.println("Black: " + Minimax.getScore(board,true,true) + " White: " + Minimax.getScore(board,false,true));
			
			winner = checkWinner();
			
			if(winner == 1) {
				System.out.println("AI WON!");
				board.printWinner(winner);
				gameFinished = true;
				return;
			}
			
			if(board.generateMoves().size() == 0) {
				System.out.println("No possible moves left. Game Over.");
				board.printWinner(0); // Prints "TIED!"
				gameFinished = true;
				return;
				
			}
			
			isPlayersTurn = true;
		}
		
	}
	private int checkWinner() {
		if(Minimax.getScore(board, true, false) >= Minimax.getWinScore()) return 2;
		if(Minimax.getScore(board, false, true) >= Minimax.getWinScore()) return 1;
		return 0;
	}
	private boolean playMove(int posX, int posY, boolean black) {
		return board.addStone(posX, posY, black);
	}
	
}
