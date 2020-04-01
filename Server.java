import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class Server {
    private static int nClients = 1;
    private static ArrayList<ClientThread> listsOfClients;
    private static Model model;
    private static Packet packet;

//Beginning of ClientThread
    public static class ClientThread extends Thread {
        final Socket client;
        private int clientID;
        private Thread writeThread;
        private ObjectInputStream ois;
        
        
        /**
         * Constructor that takes socket of client and int of clientID
         * @param client
         * @param clientID
         */
        public ClientThread(Socket client, int clientID) {
            this.client = client;
            this.clientID = clientID;
        }

        public void run() {
            
            try {
                packet = new Packet(model);
                Thread writeThread = new Thread(new Writer(this.client, this.clientID, model, packet));
                writeThread.start();
                ois = new ObjectInputStream(client.getInputStream());
                //Sends initial packet that passes Model to client and assigns client ID number.
                while (true) {
                    Object object = ois.readObject();
                    System.out.println("Reading packet");
                    if (object instanceof Move) {
                        /**
                         * Reads in Move object from client and plays move
                         */
                        Move move = (Move) ois.readObject();
                        model.makeMove(move.fR, move.fC, move.tR, move.tC);
                        /**
                         * Checks to see if player can jump again
                         * If Yes doesn't switch players and flags model to let player jump on
                         * If No switches players and carries on
                         */
                        if (move.isJump() && model.getJumpsFrom(model.getActivePlayer(), move.tR, move.tC) != null) {
                            Move[] jumps = model.getJumpsFrom(model.getActivePlayer(), move.tR, move.tC) ;
                            for (Move jump: jumps) {
                                System.out.println(jump);
                            }
                            if (jumps != null) {
                                model.setCanJump(true);
                            }
                        } else {
                            model.setCanJump(false);
                            model.switchPlayer();
                        }
                    }
                    /**
                     * Checks to see if incoming object is the string to start a new game
                     */
                    if (object.equals("newGame")) {
                        model.startGame();
                    }
                    /**
                     * Checks to see if incoming object is the string to resign the game
                     */
                    if (object.equals("resigned")) {
                        model.setResigned(true);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
    }
}// End of ClientThread
//Method to initiate the server which waits for a connection attempt from the client
    public void runServer() throws IOException {
        
        ServerSocket server = new ServerSocket(8765);
            
            model = new Model();
            model.startGame();
            listsOfClients = new ArrayList<ClientThread>();
            System.out.println("Server started, waiting for clients");
            while(true) {
                try {
                    if (nClients > 2) {
                        break;
                    }
                    //Accept clients
                    Socket client = server.accept();
                    System.out.println("Client number " + nClients + " has joined the server!");
                    //Create new Client thread and assign ID
                    ClientThread clientThread = new ClientThread(client, nClients);
                    //Start client thread
                    clientThread.start();
                    //Add client thread to hashmap with associated ID
                    listsOfClients.add(clientThread);
                    //Increase ID counter
                    nClients++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } 
    }
    public boolean clientsFull() {
        return nClients >= 3;
    }

    public static void main(String[] args) {
        try {
            new Server().runServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        

    }
} // End of Server