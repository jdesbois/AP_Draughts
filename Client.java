import java.net.*;
import java.io.*;



public class Client {
    private static Model model; 
    private static Packet packet;

    public static void main(String[] args) {
        Socket connection = null;
        GUI view = null;
        int clientID;
        ObjectInputStream ois; 
        ObjectOutputStream oos;

        try {
            //Creates initial connection to server
            connection = new Socket("127.0.0.1", 8765);
            //Creates the object input and output streams
            ois = new ObjectInputStream(connection.getInputStream());
            oos = new ObjectOutputStream(connection.getOutputStream());
            //Reads initial packet from server setting Model, ClientID and initialising UI
            packet = (Packet) ois.readObject();
            clientID = packet.getID();
            model = packet.getModel();
            view = new GUI(model, clientID, oos);
            //originally in place to initiate the handshake between client and server, since has been deprecated
            // oos.writeObject(packet);
            // oos.flush();
            // oos.reset();
            System.out.println("GUI Launched - client:" + clientID+ " ready");
            while (true) {
                //Continous loop to read packet being sent from server and update the view's model reference and the current board UI
                System.out.println("Client: "+ clientID + " reading packet"); 
                packet = (Packet) ois.readObject();
                view.updateModel(packet.getModel());
                view.updateBoard();
             }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}