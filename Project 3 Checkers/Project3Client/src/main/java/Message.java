import java.io.Serializable;
import java.util.ArrayList;

public class Message implements Serializable {
    public enum messageType {
        USERNAME, // For setting username
        GLOBAL, // For chat log
        USERLOG, // For showing usernames
        GROUP, // For creating groups
    }

    private String stringMessage;
    private messageType type;
    private ArrayList<String> Users;

    public Message(String stringMessage) {
        this.stringMessage = stringMessage;
        this.type = messageType.GLOBAL;
    }

    public Message(String stringMessage, messageType type) {
        this.stringMessage = stringMessage;
        this.type = type;
    }

    public Message(String stringMessage, messageType type, ArrayList<String> Users) {
        this.stringMessage = stringMessage;
        this.type = type;
        this.Users = Users;
    }

    public Message(ArrayList<String> Users, messageType type) {
        this.Users = Users;
        this.type = type;
    }

    public String returnMessage() {return stringMessage;}
    public messageType msgType() {return type;}
    public ArrayList<String> getActiveUsers() { return Users; }

    static final long serialVersionUID = 42L;

}
