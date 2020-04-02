import javax.swing.*;
import java.awt.GridLayout;

import java.awt.BorderLayout;



import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.awt.Color;

import java.awt.*;

public class GUI extends JFrame {

    private int nRows = 8;
    private int nCols = 8;
    private JPanel[][] board = new JPanel[nRows][nCols];
    private Container container;
    private JPanel hud;
    private JButton newGame;
    private JButton resignGame;
    private static int fromRow = -1;
    private static int fromCol = -1;
    private static int toRow = -1;
    private static int toCol = -1;
    private JLabel status = new JLabel();
    private JLabel playerStatus = new JLabel();
    private Model model;
    private int[][] boardState;
    public boolean moveMade = false;
    private int playerID;
    private int activePlayer;
    private JPanel separator;
    private ObjectOutputStream oos;
    private boolean resigned = false;

    // Key for identifying pieces.
    static final int EMPTY = 0, RED = 1, RED_KING = 3, BLACK = 2, BLACK_KING = 4;
    /**
     * Constructor that takes Model, playerID, ObjectOutputStream
     *  Runs InitiUI which will initialise components and set them visible to user
     */
    public GUI(Model model, int playerID, ObjectOutputStream oos) {
        this.model = model;
        this.playerID = playerID;
        this.oos = oos;
        initUI();
    }
    public int getFromRow(){
        return fromRow;
    }
    public int getFromCol(){
        return fromCol;
    }
    public int getToRow(){
        return toCol;
    }
    public int getToCol(){
        return toCol;
    }
    /**
     * Method to initialise components of the UI
     * Adds action listeners to both buttons
     */
    public void initUI() {
        container = new JPanel();
        board = createBoard();
        hud = new JPanel();
        separator = new JPanel();
        newGame = new JButton("New Game!");
        //Listener for newGame button that sends communication to server to restart the model.
        newGame.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent a) {
                newGame.setEnabled(false);
                try {
                    oos.writeObject("newGame");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                 
            }
        });
        resignGame = new JButton("Resign");
        //Listener on resign button that ends game for both players. 
        resignGame.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try {
                    oos.writeObject("resigned");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        separator.add(newGame);
        separator.add(resignGame);
        newGame.setEnabled(false);
        separator.setSize(100,100);
        container.setSize(750, 600);
        container.setLayout(new GridLayout(8, 8, 1, 1));
        hud.setSize(750, 150);
        setLayout(new BorderLayout());
        setSize(750, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        playerStatus.setText("You are player number " + playerID);

        hud.add(playerStatus);
        hud.add(separator);
        hud.add(status);
        add(container, BorderLayout.CENTER);
        add(hud, BorderLayout.SOUTH);

        container.setVisible(true);
        setVisible(true);
    }

    public int getPlayerID() {
        return playerID;
    }
    public void startGame() {
        boardState = model.getBoard();
        repaint();
    }
    /**
     * Method to check whether the player can jump again
     * Checks and updates text for players turn
     * Checks for a game ending condition
     * Makes sure newGame button is disabled while game is in progress
     */
    public void updateBoard() {
        this.boardState = model.getBoard();
        if (model.isCanJump()) {
            setStatus("You can jump again! Player " + model.getActivePlayer());
        }
        if (playerID == model.getActivePlayer()) {
            playerStatus.setText("You are player number " + playerID + ", its your turn");
        } else {
            playerStatus.setText("You are player number " + playerID);
        }
        if (model.checkGameOver() || model.isResigned()) {
            endGame();
        }
        if (model.isGameInProgress()) {
            newGame.setEnabled(false);
        }
        repaint();
    }
    /**
     * Method that is triggered is a game win/draw condition is met or a player hits the resign button
     */
    public void endGame() {
        model.endGame();
        newGame.setEnabled(true);
        if (model.checkWin()) {
            setStatus("Game over! Player: " + model.getWinner() + " wins!");
        }
        if (model.checkDraw()) {
            setStatus("Game over! its a draw!");
        }
        if (model.isResigned()) {
            setStatus("Game over, player resigned!");
        }
        repaint();
    }

    /**
     * Method to update the model sent to the client from the server
     * @param model
     */
    public void updateModel(Model model) {
        this.model = model;
        this.boardState = model.getBoard();
        this.activePlayer = model.getActivePlayer();
        repaint();
    }

    public void setStatus(String s) {
        status.setText(s);
    }
    public int getActivePlayer() {
        return activePlayer;
    }

    /**
     * Creates the JPanel board array using customer JPanel objects
     * 
     * @return JPanel 8x8 2D Array
     */
    public JPanel[][] createBoard() {
        JPanel[][] board = new JPanel[nRows][nCols];
        for (int row = 0; row < nRows; row++) {
            for (int col = 0; col < nCols; col++) {
                BoardSpace panel = new BoardSpace(row, col);
                board[row][col] = panel;
                container.add(board[row][col]);
            }
        }
        return board;
    }

    public static void main(String[] args) {
    }
    /**
     * Custom JPanel class
     * This class has coordinates set for row and col when it is added to the game board
     * It implements mouse listener to facilitate the ability to select a counter or board space.
     * It uses Graphics to create the board and counters drawing from the model game state board to place them.
     */
    public class BoardSpace extends JPanel implements MouseListener {
        /**
         *
         */
        private static final long serialVersionUID = -7274013949854931770L;
        private int row;
        private int col;

        public BoardSpace(int row, int col) {
            this.row = row;
            this.col = col;
            addMouseListener(this);
        }
        public int getRow() {
            return row;
        }
        public int getCol() {
            return col;
        }
        /**
         * Creates board and counters graphic
         * Higlights moveable pieces if game in progress
         * Highlights available moves if player clicks a counter
         */
        public void paintComponent(Graphics g) {
            Move[] moves = model.checkLegalMoves(model.getActivePlayer());
            if (row % 2 == col % 2) {
                g.setColor(Color.lightGray);
            } else {
                g.setColor(Color.white);
            }
            if (boardState[row][col] == 0 && row % 2 == col % 2) {
                g.setColor(Color.lightGray);
            }
            g.fillRect(0, 0, getWidth(), getHeight());

            if (boardState[row][col] == BLACK) {
                g.setColor(Color.BLACK);
                g.fillOval(0, 0, getWidth(), getHeight());
            } else if (boardState[row][col] == RED) {
                g.setColor(Color.RED);
                g.fillOval(0, 0, getWidth(), getHeight());
            } else if (boardState[row][col] == RED_KING) {
                g.setColor(Color.RED);
                g.fillOval(0, 0, getWidth(), getHeight());
                g.setColor(Color.yellow);
                g.drawString("K", getHeight() / 2, getWidth() / 2);
            } else if (boardState[row][col] == BLACK_KING) {
                g.setColor(Color.BLACK);
                g.fillOval(0, 0, getWidth(), getHeight());
                g.setColor(Color.yellow);
                g.drawString("K", getHeight() / 2, getWidth() / 2);
            }
            /**
             * IF the game is in progress and the moves array is not empty
             *  The counters that can be moved will be highlighted 
             *  Once a counter is selected the legal moves for that counter will be highlighted. 
             */
            if (moves == null ) {
                return;
            } else {
                if (model.isGameInProgress()) {

                    for (Move move : moves) {
                            if (move.fR == row && move.fC == col) {
                                g.setColor(Color.GREEN);
                                g.drawRoundRect(0,0, getWidth(), getHeight(), 20,20);
                            }
                    }
            }   
            if (fromRow >= 0) {
                for (Move move : moves) {
                    if ((move.tR == row && move.tC == col) && (move.fR == fromRow && move.fC == fromCol)) {
                        g.setColor(Color.ORANGE);
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                }
            }
            }

        }
        /**
         * MouseEvent for selecting a piece and making a move; 
         * If selected square has a piece on it then it is added to fromRow and fromCol 
         * If the next selected piece doesn't have a piece on it then toRow and toCol is added
         * This is validated against the list of legalmoves for that player and then sent to the server for the model to be updated
         */
        public void mousePressed(MouseEvent e) {
            if (model.getActivePlayer() == playerID) {
                BoardSpace selectedSpace = (BoardSpace) e.getSource();
                int row = selectedSpace.getRow();
                int col = selectedSpace.getCol();
                int piece = model.getPiece(row, col);
                System.out.println("Piece: " + piece + " row:" + row + " col:" + col);
                Move[] moves = model.checkLegalMoves(model.getActivePlayer());

                /**
                 * Logic for a player to select a move
                 * Checks current board state to see if the selected coords contain a piece or empty space.
                 * If it has a piece the fromRow fromCol variables are set
                 * If a square on the board is selected then the toRow and toCol variables are set
                 * These variabels are then validated against the list of legal moves created by the model 
                 * If they are valid the Move object is sent to the server for processing
                 * 
                 * This logic is wrapped in logic to check for an active player, if the active player isn't the current ID of the UI then a move cannot be made.
                 */
                if (piece == model.getActivePlayer() || piece == BLACK_KING || piece == RED_KING) {
                    for (Move move : moves) {
                        System.out.println(move);
                    }
                    status.setText("Counter selected at Row: " + row + " Column: " + col);
                    System.out.println("Counter Clicked");
                    fromRow = row;
                    fromCol = col;
                    System.out.println("Repainting");
                    repaint();
                } else if (row % 2 == col % 2) {                         
                    status.setText("Space selected at Row: " + row + " Column: " + col + "  Click again to confirm!");
                    System.out.println("Square Clicked");
                    toRow = row;
                    toCol = col;
                    for (Move move : moves) {
                        if ((move.fC == fromCol && move.fR == fromRow) && (move.tR == toRow && move.tC == toCol)) {
                                try {
                                    System.out.println("Player " + playerID + " sending move.");
                                    oos.writeObject(move);
                                    oos.flush();
                                    oos.reset();
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }   
                        }
                    }
                }
            } else {
                setStatus("Wait your turn!");
            }

        }

        public void mouseClicked(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }
    }
}