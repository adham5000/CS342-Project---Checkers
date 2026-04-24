import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import javafx.event.ActionEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.*;

public class CheckersAPI {
    @FXML public ToggleGroup gameType;
    @FXML private GridPane board;
    @FXML private ListView<String> chatList;
    @FXML private ListView<String> listUsers;
    @FXML private ListView<String> listMoves;
    @FXML private ListView<String> listFriends;

    @FXML private TextField friendField;
    @FXML private TextField passfield;
    @FXML private TextField userfield;
    @FXML private Button resignBtn;
    @FXML private Button startGameBtn;
    @FXML private Button drawBtn;
    @FXML private Button rematchBtn;
    @FXML private Button quitBtn;
    @FXML private Label player1Name;
    @FXML private Label player2Name;
    @FXML private Label player1WinDrawLoss;
    @FXML private Label player2WinDrawLoss;
    @FXML private Label erroruser;
    @FXML private Label rematchRequest;
    @FXML private Label winMessage;
    @FXML private TextField sendField;
    @FXML private StackPane winPopup;
    private Stage stage;

    private Client client;
    private Checkersquare[][] squares = new Checkersquare[8][8];
    private String myName;
    private String opponentName;
    private Checkersquare selectedSquare = null;
    private String myColor = "";
    private Message.MyWinDrawLoss winDrawLoss;
    private Message.MyWinDrawLoss opponentWinDrawLoss;
    private HashSet<String> friends = new HashSet<>();

