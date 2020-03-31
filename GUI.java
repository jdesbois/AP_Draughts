import javax.swing.*;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import javax.swing.border.Border;

import java.awt.event.*;

import java.awt.Color;

import java.awt.*;

public class GUI extends JFrame {

    private int nRows = 8;
    private int nCols = 8;
    private JPanel[][] board = new JPanel[nRows][nCols];
    private Container container;
    private JPanel hud;
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

    // Key for identifying pieces.
    static final int EMPTY = 0, RED = 1, RED_KING = 3, BLACK = 2, BLACK_KING = 4;

    public GUI(Model model, int playerID) {
        this.model = model;
        boardState = model.getBoard();
        this.playerID = playerID;
        initUI();
    }

    public void initUI() {
        container = new JPanel();
        board = createBoard();
        hud = new JPanel();
        separator = new JPanel();

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

    public void startGame() {
        model.startGame();
    }
    public void playRound() {
        setStatus("Player " + model.getActivePlayer() + " make a move!");
        
        model.playRound();
    }
    public boolean isMoveMade() {
        return moveMade;
    }
    public void setMoveMade(boolean b) {
        moveMade = b;
    }
    public int getPlayerID() {
        return playerID;
    }

    public void updateBoard() {
        this.boardState = model.getBoard();
        repaint();
    }
    public void endGame() {
        model.endGame();
        if (model.checkWin()) {
            setStatus("Game over! Player: " + model.getActivePlayer() + " wins!");
        }
        if (model.checkDraw()) {
            setStatus("Game over! its a draw!");
        }
    }

    public void makeMove(int fr, int fc, int tr, int tc) {
        if (model.makeMove(fr, fc, tr, tc)) {
            model.makeMove(fromRow, fromCol, toRow, toCol);
            fromRow = -1;
            fromCol = -1;
            toRow = -1;
            toCol = -1;
            updateBoard();
        } else {
            System.out.println("Illegal move!");
            status.setText("Can't make that move!");
        }
    }

    public void updateModel(Model model) {
        this.model = model;
        this.boardState = model.getBoard();
        this.activePlayer = model.getActivePlayer();
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
        Model model = new Model();
        model.startGame();
        GUI view = new GUI(model, 1);
    }

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
            Move[] moves = model.getMoves();
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
            if (model.isGameInProgress()) {
                    for (Move move : moves) {
                            if (move.fR == row && move.fC == col) {
                                g.setColor(Color.MAGENTA);
                                g.drawRoundRect(0,0, getWidth(), getHeight(), 20,15);
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
        /**
         * MouseEvent for selecting a piece and making a move; 
         * If selected square has a piece on it then it is added to fromRow and fromCol 
         * If the next selected piece doesn't have a piece on it then toRow and toCol is added
         * This is passed to makeMove() funtion which will either make the move or ask for a different one
         */
        public void mousePressed(MouseEvent e) {
            if (model.getActivePlayer() == playerID) {
                BoardSpace selectedSpace = (BoardSpace) e.getSource();
                int row = selectedSpace.getRow();
                int col = selectedSpace.getCol();
                int piece = model.getPiece(row, col);
                System.out.println("Piece: " + piece + " row:" + row + " col:" + col);
                Move[] moves = model.getMoves();
                for (Move move : moves) {
                    System.out.println(move);
                }
                // checks the current boardstate to see if the selected space has a counter on
                // it
                if (piece == model.getActivePlayer() || piece == BLACK_KING || piece == RED_KING) {
                    status.setText("Counter selected at Row: " + row + " Column: " + col);
                    fromRow = row;
                    fromCol = col;
                    System.out.println("Repainting");
                    updateBoard();
                } else if (row % 2 == col % 2) {                         
                    status.setText("Space selected at Row: " + row + " Column: " + col);
                    toRow = row;
                    toCol = col;
                    model.makeMove(fromRow, fromCol, toRow, toCol);
                }
                
            } else {
                setStatus("Not your turn!");
            }

        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseClicked(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }
    }
}