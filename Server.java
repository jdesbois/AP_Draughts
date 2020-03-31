import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
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
        private ObjectOutputStream oos;
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
                oos = new ObjectOutputStream(client.getOutputStream());
                ois = new ObjectInputStream(client.getInputStream());
                //Sends initial packet that passes Model to client and assigns client ID number.
                packet = new Packet(-1);
                initialPacket();
                oos.writeUnshared(packet);
                oos.flush();
                while (true) {
                    //Reads in packet from client. Displays Client ID and Status code of packet being read.
                    packet = (Packet) ois.readUnshared();
                    System.out.println("Reading packet statuscode: " + packet.getStatusCode() + " from client " + this.clientID);
                    //Checks if two clients are connected and responds with new status code if they are
                    if (packet.getStatusCode() == 1) {
                        System.out.println("Checking for two clients!");
                        if (nClients != 3) {
                            packet.setStatusCode(-2);
                        } else {
                            System.out.println("Two clients connected, sending status");
                            packet.setStatusCode(2);
                        }
                    }
                    if (model.checkGameOver()) {
                        packet.setStatusCode(11);
                        packet.setModel(model);
                        oos.writeUnshared(packet);
                    }
                    //After both clients have connected checks packet for status codes of each clients state.
                    //IF -3 client is asking for game to be started
                        if (packet.getStatusCode() == -3) {
                            startGame();
                        }
                        //if 4 client is stating ready and waiting for move to be made
                        if (packet.getStatusCode() == 4) {
                            System.out.println("Client: " + this.clientID +" ready, waiting for move to be played!");
                            updateModel();
                            packet.setStatusCode(6);
                        }
                        //if 5 a client has made a move and the model must be unpacked, updated and sent out
                        if (packet.getStatusCode() == 5) {
                            moveReceived();
                        }
                    //Prints out status code and client before sending packet
                    //Uses writeUnshared to make sure it is a new serialized object each write
                    System.out.println("Sending packet statuscode: " + packet.getStatusCode() + " to client " + this.clientID);
                    oos.writeUnshared(packet);
                    oos.flush(); 
                    oos.reset();
                    Thread.sleep(1000);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }
    //Response methods
    //This is the initial packet sent when clients connect, passing the model and assign a client ID
    public void initialPacket() {
        packet.lock();
        try {
            System.out.println("Sending initial packet");
            packet.setModel(model);
            packet.setID(this.clientID);
        } finally {
            packet.unlock();
        }
    }

    public void updateModel() {
        packet.setModel(model);
    }
    public void startGame() {
        packet.lock();
        try {
            System.out.println("Sending start game package!");
            model.startGame();
            packet.setModel(model);
            packet.setStatusCode(3);

        } finally {
            packet.unlock();
        }

    }
    //This method runs if a move is made by a client - the server model is updated from the packet model and set back into the model to be sent out again
    public void moveReceived() {
        packet.lock();
        try {
            System.out.println("Move by" + this.clientID + ", sending updated model");
            model = packet.getModel();
            packet.setModel(model);
            packet.setStatusCode(6);
        } finally {
            packet.unlock();
        }
    }
}// End of ClientThread
//Method to initiate the server which waits for a connection attempt from the client
    public void runServer() throws IOException {
        
        ServerSocket server = new ServerSocket(8765);
            model = new Model();
            listsOfClients = new ArrayList<ClientThread>();
            System.out.println("Server started, waiting for clients");
            while(true) {
                try {
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