    public void setClient(Client client) {
        this.client = client;
    }
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    @FXML
    public void initialize() {}


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
        drawBtn.setDisable(true);
        resignBtn.setDisable(true);
        player1Name.setText(myName);
    }

    public void handleServerMessage(Object data) {
        Message msg = (Message) data;
        Platform.runLater(() -> {
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

                    client.setController(controller);

                    controller.myName = this.myName;
                    controller.setClient(client);
                    controller.setStage(stage);

                    controller.buildBoard();

                    stage.setScene(new Scene(root));
                    Message message = new Message(Message.messageType.ACK);
                    client.send(message);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        else if (msg.msgType() == Message.messageType.USERLOG) {
            listUsers.getItems().setAll(msg.getActiveUsers());
        }
        else if(msg.msgType() == Message.messageType.GAME_START){
            startGameBtn.setDisable(true);
            drawBtn.setDisable(false);
            resignBtn.setDisable(false);
            if(Objects.equals(msg.returnMessage(), "RED")){
                myColor = "RED";
                player2Name.setText(msg.getActiveUsers().get(1));
                opponentName = msg.getActiveUsers().get(1);
                for(int r = 0; r < 8;++r){
                    for(int c = 0; c < 8;++c){
                        if((r + c) % 2 == 0){
                            int n = squares[r][c].toNotationNumber(r,c);
                            squares[r][c].setNumber(n);
                        }
                        if((r == 0 && c % 2 == 0) || (r == 1 && c % 2 == 1) || (r == 2 && c % 2 == 0)){
                            squares[r][c].setPiece(Checkersquare.Piece.RED);
                        }
                        else if ((r == 6 && c % 2 == 0) || (r == 7 && c % 2 == 1) || (r == 5 && c % 2 == 1)) {
                            squares[r][c].setPiece(Checkersquare.Piece.WHITE);
                        }
                        else{
                            squares[r][c].setPiece(Checkersquare.Piece.EMPTY);
                        }
                    }
                }
            }
            else{
                myColor = "WHITE";
                player2Name.setText(msg.getActiveUsers().get(0));
                opponentName = msg.getActiveUsers().get(0);
                for(int r = 0; r < 8;++r){
                    for(int c = 0; c < 8;++c){
                        if((r + c) % 2 == 0){
                            int n = squares[r][c].toNotationNumber(r,c);
                            n = 33 - n;
                            squares[r][c].setNumber(n);
                        }
                        if((r == 0 && c % 2 == 0) || (r == 1 && c % 2 == 1) || (r == 2 && c % 2 == 0)){
                            squares[r][c].setPiece(Checkersquare.Piece.WHITE);
                        }
                        else if ((r == 6 && c % 2 == 0) || (r == 7 && c % 2 == 1) || (r == 5 && c % 2 == 1)) {
                            squares[r][c].setPiece(Checkersquare.Piece.RED);
                        }
                        else{
                            squares[r][c].setPiece(Checkersquare.Piece.EMPTY);
                        }
                    }
                }
            }
        }
        else if(msg.msgType() == Message.messageType.CHECKERMOVE){
            if(msg.returnMessage() != null){
                listMoves.getItems().add(msg.returnMessage());
            }
            else {
                int fromRow;
                int fromCol;
                int toRow;
                int toCol;
                int kingRow = -1;
                int kingCol = -1;
                int capturedRow = -1;
                int capturedCol = -1;
                if (myColor.equals("RED")) {
                    fromRow = msg.getFromRow();
                    fromCol = msg.getFromCol();
                    toRow = msg.getToRow();
                    toCol = msg.getToCol();
                    if (msg.getKingCol() != -1) {
                        kingRow = msg.getKingRow();
                        kingCol = msg.getKingCol();
                    }
                    if (msg.getCapturedCol() != -1) {
                        capturedRow = msg.getCapturedRow();
                        capturedCol = msg.getCapturedCol();
                    }
                } else {
                    fromRow = 7 - msg.getFromRow();
                    fromCol = 7 - msg.getFromCol();
                    toRow = 7 - msg.getToRow();
                    toCol = 7 - msg.getToCol();
                    if (msg.getKingCol() != -1) {
                        kingRow = 7 - msg.getKingRow();
                        kingCol = 7 - msg.getKingCol();
                    }
                    if (msg.getCapturedCol() != -1) {
                        capturedRow = 7 - msg.getCapturedRow();
                        capturedCol = 7 - msg.getCapturedCol();
                    }
                }
                Checkersquare.Piece piece = squares[fromRow][fromCol].getPiece();
                squares[fromRow][fromCol].setPiece(Checkersquare.Piece.EMPTY);
                squares[toRow][toCol].setPiece(piece);

                if (capturedCol != -1) {
                    listMoves.getItems().add(squares[fromRow][fromCol].getSquareNumber() + "x" + squares[toRow][toCol].getSquareNumber());
                    squares[capturedRow][capturedCol].setPiece(Checkersquare.Piece.EMPTY);
                }
                else{
                    listMoves.getItems().add(squares[fromRow][fromCol].getSquareNumber() + "-" + squares[toRow][toCol].getSquareNumber());
                }
                if (kingCol != -1) {
                    if (squares[kingRow][kingCol].getPiece() == Checkersquare.Piece.WHITE) {
                        squares[kingRow][kingCol].setPiece(Checkersquare.Piece.WHITE_KING);
                    } else {
                        squares[kingRow][kingCol].setPiece(Checkersquare.Piece.RED_KING);
                    }
                }
            }
        }
        else if (msg.msgType() == Message.messageType.GAME_OVER){
            myColor = "";
            winPopup.setVisible(true);
            winMessage.setText(msg.returnMessage());
            rematchBtn.setDisable(false);
            startGameBtn.setDisable(false);
            resignBtn.setDisable(true);
            drawBtn.setDisable(true);
            rematchRequest.setText("");
        }
        else if (msg.msgType() == Message.messageType.SCORES){
            if (player1WinDrawLoss == null || player2WinDrawLoss == null) {
                return;
            }
            if(!Objects.equals(msg.returnMessage(), "OPPONENT")) {
                winDrawLoss = msg.getWinDrawLoss();
                String temp = myName + " Wins: " + winDrawLoss.wins + " Draws: " + winDrawLoss.draws + " Loss: " + winDrawLoss.losses;
                player1WinDrawLoss.setText(temp);
            }
            else{
                opponentWinDrawLoss = msg.getWinDrawLoss();
                String temp = opponentName + " Wins: " + opponentWinDrawLoss.wins + " Draws: " + opponentWinDrawLoss.draws + " Loss: " + opponentWinDrawLoss.losses;
                player2WinDrawLoss.setText(temp);
            }
            System.out.println(msg.getWinDrawLoss().wins + " " + msg.getWinDrawLoss().draws + " " + msg.getWinDrawLoss().losses);
        }
        else if (msg.msgType() == Message.messageType.FRIENDS){
            System.out.println(msg.getFriends());
            listFriends.getItems().clear();
            HashSet<String> friends = msg.getFriends();
            listFriends.getItems().addAll(friends);
        }
        else if(msg.msgType() == Message.messageType.QUIT){
            rematchBtn.setDisable(true);
        }
        else if(msg.msgType() == Message.messageType.REMATCH){
            if(msg.returnMessage() == null){
                winPopup.setVisible(false);
            }
            else{
                rematchRequest.setText(msg.returnMessage());
            }
        }
        else {
            chatList.getItems().add(msg.returnMessage());
        }
        });
    }
    @FXML
    private void sendButtonHandler(ActionEvent event) {
        if(!sendField.getText().isEmpty() && !Objects.equals(myColor, "")){
            Message message = new Message(sendField.getText(), Message.messageType.GLOBAL);
            client.send(message);
            chatList.getItems().add("You said: " + sendField.getText());
            sendField.clear();
        }
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
        ArrayList<String> namepass =  new ArrayList<>();
        namepass.add(userfield.getText());
        namepass.add(passfield.getText());

        Message message = new Message(namepass, Message.messageType.USERNAME);
        client.send(message);
       // userfield.clear();
       // passfield.clear();
    }
    @FXML void handleRandomGame(ActionEvent event) {
        friends.add("as");
        Message message = new Message(Message.messageType.GAME_START);
        client.send(message);
    }

    @FXML void drawBtnHandler(ActionEvent event) {
        Message msg = new Message("DRAW", Message.messageType.GAME_OVER);
        drawBtn.setDisable(true);
        client.send(msg);
    }
    public void setFriends(HashSet<String> newFriends) {
        friends.clear();
        friends.addAll(newFriends);

        if (listUsers.getCellFactory() == null) {
            listUsers.setCellFactory(lv -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                        return;
                    }

                    setText(item);

                    if (friends.contains(item)) {
                        setStyle("-fx-background-color: lightgreen; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            });
        }


        listUsers.layout();
    }


    @FXML
    public void friendBtnHandler(ActionEvent event) {
        Message msg;
        String friend = friendField.getText();
        if(listUsers.getItems().contains(friend) && !friends.contains(friend)){
            msg = new Message(friend, Message.messageType.FRIEND_REQUEST);
            client.send(msg);
        }
    }
    @FXML
    public void resignBtnHandler(ActionEvent event) {
        Message msg = new Message(Message.messageType.RESIGN);
        client.send(msg);
    }
    @FXML void rematchBtnHandler(ActionEvent event) {
        Message msg = new Message(player2Name.getText(),Message.messageType.REMATCH);
        rematchRequest.setText("YOU REQUESTED A REMATCH");
        rematchBtn.setDisable(true);
        client.send(msg);
    }
    @FXML void quitBtnHandler(ActionEvent event) {
        winPopup.setVisible(false);
        Message msg = new Message(player2Name.getText(),Message.messageType.QUIT);
        client.send(msg);
    }
}
