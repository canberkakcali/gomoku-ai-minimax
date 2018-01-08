import java.awt.event.MouseListener;
import java.util.ArrayList;



public class Board {
	
	private BoardGUI gui;
	private int[][] boardMatrix; // 0: Empty 1: White 2: Black
	
	
	public Board(int sideLength, int boardSize) {
		gui = new BoardGUI(sideLength, boardSize);
		boardMatrix = new int[boardSize][boardSize];
		
	}
	// Fake copy constructor (only copies the boardMatrix)
	public Board(Board board) {
		int[][] matrixToCopy = board.getBoardMatrix();
		boardMatrix = new int[matrixToCopy.length][matrixToCopy.length];
		for(int i=0;i<matrixToCopy.length; i++) {
			for(int j=0; j<matrixToCopy.length; j++) {
				boardMatrix[i][j] = matrixToCopy[i][j];
			}
		}
	}
	public int getBoardSize() {
		return boardMatrix.length;
	}
	public void addStoneNoGUI(int posX, int posY, boolean black) {
		boardMatrix[posY][posX] = black ? 2 : 1;
	}
	public boolean addStone(int posX, int posY, boolean black) {
		
		// Check whether the cell is empty or not
		if(boardMatrix[posY][posX] != 0) return false;
		
		gui.drawStone(posX, posY, black);
		boardMatrix[posY][posX] = black ? 2 : 1;
		return true;
		
	}
	public ArrayList<int[]> generateMoves() {
		ArrayList<int[]> moveList = new ArrayList<int[]>();
		
		int boardSize = boardMatrix.length;
		
		// Look for cells that has at least one stone in an adjacent cell.
		for(int i=0; i<boardSize; i++) {
			for(int j=0; j<boardSize; j++) {
				
				if(boardMatrix[i][j] > 0) continue;
				
				if(i > 0) {
					if(j > 0) {
						if(boardMatrix[i-1][j-1] > 0 ||
						   boardMatrix[i][j-1] > 0) {
							int[] move = {i,j};
							moveList.add(move);
							continue;
						}
					}
					if(j < boardSize-1) {
						if(boardMatrix[i-1][j+1] > 0 ||
						   boardMatrix[i][j+1] > 0) {
							int[] move = {i,j};
							moveList.add(move);
							continue;
						}
					}
					if(boardMatrix[i-1][j] > 0) {
						int[] move = {i,j};
						moveList.add(move);
						continue;
					}
				}
				if( i < boardSize-1) {
					if(j > 0) {
						if(boardMatrix[i+1][j-1] > 0 ||
						   boardMatrix[i][j-1] > 0) {
							int[] move = {i,j};
							moveList.add(move);
							continue;
						}
					}
					if(j < boardSize-1) {
						if(boardMatrix[i+1][j+1] > 0 ||
						   boardMatrix[i][j+1] > 0) {
							int[] move = {i,j};
							moveList.add(move);
							continue;
						}
					}
					if(boardMatrix[i+1][j] > 0) {
						int[] move = {i,j};
						moveList.add(move);
						continue;
					}
				}
				
			}
		}

		return moveList;
		
	}
	public int[][] getBoardMatrix() {
		return boardMatrix;
	}
	
	public void startListening(MouseListener listener) {
		gui.attachListener(listener);
	}
	public BoardGUI getGUI() {
		return gui;
	}
	public int getRelativePos(int x) {
		return gui.getRelativePos(x);
	}
	public void printWinner(int winner) {
		gui.printWinner(winner);
	}
	public void thinkingStarted() {
		gui.setAIThinking(true);
	}
	public void thinkingFinished() {
		gui.setAIThinking(false);
	}
	
	
}
