import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Message implements Serializable {
    public enum messageType {
        USERNAME, // For setting username
        GLOBAL, // For chat log
        USERLOG, // For showing usernames
        GROUP,// For creating groups
        CHECKERMOVE,// For moves
        GAME_START,
        GAME_OVER,
        SCORES,
        FRIENDS,
        FRIEND_REQUEST,
        RESIGN,
        ACK,
        QUIT,
        REMATCH,
        REGISTER,
    }

    public static class MyWinDrawLoss implements Serializable {
        private static final long serialVersionUID = 1L;
        public int wins;
        public int draws;
        public int losses;

        public MyWinDrawLoss(int wins, int draws, int losses) {
            this.wins = wins;
            this.draws = draws;
            this.losses = losses;
        }
    }

    private String stringMessage;
    private messageType type;
    private ArrayList<String> Users;
    private int fromRow;
    private int fromCol;
    private int toRow;
    private int toCol;
    private int capturedRow;
    private int capturedCol;
    private int kingRow;
    private int kingCol;
    private MyWinDrawLoss winDrawLoss;
    private HashSet<String> friends;

    public Message(String stringMessage) {
        this.stringMessage = stringMessage;
        this.type = messageType.GLOBAL;
    }
    public Message(HashSet<String> friends, messageType type) {
        this.friends = friends;
        this.type = type;
    }
    public Message(String stringMessage, messageType type) {
        this.stringMessage = stringMessage;
        this.type = type;
    }

    public Message(MyWinDrawLoss scores, messageType type) {
        this.winDrawLoss = scores;
        this.type = type;
    }
    public Message(String stringMessage, MyWinDrawLoss scores, messageType type) {
        this.stringMessage = stringMessage;
        this.winDrawLoss = scores;
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

    public Message(int fromRow,int fromCol, int toRow, int toCol,messageType type){
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.capturedRow = -1;
        this.capturedCol = -1;
        this.kingRow = -1;
        this.kingCol = -1;
        this.type = type;
    }

    public Message(int fromRow,int fromCol, int toRow, int toCol,int capturedRow,int capturedCol,int kingRow,int kingCol,messageType type){
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.capturedRow = capturedRow;
        this.capturedCol = capturedCol;
        this.kingRow = kingRow;
        this.kingCol = kingCol;
        this.type = type;
    }
    public Message(messageType type) {
        this.type = type;
    }

    public HashSet<String> getFriends() {return friends;}
    public int getFromRow() {
        return fromRow;
    }
    public int getFromCol() {
        return fromCol;
    }
    public int getToRow() {
        return toRow;
    }
    public int getToCol() {
        return toCol;
    }
    public int getKingRow() {
        return kingRow;
    }
    public int getKingCol() {
        return kingCol;
    }
    public int getCapturedRow() {
        return capturedRow;
    }
    public int getCapturedCol() {
        return capturedCol;
    }
    public String returnMessage() {return stringMessage;}
    public messageType msgType() {return type;}
    public ArrayList<String> getActiveUsers() { return Users; }
    public MyWinDrawLoss getWinDrawLoss() {return winDrawLoss;}

    static final long serialVersionUID = 42L;

}
