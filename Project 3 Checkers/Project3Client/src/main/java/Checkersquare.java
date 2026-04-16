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
        this.row = row;
        this.col = col;
        this.piece = piece;

        setPrefSize(80, 80);
        setFont(Font.font(32));
        if((row + col) % 2 == 1) {
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
    }

    private void updateVisuals() {
        if(piece == Piece.EMPTY){
            return;
        }
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
        setFont(Font.font(30));
    }
}
