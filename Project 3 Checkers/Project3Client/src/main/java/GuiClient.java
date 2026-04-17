
import java.util.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class GuiClient extends Application{


	Client clientConnection;

	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/scene1.fxml"));
		Parent root = loader.load();

		CheckersAPI controller = loader.getController();
		clientConnection = new Client(data->{
				Platform.runLater(()->{controller.handleServerMessage(data);});
		});
							
		clientConnection.start();
		controller.setClient(clientConnection);

		Scene scene = new Scene(root);
		scene.getStylesheets().add(
				getClass().getResource("/STYLES/scene1.css").toExternalForm()
		);

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

		primaryStage.setScene(scene);
		primaryStage.setTitle("Checkers Client");
		primaryStage.show();
		
	}
	



}
