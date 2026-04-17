import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import javafx.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class CheckersAPI {
    @FXML private GridPane board;
    @FXML private ListView<String> chatList;
    @FXML private ListView<String> listUsers;
    @FXML private TextField chatInput;
    @FXML private TextField nameField;
    @FXML private Button setUserName;
    @FXML private Button startGameBtn;
    @FXML private Button rematchBtn;
    @FXML private Button sendBtn;
    @FXML private Label player1Name;

    private Client client;
    private Checkersquare[][] squares = new Checkersquare[8][8];
    private String myName;
    private Checkersquare selectedSquare = null;
    private String myColor = "RED";

    public void setClient(Client client) {
        this.client = client;
    }

    @FXML
    public void initialize() {
        sendBtn.setDisable(true);
        nameField.setDisable(true);
        rematchBtn.setDisable(true);
        buildBoard();
    }

    private void buildBoard() {
        for(int r = 0; r < 8;++r){
            for(int c = 0; c < 8;++c){
                Checkersquare square;
                if((r == 0 && c % 2 == 1) || (r == 1 && c % 2 == 0) || (r == 2 && c % 2 == 1)){
                    square = new Checkersquare(r,c, Checkersquare.Piece.WHITE);
                }
                else if ((r == 6 && c % 2 == 1) || (r == 7 && c % 2 == 0) || (r == 5 && c % 2 == 0)) {
                    square = new Checkersquare(r,c, Checkersquare.Piece.RED);
                }
                else {
                    square = new Checkersquare(r, c, Checkersquare.Piece.EMPTY);
                }
                square.setPrefSize(80,80);
                square.getStyleClass().add("square");
                square.setOnAction(event -> handleSquareClick(square));
                board.add(square,c,r);
                squares[r][c] = square;
            }
        }
    }

    public void handleServerMessage(Object data) {
        Message msg = (Message) data;
        if (msg.msgType() == Message.messageType.USERNAME) {
            setUserName.setDisable(false);
            setUserName.setDisable(true);
            nameField.setDisable(true);
            chatList.getItems().add(msg.returnMessage());
        }
        else if (msg.msgType() == Message.messageType.USERLOG) {
            listUsers.getItems().setAll(msg.getActiveUsers());
        }
        else if(msg.msgType() == Message.messageType.GAME_START){
            startGameBtn.setDisable(true);
            if(Objects.equals(msg.returnMessage(), "RED")){
                myColor = "RED";
                return;
            }
            else{
                myColor = "WHITE";
                for(int r = 0; r < 8;++r){
                    for(int c = 0; c < 8;++c){
                        if((r == 0 && c % 2 == 1) || (r == 1 && c % 2 == 0) || (r == 2 && c % 2 == 1)){
                            squares[r][c].setPiece(Checkersquare.Piece.RED);
                        }
                        else if ((r == 6 && c % 2 == 1) || (r == 7 && c % 2 == 0) || (r == 5 && c % 2 == 0)) {
                            squares[r][c].setPiece(Checkersquare.Piece.WHITE);
                        }
                    }
                }
            }
        }
        else if(msg.msgType() == Message.messageType.CHECKERMOVE){
            Checkersquare.Piece piece = squares[msg.getFromRow()][msg.getFromCol()].getPiece();
            squares[msg.getFromRow()][msg.getFromCol()].setPiece(Checkersquare.Piece.EMPTY);
            squares[msg.getToRow()][msg.getToCol()].setPiece(piece);

        }
        else {
            chatList.getItems().add(msg.returnMessage());
        }

    }
    @FXML
    private void sendButtonHandler(ActionEvent event) {
        if (nameField.getText().isEmpty()) {
            Message message = new Message(chatInput.getText(), Message.messageType.GLOBAL);
            client.send(message);
            chatInput.clear();
        }
        else {
            String[] items = nameField.getText().split(",");
            ArrayList<String> temp = new ArrayList<>(Arrays.asList(items));
            temp.add(myName);
            Message message = new Message(chatInput.getText(), Message.messageType.GROUP, temp);
            client.send(message);
            chatInput.clear();
        }
    }
    @FXML
    private void setNameButtonHandler(ActionEvent event) {
        Message message = new Message(chatInput.getText(), Message.messageType.USERNAME);
        client.send(message);
        setUserName.setDisable(true);
        sendBtn.setDisable(false);
        nameField.setDisable(false);
        myName = chatInput.getText();
        player1Name.setText(myName);
        chatInput.clear();
    }
    @FXML
    private void handleSquareClick(Checkersquare square) {
        if(selectedSquare == null){
            if(square.getPiece() == Checkersquare.Piece.EMPTY){
                return;
            }
            selectedSquare = square;
            square.getStyleClass().add("selected-square");
            return;
        }

        Checkersquare from =  selectedSquare;
        Checkersquare to =  square;

        from.getStyleClass().remove("selected-square");
        selectedSquare = null;

        Message message = new Message(from.getRow(),from.getCol(),to.getRow(),to.getCol(),Message.messageType.CHECKERMOVE);
        client.send(message);
    }

    @FXML
    private void handleStartGame(ActionEvent event) {
        Message message = new Message(Message.messageType.GAME_START);
        client.send(message);
    }

}
