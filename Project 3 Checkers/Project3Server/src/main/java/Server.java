import java.awt.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.scene.control.ListView;

public class Server {

	int count = 1;
	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	TheServer server;
	private Consumer<Serializable> callback;
	HashMap<Integer, String> userNames = new HashMap<>();

	Server(Consumer<Serializable> call){

		callback = call;
		server = new TheServer();
		server.start();
	}

	public class TheServer extends Thread{

		public void run() {

			try (ServerSocket mysocket = new ServerSocket(5555);) {
			System.out.println("Server is waiting for a client!");

			while(true) {
				ClientThread c = new ClientThread(mysocket.accept(), count);
				Message msg = new Message("client has connected to server: client #" + count);
				callback.accept(msg);
				clients.add(c);
				c.start();

				count++;

				} //end of while
			} //end of try
			catch(Exception e) {
				Message msg = new Message("Server socket did not launch");
				callback.accept(msg);
			}
		}
	}

	class ClientThread extends Thread{
		Socket connection;
		int count;
		ObjectInputStream in;
		ObjectOutputStream out;
		Message msg;

		ClientThread(Socket s, int count){
			this.connection = s;
			this.count = count;
		}

		public void updateClients(Message message) {
			if (message.msgType() == Message.messageType.GLOBAL ||  message.msgType() == Message.messageType.USERLOG) {
				for (int i = 0; i < clients.size(); i++) {
					ClientThread t = clients.get(i);
					try {
						t.out.writeObject(message);
					}
					catch(Exception e) {}
				}
			}
			else {
				ArrayList<String> recipients = message.getActiveUsers();
				for (int i = 0; i < clients.size(); i++) {
					ClientThread t = clients.get(i);
					int id = t.count;
					String username = userNames.get(id);
					if (recipients.contains(username)) {
						try {
							t.out.writeObject(message);
						}
						catch(Exception e) {}
					}
				}
			}
		}

		public void run(){

			try {
				in = new ObjectInputStream(connection.getInputStream());
				out = new ObjectOutputStream(connection.getOutputStream());
				connection.setTcpNoDelay(true);
			}
			catch(Exception e) {
				System.out.println("Streams not open");
			}

			msg = new Message("new client on server: client #" + count);
			updateClients(msg);
			callback.accept(msg);

			 while(true) {
				try {
					Message data = (Message) in.readObject(); // This reads txt from client

					if (data.msgType() == Message.messageType.USERNAME) {
						if (!userNames.containsValue(data.returnMessage())) {
							userNames.put(count, data.returnMessage());

							msg = new Message("client: " + count + " name set: " + data.returnMessage());
							updateClients(msg);
							callback.accept(msg);

							ArrayList<String> tempList = new ArrayList<>(userNames.values());
							msg = new Message(tempList, Message.messageType.USERLOG);
							updateClients(msg);
							callback.accept(msg);
						}
						else {
							msg = new Message("Username is already taken.", Message.messageType.USERNAME);
							out.writeObject(msg);
						}
					}
					else if(data.msgType() == Message.messageType.GROUP){
						msg = new Message("client: " + count + " user: " + userNames.get(count) + ": " + data.returnMessage() +
								" sent to: " + data.getActiveUsers());
						callback.accept(msg);

						msg = new Message("client: " + count + " user: " + userNames.get(count) + ": " + data.returnMessage()
								, Message.messageType.GROUP, data.getActiveUsers());
						updateClients(msg);
					}
					else {
						msg = new Message("client: " + count + " user: " + userNames.get(count) + ": " + data.returnMessage());
						updateClients(msg);
						callback.accept(msg);
					}
				}
				catch(Exception e) {
					msg = new Message("OOOOPPs...Something wrong with the socket from client: " + count + "....closing down!");
					callback.accept(msg);

					msg = new Message("Client #" + count + " has left the server!");
					updateClients(msg);

					userNames.remove(count);
					clients.remove(this);

					ArrayList<String> tempList = new ArrayList<>(userNames.values());
					msg = new Message(tempList, Message.messageType.USERLOG);
					callback.accept(msg);
					updateClients(msg);
					break;
				}
			 }
		}//end of run

	}//end of client thread
}