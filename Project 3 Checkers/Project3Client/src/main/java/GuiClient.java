
import java.util.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
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

	
	TextField c1, nameField;
	Button b1, setUserName;
	HashMap<String, Scene> sceneMap;
	VBox clientBox;
	Client clientConnection;
	
	ListView<String> listItems2;
	ListView<String> listUsers;
	String myName;
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		clientConnection = new Client(data->{
				Platform.runLater(()->{
					Message msg = (Message) data;
					if (msg.msgType() == Message.messageType.USERNAME) {
						setUserName.setDisable(false);
						b1.setDisable(true);
						nameField.setDisable(true);
						listItems2.getItems().add(msg.returnMessage());
					}
					else if (msg.msgType() == Message.messageType.USERLOG) {
						listUsers.getItems().setAll(msg.getActiveUsers());
					}
					else {
						listItems2.getItems().add(msg.returnMessage());
					}

			});
		});
							
		clientConnection.start();

		listItems2 = new ListView<>();
		listUsers = new ListView<>();
		
		c1 = new TextField();
		nameField = new TextField();
		nameField.setDisable(true);
		nameField.setPromptText("Enter Empty(Global)/Individual/Group Ex: ' ' or A or A,B,C");
		b1 = new Button("Send");
		b1.setDisable(true);
		setUserName = new Button("Apply Name");
		b1.setOnAction(e->{
			if (nameField.getText().isEmpty()) {
				Message message = new Message(c1.getText(), Message.messageType.GLOBAL);
				clientConnection.send(message);
				c1.clear();
			}
			else {
				String[] items = nameField.getText().split(",");
				ArrayList<String> temp = new ArrayList<>(Arrays.asList(items));
				temp.add(myName);
				Message message = new Message(c1.getText(), Message.messageType.GROUP, temp);
				clientConnection.send(message);
				c1.clear();
			}
		});

		setUserName.setOnAction(e->{
			Message message = new Message(c1.getText(), Message.messageType.USERNAME);
			clientConnection.send(message);
			setUserName.setDisable(true);
			b1.setDisable(false);
			nameField.setDisable(false);
			myName = c1.getText();
			c1.clear();
		});
		
		sceneMap = new HashMap<String, Scene>();

		sceneMap.put("client", createClientGui());
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

		primaryStage.setScene(sceneMap.get("client"));
		primaryStage.setTitle("Client");
		primaryStage.show();
		
	}
	

	
	public Scene createClientGui() {

		HBox buttons = new HBox(setUserName, b1);
		HBox listViews = new HBox(listItems2, listUsers);
		clientBox = new VBox(10, c1, nameField, buttons, listViews);
		clientBox.setStyle("-fx-background-color: blue;"+"-fx-font-family: 'serif';");
		return new Scene(clientBox, 500, 400);
		
	}

}
