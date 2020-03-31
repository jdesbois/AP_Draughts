import java.net.*;
import java.io.*;



public class Client {

    
    // private static GUI view;
    // private static int clientID;
    private static ObjectInputStream ois; 
    private static ObjectOutputStream oos;
    private static Model model;
    private static Packet packet;

    public static void main(String[] args) {
        Socket connection = null;
        GUI view = null;
        int clientID = 0;

        try {
            connection = new Socket("127.0.0.1", 8765);
            ois = new ObjectInputStream(connection.getInputStream());
            oos = new ObjectOutputStream(connection.getOutputStream());
            while (true) {
                packet = (Packet) ois.readUnshared();
                System.out.println("Client: "+ clientID + " reading packet statuscode: " + packet.getStatusCode());
                /**
                 * Reads initial packet from server that passes Model and ClientID
                 * Sets up client GUI assigns model and clientID to GUI
                 */
                if (packet.getStatusCode() == -1) {
                    model = packet.getModel();
                    clientID = packet.getID();
                    view = new GUI(model, clientID);
                    view.setStatus("Waiting for players!");
                    packet.setID(clientID);
                    packet.setStatusCode(1);
                } 
                /**
                 * Receives response from server as to whether two clients are already connected
                 */
                if (packet.getStatusCode() == -2) {
                    System.out.println("Waiting for two players to connect");
                    packet.setStatusCode(1);
                }
                /**
                 * Status code 2: Means two clients are present and sends packet to receive start game message
                 */
                if (packet.getStatusCode() == 2) {
                    System.out.println("Two players connected, asking to start game");
                    packet.setID(clientID);
                    packet.setStatusCode(-3);
                }    
                /**
                 * Status code 11: This triggers once the server has found a win or draw condition
                 */
                if (packet.getStatusCode() == 11) {
                    view.updateModel(packet.getModel());
                    view.updateBoard();
                    view.endGame();
                }
                if (packet.getStatusCode() == 3) {
                    System.out.println("Start game");
                    model = packet.getModel();
                    view.updateModel(model);
                    view.updateBoard();
                    view.setStatus("Player " + model.getActivePlayer() + " make a move!");
                    packet.setStatusCode(4);
                }
                if (clientID == packet.getModel().getActivePlayer()) {
                    view.updateModel(packet.getModel());
                    view.updateBoard();
                    view.playRound();
                }
                // if (model.isMoveMade()) {
                //     System.out.println("Move Made by client: " + clientID);
                //     packet.setModel(model);
                //     packet.setStatusCode(5);
                // }
                if (packet.getStatusCode() == 6) {
                    model = packet.getModel();
                    view.updateModel(model);
                    view.updateBoard();
                    view.setStatus("Player " + view.getActivePlayer() + " make a move!");
                    System.out.println("Updating board");
                    packet.setStatusCode(4);
                }
                
                System.out.println("Client: "+ clientID+ " sending packet, statuscode: " + packet.getStatusCode());
                
                oos.writeUnshared(packet);
                oos.flush();
                oos.reset();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } 
           
    }
}