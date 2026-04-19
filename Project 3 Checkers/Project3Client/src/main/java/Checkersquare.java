import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Checkersquare extends Button {
    enum Piece {
        RED, WHITE, RED_KING, WHITE_KING, EMPTY
    }
    private final int row;
    private final int col;
    private Piece piece;

    Checkersquare(int row, int col, Piece piece) {
        setPrefSize(100, 100);       // preferred size
        setMinSize(0, 0);            // allow shrinking
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE); // allow growing

        this.row = row;
        this.col = col;
        this.piece = piece;


        setFont(Font.font(32));
        if((row + col) % 2 == 0) {
            setStyle("-fx-background-color: BLACK;");
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
            setText("");
            setCursor(Cursor.DEFAULT);
            return;
        }
        setCursor(Cursor.HAND);
        if (piece == Piece.RED ||  piece == Piece.WHITE) {
            setText("⛀");
        }
        else{
            setText("♕");
        }
        if(piece == Piece.RED ||  piece == Piece.RED_KING) {
            setTextFill(Color.RED);
        }
        else{
            setTextFill(Color.WHITE);
        }
        setFont(Font.font(15));
    }
}
