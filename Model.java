import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Model implements Serializable{
    /**
     *
     */
    private static final long serialVersionUID = -819388264372277152L;
    /**
     *
     */
    static final int 
        EMPTY = 0,
        RED = 1,
        RED_KING = 3,
        BLACK = 2,
        BLACK_KING = 4;

    public int[][] board;
    private Lock lock = new ReentrantLock();
    private int activePlayer;
    private boolean gameInProgress;
    private boolean moveMade; 
    private Move[] moves;

    public Model() {
        board = new int[8][8];
        initialBoard();
    }
    
    public void startGame() {
        gameInProgress = true;
        activePlayer = RED;
        moves = checkLegalMoves(getActivePlayer());
    }
    public void endGame() {
        gameInProgress = false;
    }
    public void playRound() {
        moves = checkLegalMoves(getActivePlayer());
        moveMade = false;
        
    }
    public boolean checkGameOver() {
        if (checkWin()) {
            return true;
        }
        if (checkDraw()) {
            return true;
        }
        return false;
    }

    public boolean checkDraw() {
        Move[] redMoves = checkLegalMoves(RED);
        Move[] blackMoves = checkLegalMoves(BLACK);
        if (blackMoves.length < 0 && redMoves.length < 0) {
            return true;
        }
        return false;
    }
    public boolean checkWin() {
        int blackCounters = 0;
        int redCounters = 0;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (board[row][col] == RED || board[row][col] == RED_KING) {
                    redCounters++;
                } else if(board[row][col] == BLACK || board[row][col] == BLACK_KING) {
                    blackCounters++;
                }
            }
        }
        if (redCounters == 0 || blackCounters == 0) {
            return true;
        }
        return false;
    }
    public int getPiece(int row, int col) {
        return board[row][col];
    }
    public Move[] getMoves() {
        return moves;
    }
    public void initialBoard() {
        for (int row=0; row < 8; row++) {
            for (int col=0; col < 8; col++) {
                if (row%2 == col%2) {
                    if (row < 3) {
                        board[row][col] = BLACK;
                    } else if (row > 4) {
                        board[row][col] = RED;
                    } else {
                        board[row][col] = EMPTY;
                    }
                } else {
                    board[row][col] = EMPTY;
                }
            }
        }
    }
    public int[][] getBoard() {
        return board;
    }
    public long getID() {
        return serialVersionUID;
    }
    public int getPos(int row, int col) {
        return board[row][col];
    }
    public boolean isMoveMade(){
        return moveMade;
    }
    public void setMoveMade(boolean b) {
        moveMade = b;
    }
    public boolean makeMove(int fr, int fc, int tr, int tc) {
        moves = checkLegalMoves(getActivePlayer());

        for (Move move : moves) {
            if (move.fC == fc && move.fR == fr) {
                if (move.tR == tr && move.tC == tc) {
                    //If player is moving into end row then king player
                    if (tr == 0 || tr == 7) {
                        if (board[fr][fc] == RED) {
                            board[fr][fc] = RED_KING;
                        } else {
                            board[fr][fc] = BLACK_KING;
                        }            
                    }
                    // If move is a jump remove counter from jumped square.
                    if (move.isJump()) {
                        int jCol = (fc + tc) /2;
                        int jRow = (fr + tr) /2;
                        board[jRow][jCol] = EMPTY;
                        //check for follow up jumps after a jump
                        System.out.println("Checking follow up jumps for player:" + getActivePlayer());
                        Move[] followUpJumps = checkFollowUpJump(getActivePlayer(), move.tR, move.tC);
                        if (followUpJumps != null) {
                            board[tr][tc] = board[fr][fc];
                            board[fr][fc] = EMPTY; 
                            return true;
                        }
                    } else {
                        board[tr][tc] = board[fr][fc];
                        board[fr][fc] = EMPTY; 
    
                        switchPlayer();
                        moveMade = true;
                        return true;
                    }
                } 
            }
        }
        return false;
    }
    //Checks all pieces for all legal moves on the board. 
    public Move[] checkLegalMoves(int player) {
        ArrayList<Move> moves = new ArrayList<Move>();
        int playerKing;
        if (player == BLACK) {
            playerKing = BLACK_KING;
        } else {
            playerKing = RED_KING;
        }

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (board[row][col] == player || board[row][col] == playerKing) {
                    if (checkJump(player, row, col, row+1, col+1, row+2, col+2))
                        moves.add(new Move(row, col, row+2, col+2));
                    if (checkJump(player, row, col, row-1, col+1, row-2, col+2))
                        moves.add(new Move(row, col, row-2, col+2));
                    if (checkJump(player, row, col, row+1, col-1, row+2, col-2))
                        moves.add(new Move(row, col, row+2, col-2));
                    if (checkJump(player, row, col, row-1, col-1, row-2, col-2))
                        moves.add(new Move(row, col, row-2, col-2));
                }
            }
        } 
        System.out.println("Jumps: " + moves.size());
        if (moves.size() == 0) {
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    if (board[row][col] == player || board[row][col] == playerKing) {
                        if (canMove(player,row,col,row+1,col+1))
                            moves.add(new Move(row,col,row+1,col+1));
                        if (canMove(player,row,col,row-1,col+1))
                            moves.add(new Move(row,col,row-1,col+1));
                        if (canMove(player,row,col,row+1,col-1))
                            moves.add(new Move(row,col,row+1,col-1));
                        if (canMove(player,row,col,row-1,col-1))
                            moves.add(new Move(row,col,row-1,col-1));
                    }
                }
            }
        }
        System.out.println("Moves: " + moves.size());
        if (moves.size() == 0) {
            return null;
        } else {
            Move[] legalMoves = new Move[moves.size()];
            for (int i=0; i<moves.size(); i++) {
                legalMoves[i] = moves.get(i);
            }
            return legalMoves;
        }
    }
    /**
     * Method takes Current pos, plus position of piece to jump and landing position and returns true if legal jump.
     * @param r1
     * @param c1
     * @param r2
     * @param c2
     * @param r3
     * @param c3
     * @return
     */
    public boolean checkJump(int player, int r1, int c1, int r2, int c2, int r3, int c3) {
        if (r3 < 0 || r3 >= 8 || c3 < 0 || c3 >=8) {
            return false; //if landing position is off the board
        }
        if (board[r3][c3] != EMPTY) {
            return false; //if the landing position is not empty
        }
        if (player == BLACK) {
            if (board[r1][c1] == BLACK && r3 < r1) {
                return false; //normal pieces can only jump forward
            }
            if (board[r2][c2] != RED && board[r2][c2] != RED_KING) {
                return false; //No black piece for Red piece to jump
            }
            return true; 
        } else {
            if (board[r1][c1] == RED && r3 > r1) {
                return false; //normal pieces can only jump forward
            }
            if (board[r2][c2] != BLACK && board[r2][c2] != BLACK_KING) {
                return false; //No black piece for Red piece to jump
            }
            return true; 
        }
    }
    public boolean canMove(int player, int fR, int fC, int tR, int tC) {
        //if the to position is not on the board
        if (tR < 0 || tR >=8 || tC < 0 || tC >=8) {
            return false;
        }
        //if the to position is not empty
        if (board[tR][tC] != EMPTY) {
            return false;
        }
        if (player == RED) {
            if (board[fR][fC] == RED && tR > fR)
                return false;  // IF not king can't move backwards
            return true; 
        }
        else {
            if (board[fR][fC] == BLACK && tR < fR)
                return false;  // IF not king can't move backwards
            return true; 
        }
    }
    public boolean canPlayerJump() {
        Move[] moves = checkLegalMoves(activePlayer);
            for (Move move : moves) {
                if (move.isJump()) {
                    return true;
                }
            }
            return false;
        }
    public int getActivePlayer() {
        return activePlayer;
    }
    public void setActivePlayer(int player) {
        activePlayer = player;
    }
    public Move[] checkFollowUpJump(int player, int toRow, int toCol) {
        ArrayList<Move> moves = new ArrayList<Move>();
        
        if (board[toRow][toCol] == getActivePlayer() || board[toRow][toCol] == getActivePlayer() + 1) {
            if (checkJump(getActivePlayer(), toRow, toCol, toRow+1, toCol+1, toRow+2, toCol+2))
                moves.add(new Move(toRow, toCol, toRow+2, toCol+2));
            if (checkJump(getActivePlayer(), toRow, toCol, toRow-1, toCol+1, toRow-2, toCol+2))
                moves.add(new Move(toRow, toCol, toRow-2, toCol+2));
            if (checkJump(getActivePlayer(), toRow, toCol, toRow+1, toCol-1, toRow+2, toCol-2))
                moves.add(new Move(toRow, toCol, toRow+2, toCol-2));
            if (checkJump(getActivePlayer(), toRow, toCol, toRow-1, toCol-1, toRow-2, toCol-2))
                moves.add(new Move(toRow, toCol, toRow-2, toCol-2));
        }
        if (moves.size() == 0) {
            return null;
        } else {
            Move[] movesArray = new Move[moves.size()];
            for (int i=0; i < moves.size(); i++) {
                System.out.println(movesArray[i]);
                movesArray[i] = moves.get(i);
            }
            return movesArray;
        }
    }
     public void switchPlayer() {
        //switch player
        if (activePlayer == RED) {
            activePlayer = BLACK;
        } else {
            activePlayer = RED;
        }
        moves = checkLegalMoves(activePlayer);
    }
    public boolean isGameInProgress() {
        return gameInProgress;
    }
    public void lock() {
        lock.lock();
    }
    public void unlock() {
        lock.unlock();
    }
    public String toString() {
        String output = "";
        for (int row=0; row< 8; row++) {
            output += row + " |";
            for (int col=0; col<8; col++) {
                output += board[row][col] + "|";
            }
            output += "\n";
        }
        return output;
    }

    public static void main(String[] args) {
        Model model = new Model();
        System.out.println(model);
        System.out.println("making move");
        model.makeMove(5, 7, 4, 6);
        System.out.println(model);


    }
}