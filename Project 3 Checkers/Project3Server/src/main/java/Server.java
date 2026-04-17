import java.awt.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.scene.control.ListView;

import java.util.Queue;
import java.util.LinkedList;

public class Server {

	int count = 1;
	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	TheServer server;
	private Consumer<Serializable> callback;
	HashMap<Integer, String> userNames = new HashMap<>();

	private final Queue<ClientThread> waitList = new LinkedList<>();
	private final ArrayList<GameSession> activeGames = new ArrayList<>();

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
		private GameSession game;
		private int playerColor;

		ClientThread(Socket s, int count){
			this.connection = s;
			this.count = count;
		}
		public void setSession(GameSession game){
			this.game = game;
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
					else if(data.msgType() == Message.messageType.GAME_START){
						addToQueue(this);
					}
					else if(data.msgType() == Message.messageType.CHECKERMOVE && game!=null && game.getTurn() == playerColor){
						//boolean validMove = evaluate()
						msg = new Message(data.getFromRow(),data.getFromCol(),data.getToRow(),data.getToCol(), Message.messageType.CHECKERMOVE);
						game.getOpponent(this).out.writeObject(msg);
						out.writeObject(msg);
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
	public synchronized void addToQueue(ClientThread player) {
		waitList.add(player);

		if (waitList.size() >= 2) {
			ClientThread p1 = waitList.poll();
			ClientThread p2 = waitList.poll();

			startGame(p1, p2);
		}
	}

	private void startGame(ClientThread p1, ClientThread p2) {
		GameSession session = new GameSession(p1, p2);
		activeGames.add(session);

		p1.setSession(session);
		p2.setSession(session);

		Message msg = new Message("RED",Message.messageType.GAME_START);
		try {
			p1.out.writeObject(msg);
			msg = new Message("BLACK", Message.messageType.GAME_START);
			p2.out.writeObject(msg);
		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}

	public class GameSession {


		private ClientThread redPlayer;
		private ClientThread blackPlayer;
		private int[][] board = new int[8][8];
		private int turn;

		public GameSession(ClientThread r, ClientThread b) {
			this.redPlayer = r;
			this.redPlayer.playerColor = 1;
			this.blackPlayer = b;
			this.blackPlayer.playerColor = 2;
			this.turn = 1;
			initializeBoard();
		}

		public ClientThread getOpponent(ClientThread p) {
			return (p == redPlayer) ? blackPlayer : redPlayer;
		}
		private void initializeBoard() {
			for (int r = 0; r < 8; r++) {
				for (int c = 0; c < 8; c++) {
					if((r == 0 && c % 2 == 1) || (r == 1 && c % 2 == 0) || (r == 2 && c % 2 == 1)){
						board[r][c] = 2;
					}
					else if ((r == 6 && c % 2 == 1) || (r == 7 && c % 2 == 0) || (r == 5 && c % 2 == 0)) {
						board[r][c] = 1;
					}
					else {
						board[r][c] = 0;
					}
				}
			}
		}
		public int getTurn() {
			return turn;
		}
	}
}