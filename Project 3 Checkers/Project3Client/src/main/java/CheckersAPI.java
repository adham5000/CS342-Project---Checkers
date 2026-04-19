import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class CheckersAPI {
    @FXML public ToggleGroup gameType;
    @FXML private GridPane board;
    @FXML private ListView<String> chatList;
    @FXML private ListView<String> listUsers;
    @FXML private TextField chatInput;
    @FXML private TextField nameField;
    @FXML private TextField userfield;
    @FXML private Button setUserName;
    @FXML private Button startGameBtn;
    @FXML private Button rematchBtn;
    @FXML private Button sendBtn;
    @FXML private Label player1Name;
    @FXML private Label erroruser;

    private Stage stage;

    private Client client;
    private Checkersquare[][] squares = new Checkersquare[8][8];
    private String myName;
    private Checkersquare selectedSquare = null;
    private String myColor = "";

    public void setClient(Client client) {
        this.client = client;
    }
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    @FXML
    public void initialize() {
//        sendBtn.setDisable(true);
//        nameField.setDisable(true);
//        rematchBtn.setDisable(true);
//        buildBoard();
    }

    private void buildBoard() {

        for(int r = 0; r < 8;++r){
            for(int c = 0; c < 8;++c){
                Checkersquare square;
                if((r == 0 && c % 2 == 0) || (r == 1 && c % 2 == 1) || (r == 2 && c % 2 == 0)){
                    square = new Checkersquare(r,c, Checkersquare.Piece.RED);
                }
                else if ((r == 6 && c % 2 == 0) || (r == 7 && c % 2 == 1) || (r == 5 && c % 2 == 1)) {
                    square = new Checkersquare(r,c, Checkersquare.Piece.WHITE);
                }
                else {
                    square = new Checkersquare(r, c, Checkersquare.Piece.EMPTY);
                }
                square.setPrefSize(80,80);
                square.getStyleClass().add("square");
                square.setOnAction(event -> handleSquareClick(square));
                board.add(square, c,7-r);
                squares[r][c] = square;
            }
        }
    }

    public void handleServerMessage(Object data) {
        Message msg = (Message) data;
        if (msg.msgType() == Message.messageType.USERNAME) {
            if(Objects.equals(msg.returnMessage(), "")){
                erroruser.setVisible(true);
            }
            else{
                myName = msg.returnMessage();
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/checkersMulti.fxml"));
                    Parent root = loader.load();

                    CheckersAPI controller = loader.getController();

                    controller.setClient(client);
                    controller.setStage(stage);


                    client.setController(controller);
                    controller.buildBoard();


                    stage.setScene(new Scene(root));
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
//            setUserName.setDisable(false);
//            setUserName.setDisable(true);
//            nameField.setDisable(true);
//            chatList.getItems().add(msg.returnMessage());
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
                        if((r == 0 && c % 2 == 0) || (r == 1 && c % 2 == 1) || (r == 2 && c % 2 == 0)){
                            squares[r][c].setPiece(Checkersquare.Piece.WHITE);
                        }
                        else if ((r == 6 && c % 2 == 0) || (r == 7 && c % 2 == 1) || (r == 5 && c % 2 == 1)) {
                            squares[r][c].setPiece(Checkersquare.Piece.RED);
                        }
                    }
                }
            }
        }
        else if(msg.msgType() == Message.messageType.CHECKERMOVE){
            int fromRow;
            int fromCol;
            int toRow;
            int toCol;
            int kingRow = -1;
            int kingCol = -1;
            int capturedRow = -1;
            int capturedCol = -1;
            if(myColor.equals("RED")){
                fromRow = msg.getFromRow();
                fromCol = msg.getFromCol();
                toRow = msg.getToRow();
                toCol = msg.getToCol();
                if(msg.getKingCol() != -1) {
                    kingRow = msg.getKingRow();
                    kingCol = msg.getKingCol();
                }
                if(msg.getCapturedCol() != -1) {
                    capturedRow = msg.getCapturedRow();
                    capturedCol = msg.getCapturedCol();
                }
            }
            else{
                fromRow = 7 - msg.getFromRow();
                fromCol = 7 - msg.getFromCol();
                toRow = 7 - msg.getToRow();
                toCol = 7 - msg.getToCol();
                if(msg.getKingCol() != -1) {
                    kingRow = 7 - msg.getKingRow();
                    kingCol = 7 - msg.getKingCol();
                }
                if(msg.getCapturedCol() != -1) {
                    capturedRow = 7 - msg.getCapturedRow();
                    capturedCol = 7 - msg.getCapturedCol();
                }
            }
            Checkersquare.Piece piece = squares[fromRow][fromCol].getPiece();
            squares[fromRow][fromCol].setPiece(Checkersquare.Piece.EMPTY);
            squares[toRow][toCol].setPiece(piece);

            if(capturedCol != -1){
                squares[capturedRow][capturedCol].setPiece(Checkersquare.Piece.EMPTY);
            }
            if(kingCol != -1){
                if(squares[kingRow][kingCol].getPiece() == Checkersquare.Piece.WHITE){
                    squares[kingRow][kingCol].setPiece(Checkersquare.Piece.WHITE_KING);
                }
                else{
                    squares[kingRow][kingCol].setPiece(Checkersquare.Piece.RED_KING);
                }
            }

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
        Message message;

        from.getStyleClass().remove("selected-square");
        selectedSquare = null;
        if(myColor != "") {
            if (myColor.equals("RED")) {
                message = new Message(from.getRow(), from.getCol(), to.getRow(), to.getCol(), Message.messageType.CHECKERMOVE);
            } else {
                message = new Message(7 - from.getRow(), 7 - from.getCol(), 7 - to.getRow(), 7 - to.getCol(), Message.messageType.CHECKERMOVE);
            }
            client.send(message);
        }
    }

    @FXML
    private void handleStartGame(ActionEvent event) {
        Message message = new Message(userfield.getText(), Message.messageType.USERNAME);
        client.send(message);
        userfield.clear();
        if(myName != null) {

            player1Name.setText(myName);
            message = new Message(Message.messageType.GAME_START);
            client.send(message);
        }
    }

}
