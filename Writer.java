import java.net.Socket;
import java.util.Random;
import java.io.IOException;
import java.io.ObjectOutputStream;;



public class Writer implements Runnable {
    private Socket socket;
    private Model model;
    private int clientID;
    public Packet packet;
    /**
     * Constructor for Writer that takes the static packet and model variables from the server
     * Also takes the socket for the client and assigned clientID
     */
    public Writer(Socket s, int clientID, Model model, Packet packet) {
        this.socket = s;
        this.packet = packet;
        this.clientID = clientID;
        this.model = model;
    }
    public void run() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            while(true){
                /**
                 * Sends packet containing Client ID and Model every 150ms
                 */
                packet.setID(this.clientID);
                oos.writeObject(packet);
                System.out.println("Sending gamestate packet..");
                oos.flush();
                oos.reset();
                Thread.sleep(150);
            }      
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}