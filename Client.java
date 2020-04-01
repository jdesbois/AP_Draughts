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
            connection = new Socket("127.0.0.1", 8765);
            ois = new ObjectInputStream(connection.getInputStream());
            oos = new ObjectOutputStream(connection.getOutputStream());
            packet = (Packet) ois.readObject();
            clientID = packet.getID();
            model = packet.getModel();
            view = new GUI(model, clientID, oos, ois);
            System.out.println("GUI Launched - client:" + clientID+ " ready");
            while (true) {
                System.out.println("Client: "+ clientID + " reading packet"); 
                packet = (Packet) ois.readObject();
                view.updateModel(packet.getModel());
                view.updateBoard();
             }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}