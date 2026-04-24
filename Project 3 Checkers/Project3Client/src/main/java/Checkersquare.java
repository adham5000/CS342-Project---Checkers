import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Checkersquare extends Button {
    enum Piece {
        RED, WHITE, RED_KING, WHITE_KING, EMPTY
    }
    private final Label pieceLabel = new Label();
    private final Label numberLabel = new Label();
    private final int row;
    private final int col;
    private Piece piece;

    Checkersquare(int row, int col, Piece piece) {
        setPrefSize(100, 100);
        setMinSize(0, 0);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        this.row = row;
        this.col = col;
        this.piece = piece;
        StackPane content = new StackPane();
        numberLabel.setStyle("-fx-font-size: 12; -fx-text-fill: white;");
        StackPane.setAlignment(numberLabel, Pos.TOP_LEFT);
        numberLabel.setTranslateX(4);
        numberLabel.setTranslateY(2);

        setPadding(Insets.EMPTY);
        // Piece label (center)
        pieceLabel.setStyle("-fx-font-size: 32;");
        StackPane.setAlignment(pieceLabel, Pos.CENTER);

        content.getChildren().addAll(numberLabel, pieceLabel);
        content.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        // Put the content inside the button
        setGraphic(content);

        setFont(Font.font(32));
        if((row + col) % 2 == 0) {
            setStyle("-fx-background-color: BLACK;");
            numberLabel.setText(String.valueOf(toNotationNumber(row,col)));
        }
        else{
            setStyle("-fx-background-color: WHITE;");
        }
        updateVisuals();
    }

    public int getRow() {
        return row;
    }
    public int getCol() {
        return col;
    }

    public Piece getPiece() {
        return piece;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
        updateVisuals();
    }

    private void updateVisuals() {
        if(piece == Piece.EMPTY){
            pieceLabel.setText("");
            setCursor(Cursor.DEFAULT);
            return;
        }
        setCursor(Cursor.HAND);
        if (piece == Piece.RED ||  piece == Piece.WHITE) {
            pieceLabel.setText("⛀");
        }
        else{
            pieceLabel.setText("♕");
        }
        if(piece == Piece.RED ||  piece == Piece.RED_KING) {
            pieceLabel.setTextFill(Color.RED);
        }
        else{
            pieceLabel.setTextFill(Color.WHITE);
        }
        setFont(Font.font(15));
    }
    public int toNotationNumber(int row, int col) {
        if ((row + col) % 2 != 0) {
            return -1; // not a dark square
        }

        int index = (row % 2 == 1 ? (col - 1) / 2 : col / 2);
        return row * 4 + (3 - index) + 1;
    }
    public void setNumber(int num){
        numberLabel.setText(String.valueOf(num));
    }
    public String getSquareNumber(){
        return numberLabel.getText();
    }
}
