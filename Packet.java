import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Packet implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private int clientID;
    private Model model;
    private int statusCode;
    final Lock packetLock = new ReentrantLock();
    /**
     * Contructor that stakes a status code (int) to identify the state
     * @param statusCode
     */
    public Packet(int statusCode) {
        this.statusCode = statusCode;
    }
    /**
     * Getter for statuscode
     * @return int
     */
    public int getStatusCode() {
        return statusCode;
    }
    /**
     * Setter for status code, takes an int that represents state/request
     * @param statusCode
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    public int getID() {
        return this.clientID;
    }
    public void setID(int id) {
        this.clientID = id;
    }
    public void setModel(Model model) {
        this.model = model;
    }
    public Model getModel() {
        return this.model;
    }
    public void lock() {
        packetLock.lock();
    }
    public void unlock() {
        packetLock.unlock();
    }
}