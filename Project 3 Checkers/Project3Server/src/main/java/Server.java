import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Consumer;

import java.util.LinkedList;

public class Server {

	int count = 1;
	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	TheServer server;
	private Consumer<Serializable> callback;
	HashMap<Integer, String> userNames = new HashMap<>();
	HashMap<String,String> userInfo = new HashMap<>();
	HashMap<String, Message.MyWinDrawLoss> userScores = new HashMap<>();
	private final Queue<ClientThread> waitList = new LinkedList<>();
	private final ArrayList<GameSession> activeGames = new ArrayList<>();
	HashMap<String,HashSet<String>> friendsMap = new HashMap<>();
	HashMap<String,HashSet<String>> pendingRequests = new HashMap<>();
	Scanner sc;
	Server(Consumer<Serializable> call){

		try {
			sc = new Scanner(Path.of("info.txt"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		while(sc.hasNext()){
			String name = sc.next();
			String password = sc.next();
			int wins = Integer.parseInt(sc.next());
			int draws = Integer.parseInt(sc.next());
			int losses = Integer.parseInt(sc.next());
			Message.MyWinDrawLoss scores = new Message.MyWinDrawLoss(wins, draws, losses);
			userScores.put(name,scores);
			userInfo.put(name,password);
			sc.nextLine();
		}
		sc.close();
		try {
			sc = new Scanner(new File("users.txt"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		while (sc.hasNextLine()) {
			String line = sc.nextLine().trim();
			if (line.isEmpty()) continue;

			Scanner lineScanner = new Scanner(line);

			String username = lineScanner.next();
			HashSet<String> friendSet = new HashSet<>();

			while (lineScanner.hasNext()) {
				friendSet.add(lineScanner.next());
			}

			friendsMap.put(username, friendSet);
			lineScanner.close();
		}
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
		Message.MyWinDrawLoss clientScore;
		boolean rematchRequested;

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
			//updateClients(msg);
			callback.accept(msg);

			 while(true) {
				try {
					Message data = (Message) in.readObject(); // This reads txt from client

					if (data.msgType() == Message.messageType.USERNAME) {
//						if (!userInfo.containsKey(data.getActiveUsers().get(0)) && !Objects.equals(data.getActiveUsers().get(1), "")) {
//							userNames.put(count, data.getActiveUsers().get(0));
//
//							Files.write(
//									Path.of("info.txt"),
//									(data.getActiveUsers().get(0) + " " + data.getActiveUsers().get(1) + " 0 0 0" + "\n").getBytes(),
//									StandardOpenOption.APPEND
//							);
//							userInfo.put(data.getActiveUsers().get(0),data.getActiveUsers().get(1));
//							Message.MyWinDrawLoss newScore = new Message.MyWinDrawLoss(0, 0, 0);
//							userScores.put(data.getActiveUsers().get(0), newScore);
//							Files.write(
//									Path.of("users.txt"),
//									(data.getActiveUsers().get(0) + "\n").getBytes(),
//									StandardOpenOption.APPEND
//							);
//							msg = new Message("client: " + count + " name set: " + data.getActiveUsers().get(0));
//							clientScore = newScore;
//							callback.accept(msg);
//							msg = new Message(data.getActiveUsers().get(0),Message.messageType.USERNAME);
//							out.writeObject(msg);
//							msg = (Message) in.readObject();
//							if(msg.msgType() != Message.messageType.ACK){
//								throw new RuntimeException();
//							}
//							msg = new Message(clientScore, Message.messageType.SCORES);
//
//							out.writeObject(msg);
//							ArrayList<String> tempList = new ArrayList<>(userNames.values());
//							msg = new Message(tempList, Message.messageType.USERLOG);
//							updateClients(msg);
//							callback.accept(msg);
//
//							HashSet<String> friends = new HashSet<>();
//							friendsMap.put(data.getActiveUsers().get(0), friends);
//							msg = new Message(friends, Message.messageType.FRIENDS);
//							out.writeObject(msg);
//						}
						if(userInfo.containsKey(data.getActiveUsers().get(0)) && userInfo.get(data.getActiveUsers().get(0)).equals(data.getActiveUsers().get(1))){
							userNames.put(count, data.getActiveUsers().get(0));
							msg = new Message("client: " + count + " name set: " + data.getActiveUsers().get(0));
							//updateClients(msg);
							callback.accept(msg);
							msg = new Message(data.getActiveUsers().get(0),Message.messageType.USERNAME);
							out.writeObject(msg);
							msg = (Message) in.readObject();
							if(msg.msgType() != Message.messageType.ACK){
								throw new RuntimeException();
							}
							clientScore = userScores.get(data.getActiveUsers().get(0));
							msg = new Message(clientScore, Message.messageType.SCORES);
							out.writeObject(msg);
							ArrayList<String> tempList = new ArrayList<>(userNames.values());
							msg = new Message(tempList, Message.messageType.USERLOG);
							updateClients(msg);
							callback.accept(msg);

							if(friendsMap.get(data.getActiveUsers().get(0)) == null){
								HashSet<String> friends = new HashSet<>();
								friendsMap.put(data.getActiveUsers().get(0), friends);
							}
							HashSet<String> temp = friendsMap.get(data.getActiveUsers().get(0));
							temp.retainAll(userNames.values());
							out.reset();
							msg = new Message(temp, Message.messageType.FRIENDS);
							out.writeObject(msg);

							String loggedInUser = data.getActiveUsers().get(0);
							for (String friend : friendsMap.get(loggedInUser)) {
								friendsMap.putIfAbsent(friend, new HashSet<>());
								friendsMap.get(friend).add(loggedInUser);
							}
							for (ClientThread c : clients) {
								String other = userNames.get(c.count);
								if (other == null) continue;

								if (friendsMap.getOrDefault(other, new HashSet<>()).contains(loggedInUser)) {

									HashSet<String> updated = new HashSet<>(friendsMap.get(other));
									updated.retainAll(userNames.values()); // only online friends

									try {
										c.out.reset();
										c.out.writeObject(new Message(updated, Message.messageType.FRIENDS));
									} catch (Exception ignored) {}
								}
							}
						}
						else {
							msg = new Message("", Message.messageType.USERNAME);
							out.writeObject(msg);
						}
					}
					else if(data.msgType() == Message.messageType.REGISTER) {
						if (!userInfo.containsKey(data.getActiveUsers().get(0)) && !Objects.equals(data.getActiveUsers().get(1), "")) {
							userNames.put(count, data.getActiveUsers().get(0));

							Files.write(
									Path.of("info.txt"),
									(data.getActiveUsers().get(0) + " " + data.getActiveUsers().get(1) + " 0 0 0" + "\n").getBytes(),
									StandardOpenOption.APPEND
							);
							userInfo.put(data.getActiveUsers().get(0), data.getActiveUsers().get(1));
							Message.MyWinDrawLoss newScore = new Message.MyWinDrawLoss(0, 0, 0);
							userScores.put(data.getActiveUsers().get(0), newScore);
							Files.write(
									Path.of("users.txt"),
									(data.getActiveUsers().get(0) + "\n").getBytes(),
									StandardOpenOption.APPEND
							);
						} else {
							msg = new Message("", Message.messageType.USERNAME);
							out.writeObject(msg);
						}
					}
					else if(data.msgType() == Message.messageType.GAME_START){
						addToQueue(this);
					}
					else if(data.msgType() == Message.messageType.CHECKERMOVE && game!=null){
						if(game.getTurn() != playerColor){
							msg = new Message("IT IS NOT YOUR TURN",Message.messageType.CHECKERMOVE);
							out.writeObject(msg);
						}
						else {
							msg = game.evaluate(data.getFromRow(), data.getFromCol(), data.getToRow(), data.getToCol());
							if (msg.returnMessage() != null) {
								out.writeObject(msg);
							} else {
								game.getOpponent(this).out.writeObject(msg);
								out.writeObject(msg);
								if (!game.canMoveAgain) {
									if (game.getTurn() == 1) {
										game.setTurn(2);
									} else {
										game.setTurn(1);
									}
								}
								if(game.whiteLost()){
									game.getWhitePlayer().clientScore.losses++;
									game.getRedPlayer().clientScore.wins++;

									out.reset();
									msg = new Message(clientScore,Message.messageType.SCORES);
									out.writeObject(msg);

									out.reset();
									msg = new Message("OPPONENT",game.getOpponent(this).clientScore,Message.messageType.SCORES);
									out.writeObject(msg);

									game.getOpponent(this).out.reset();
									msg = new Message(game.getOpponent(this).clientScore,Message.messageType.SCORES);
									game.getOpponent(this).out.writeObject(msg);

									game.getOpponent(this).out.reset();
									msg = new Message("OPPONENT",clientScore,Message.messageType.SCORES);
									game.getOpponent(this).out.writeObject(msg);

									msg = new Message("RED WON", Message.messageType.GAME_OVER);
									game.getWhitePlayer().out.writeObject(msg);
									msg = new Message("YOU WON", Message.messageType.GAME_OVER);
									game.getRedPlayer().out.writeObject(msg);
									activeGames.remove(game);
									game = null;
								}
								if(game != null && game.redLost()){
									game.getRedPlayer().clientScore.losses++;
									game.getWhitePlayer().clientScore.wins++;

									out.reset();
									msg = new Message(clientScore,Message.messageType.SCORES);
									out.writeObject(msg);

									out.reset();
									msg = new Message("OPPONENT",game.getOpponent(this).clientScore,Message.messageType.SCORES);
									out.writeObject(msg);

									game.getOpponent(this).out.reset();
									msg = new Message(game.getOpponent(this).clientScore,Message.messageType.SCORES);
									game.getOpponent(this).out.writeObject(msg);

									game.getOpponent(this).out.reset();
									msg = new Message("OPPONENT",clientScore,Message.messageType.SCORES);
									game.getOpponent(this).out.writeObject(msg);

									msg = new Message("WHITE WON", Message.messageType.GAME_OVER);
									game.getRedPlayer().out.writeObject(msg);
									msg = new Message("YOU WON", Message.messageType.GAME_OVER);
									game.getWhitePlayer().out.writeObject(msg);
									activeGames.remove(game);
									game = null;
								}
								if(game != null && game.movesWithoutCap == 40){
									game.getRedPlayer().clientScore.draws++;
									game.getWhitePlayer().clientScore.draws++;

									out.reset();
									msg = new Message(clientScore,Message.messageType.SCORES);
									out.writeObject(msg);

									out.reset();
									msg = new Message("OPPONENT",game.getOpponent(this).clientScore,Message.messageType.SCORES);
									out.writeObject(msg);

									game.getOpponent(this).out.reset();
									msg = new Message(game.getOpponent(this).clientScore,Message.messageType.SCORES);
									game.getOpponent(this).out.writeObject(msg);

									game.getOpponent(this).out.reset();
									msg = new Message("OPPONENT",clientScore,Message.messageType.SCORES);
									game.getOpponent(this).out.writeObject(msg);

									msg = new Message("DRAW", Message.messageType.GAME_OVER);
									game.getWhitePlayer().out.writeObject(msg);
									game.getRedPlayer().out.writeObject(msg);
									activeGames.remove(game);
									game = null;
								}
							}
						}
					}
					else if(data.msgType() == Message.messageType.GAME_OVER && game!=null){
						if(playerColor == 1){
							game.player1draw = true;
						}
						else{
							game.player2draw = true;
						}
						if(game.player1draw && game.player2draw){
							game.getRedPlayer().clientScore.draws++;
							game.getWhitePlayer().clientScore.draws++;
							System.out.println(clientScore.draws + " " + game.getOpponent(this).clientScore.draws);

							out.reset();
							msg = new Message(clientScore,Message.messageType.SCORES);
							out.writeObject(msg);

							out.reset();
							msg = new Message("OPPONENT",game.getOpponent(this).clientScore,Message.messageType.SCORES);
							out.writeObject(msg);

							game.getOpponent(this).out.reset();
							msg = new Message(game.getOpponent(this).clientScore,Message.messageType.SCORES);
							game.getOpponent(this).out.writeObject(msg);

							game.getOpponent(this).out.reset();
							msg = new Message("OPPONENT",clientScore,Message.messageType.SCORES);
							game.getOpponent(this).out.writeObject(msg);

							msg = new Message("DRAW", Message.messageType.GAME_OVER);
							out.writeObject(msg);
							game.getOpponent(this).out.writeObject(msg);

							activeGames.remove(game);
							game = null;
						}
						else {
							msg = new Message("Your Opponent requested a draw");
							game.getOpponent(this).out.writeObject(msg);
						}
					}
					else if(data.msgType() == Message.messageType.RESIGN && game!=null){
						if(playerColor == 1){
							game.getRedPlayer().clientScore.losses++;
							game.getWhitePlayer().clientScore.wins++;

							out.reset();
							msg = new Message(clientScore,Message.messageType.SCORES);
							out.writeObject(msg);

							out.reset();
							msg = new Message("OPPONENT",game.getOpponent(this).clientScore,Message.messageType.SCORES);
							out.writeObject(msg);

							game.getOpponent(this).out.reset();
							msg = new Message(game.getOpponent(this).clientScore,Message.messageType.SCORES);
							game.getOpponent(this).out.writeObject(msg);

							game.getOpponent(this).out.reset();
							msg = new Message("OPPONENT",clientScore,Message.messageType.SCORES);
							game.getOpponent(this).out.writeObject(msg);

							msg = new Message("WHITE WON", Message.messageType.GAME_OVER);
							game.getRedPlayer().out.writeObject(msg);
							msg = new Message("YOU WON", Message.messageType.GAME_OVER);
							game.getWhitePlayer().out.writeObject(msg);
							activeGames.remove(game);
							game = null;
						}
						else{
							game.getRedPlayer().clientScore.wins++;
							game.getWhitePlayer().clientScore.losses++;

							out.reset();
							msg = new Message(clientScore,Message.messageType.SCORES);
							out.writeObject(msg);

							out.reset();
							msg = new Message("OPPONENT",game.getOpponent(this).clientScore,Message.messageType.SCORES);
							out.writeObject(msg);

							game.getOpponent(this).out.reset();
							msg = new Message(game.getOpponent(this).clientScore,Message.messageType.SCORES);
							game.getOpponent(this).out.writeObject(msg);

							game.getOpponent(this).out.reset();
							msg = new Message("OPPONENT",clientScore,Message.messageType.SCORES);
							game.getOpponent(this).out.writeObject(msg);

							msg = new Message("RED WON", Message.messageType.GAME_OVER);
							game.getWhitePlayer().out.writeObject(msg);
							msg = new Message("YOU WON", Message.messageType.GAME_OVER);
							game.getRedPlayer().out.writeObject(msg);
							activeGames.remove(game);
							game = null;
						}
					}
					else if (data.msgType() == Message.messageType.FRIEND_REQUEST) {

						String sender = userNames.get(count);
						String target = data.returnMessage();

						// Already friends?
						if (friendsMap.get(sender).contains(target)) return;

						// Ensure both users have pending lists
						pendingRequests.putIfAbsent(sender, new HashSet<>());
						pendingRequests.putIfAbsent(target, new HashSet<>());

						// If target already requested sender → auto-friend
						if (pendingRequests.get(sender).contains(target)) {

							friendsMap.get(sender).add(target);
							friendsMap.get(target).add(sender);
							System.out.println(friendsMap.get(target) + " HERE");
							// Send updated lists
							out.reset();
							HashSet<String> friends = friendsMap.get(sender);
							friends.retainAll(userNames.values());
							out.writeObject(new Message(friends, Message.messageType.FRIENDS));

							for (ClientThread c : clients) {
								if (Objects.equals(userNames.get(c.count), target)) {
									friends = friendsMap.get(target);
									friends.retainAll(userNames.values());
									c.out.reset();
									c.out.writeObject(new Message(friends,Message.messageType.FRIENDS));
								}
							}
						}
						else {
							// Store sender → target request
							pendingRequests.get(target).add(sender);

							// Notify target with a simple message (your client expects this)
							for (ClientThread c : clients) {
								if (Objects.equals(userNames.get(c.count), target)) {
									c.out.reset();
									c.out.writeObject(new Message(sender + " requested to be your friend"));
								}
							}
						}
					}
					else if(data.msgType() == Message.messageType.QUIT){
						rematchRequested = false;
						for (ClientThread c : clients) {
							if(Objects.equals(userNames.get(c.count), data.returnMessage())){
								msg = new Message(Message.messageType.QUIT);
								c.out.writeObject(msg);
							}
						}
					}
					else if(data.msgType() == Message.messageType.REMATCH){
						rematchRequested = true;
						for (ClientThread c : clients) {
							if(Objects.equals(userNames.get(c.count), data.returnMessage())){
								if(c.rematchRequested){
									msg = new Message(Message.messageType.REMATCH);
									c.out.writeObject(msg);
									out.writeObject(msg);
									startGame(this,c);
									c.rematchRequested = false;
									this.rematchRequested = false;
								}
								else {
									msg = new Message(data.returnMessage() + " requested a rematch!",Message.messageType.REMATCH);
									c.out.writeObject(msg);
								}
							}
						}
					}
					else if(data.msgType() == Message.messageType.GLOBAL && game!=null){
						msg = new Message("user: " + userNames.get(count) + ": " + data.returnMessage());
						game.getOpponent(this).out.writeObject(msg);
						callback.accept(msg);
					}
				}
				catch(Exception e) {
					e.printStackTrace();
					msg = new Message("OOOOPPs...Something wrong with the socket from client: " + count + "....closing down!");
					callback.accept(msg);
					try {
						if (game != null) {
							game.getOpponent(this).clientScore.wins++;
							this.clientScore.losses++;

							game.getOpponent(this).out.reset();
							msg = new Message(game.getOpponent(this).clientScore, Message.messageType.SCORES);
							game.getOpponent(this).out.writeObject(msg);

							game.getOpponent(this).out.reset();
							msg = new Message("OPPONENT", clientScore, Message.messageType.SCORES);
							game.getOpponent(this).out.writeObject(msg);

							msg = new Message("YOU WON", Message.messageType.GAME_OVER);
							game.getOpponent(this).out.writeObject(msg);
							activeGames.remove(game);
							game = null;
						}
					}
					catch (Exception e1) {
						e1.printStackTrace();
					}
					try {
						List<String> lines = Files.readAllLines(Path.of("info.txt"));

						for (int i = 0; i < lines.size(); i++) {
							String[] p = lines.get(i).split(" ");
							if (p[0].equals(userNames.get(count))) {
								p[2] = String.valueOf(clientScore.wins);
								p[3] = String.valueOf(clientScore.draws);
								p[4] = String.valueOf(clientScore.losses);
								lines.set(i, String.join(" ", p));
								break;
							}
						}

						Files.write(Path.of("info.txt"), lines);
					}
					catch(Exception e1) {
						e1.printStackTrace();
					}
					try {
						List<String> lines = Files.readAllLines(Path.of("users.txt"));

						for (int i = 0; i < lines.size(); i++) {
							String[] p = lines.get(i).split(" ");
							if (p[0].equals(userNames.get(count))) {
								String friends = String.join(" ", friendsMap.get(userNames.get(count)));
								lines.set(i, p[0] + " " + friends);

								break;
							}
						}

						Files.write(Path.of("users.txt"), lines);
					}
					catch(Exception e1) {
						e1.printStackTrace();
					}
					msg = new Message("Client #" + count + " has left the server!");
					updateClients(msg);
					String disconnectedUser = userNames.get(count);

// Update all users who had THIS user as a friend
					userNames.remove(count);
					for (ClientThread c : clients) {
						String other = userNames.get(c.count);
						if (other == null) continue;

						// If OTHER user had disconnectedUser as a friend
						if (friendsMap.getOrDefault(other, new HashSet<>()).contains(disconnectedUser)) {

							HashSet<String> updated = new HashSet<>(friendsMap.get(other));
							updated.retainAll(userNames.values()); // only online friends

							try {
								c.out.reset();
								c.out.writeObject(new Message(updated, Message.messageType.FRIENDS));
							} catch (Exception ignored) {}
						}
					}
					clients.remove(this);
					waitList.remove(this);
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
		ArrayList<String> names = new ArrayList<>();
		names.add(userNames.get(p1.count));
		names.add(userNames.get(p2.count));
		Message msg;
		try {
			msg = new Message("RED",Message.messageType.GAME_START, names);
			p1.out.writeObject(msg);
			msg = new Message("WHITE", Message.messageType.GAME_START, names);
			p2.out.writeObject(msg);
			msg = new Message("OPPONENT",p2.clientScore, Message.messageType.SCORES);
			p1.out.writeObject(msg);
			msg = new Message("OPPONENT",p1.clientScore, Message.messageType.SCORES);
			p2.out.writeObject(msg);
		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}

	public class GameSession {


		private ClientThread redPlayer;
		private ClientThread whitePlayer;
		private int[][] board = new int[8][8];
		private int turn;
		public boolean canMoveAgain;
		public int movesWithoutCap;
		public boolean player1draw;
		public boolean player2draw;

		public GameSession(ClientThread r, ClientThread b) {
			this.redPlayer = r;
			this.redPlayer.playerColor = 1;
			this.whitePlayer = b;
			this.whitePlayer.playerColor = 2;
			this.turn = 1;
			this.canMoveAgain = false;
			this.movesWithoutCap = 0;
			this.player1draw = false;
			this.player2draw = false;
			initializeBoard();
		}
		public ClientThread getRedPlayer() {
			return redPlayer;
		}
		public ClientThread getWhitePlayer() {
			return whitePlayer;
		}
		public ClientThread getOpponent(ClientThread p) {
			return (p == redPlayer) ? whitePlayer : redPlayer;
		}
		private void initializeBoard() {
			for (int r = 0; r < 8; r++) {
				for (int c = 0; c < 8; c++) {
					if((r == 0 && c % 2 == 0) || (r == 1 && c % 2 == 1) || (r == 2 && c % 2 == 0)){
						board[r][c] = 1;
					}
					else if ((r == 6 && c % 2 == 0) || (r == 7 && c % 2 == 1) || (r == 5 && c % 2 == 1)) {
						board[r][c] = 2;
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
		public void setTurn(int turn) {
			this.turn = turn;
		}

		public Message evaluate(int fromRow, int fromCol, int toRow, int toCol) {
			Message msg;
			int capturedRow = -1;
			int capturedCol = -1;
			int kingRow = -1;
			int kingCol = -1;
			int piece;
			if(toRow == fromRow && toCol == fromCol) {
				msg = new Message("YOU CANNOT MOVE TO THE SAME SPOT", Message.messageType.CHECKERMOVE);
				return msg;
			}

			if((toRow + toCol) % 2 == 1) {
				msg = new Message("YOU CANNOT MOVE TO A WHITE SQUARE", Message.messageType.CHECKERMOVE);
				return msg;
			}

			if(turn == 1 && (board[fromRow][fromCol] == 2 || board[fromRow][fromCol] == 4)) {
				msg = new Message("YOU CANNOT MOVE WHITE PIECES", Message.messageType.CHECKERMOVE);
				return msg;
			}

			if(turn == 2 && (board[fromRow][fromCol] == 1 || board[fromRow][fromCol] == 3)) {
				msg = new Message("YOU CANNOT MOVE RED PIECES", Message.messageType.CHECKERMOVE);
				return msg;
			}

			if(board[toRow][toCol] != 0){
				msg = new Message("YOU CANNOT LAND ON ANOTHER PIECE", Message.messageType.CHECKERMOVE);
				return msg;
			}

			boolean isSimpleMove = false;
			boolean isJump = false;
			int rowDif =  toRow - fromRow;
			int colDif = toCol - fromCol;

			if(colDif == 0 || rowDif == 0) {
				msg = new Message("PIECES CANNOT MOVE IN STRAIGHT DIRECTIONS", Message.messageType.CHECKERMOVE);
				return msg;
			}

			if(Math.abs(rowDif) == 1 && Math.abs(colDif) == 1) {
				isSimpleMove = true;
			}
			else{
				isJump = true;
			}

			if(isSimpleMove && ((toRow <= fromRow && board[fromRow][fromCol] == 1) || (toRow >= fromRow && board[fromRow][fromCol] == 2))) {
				msg = new Message("REGULAR PIECES CAN MOVE ONLY DIAGONALLY FORWARD", Message.messageType.CHECKERMOVE);
				return msg;
			}

			if(playerMustJump(turn) && isSimpleMove){
				msg = new Message("YOU MUST JUMP IF YOU CAN", Message.messageType.CHECKERMOVE);
				return msg;
			}
			if(isSimpleMove && (turn == 1) && (rowDif != 1) && (board[fromRow][fromCol] == 1)){
				msg = new Message("YOU CAN ONLY MOVE CHECKERS FORWARD", Message.messageType.CHECKERMOVE);
				return msg;
			}
			if(isSimpleMove && (turn == 2) && (rowDif != -1) && (board[fromRow][fromCol] == 2)){
				msg = new Message("YOU CAN ONLY MOVE CHECKERS FORWARD", Message.messageType.CHECKERMOVE);
				return msg;
			}
			if(isJump && !(Math.abs(rowDif) == 2 && Math.abs(colDif) == 2)) {
				msg = new Message("YOU CAN ONLY JUMP DIAGONALLY ABOVE ONE ENEMY CHECKER", Message.messageType.CHECKERMOVE);
				return msg;
			}
			if(isJump && (turn == 1) && (board[fromRow][fromCol] == 1) &&(rowDif != 2)){
				msg = new Message("YOU CAN ONLY JUMP CHECKERS FORWARD", Message.messageType.CHECKERMOVE);
				return msg;
			}
			if(isJump && (turn == 2) && (board[fromRow][fromCol] == 2) &&(rowDif != -2)){
				msg = new Message("YOU CAN ONLY JUMP CHECKERS FORWARD",Message.messageType.CHECKERMOVE);
				return msg;
			}
			if(isSimpleMove){
				piece = board[fromRow][fromCol];
				board[toRow][toCol] = piece;
				board[fromRow][fromCol] = 0;
				if((toRow == 7 && piece == 1)){
					kingRow = toRow;
					kingCol = toCol;
					board[kingRow][kingCol] = 3;
				}
				if(toRow == 0 && piece == 2){
					kingRow = toRow;
					kingCol = toCol;
					board[kingRow][kingCol] = 4;
				}
				++movesWithoutCap;
				msg = new Message(fromRow,fromCol,toRow,toCol,-1,-1,kingRow,kingCol, Message.messageType.CHECKERMOVE);
				return msg;
			}
			else {
				int jumpedRow = (fromRow + toRow) / 2;
				int jumpedCol = (fromCol + toCol) / 2;

				int jumpedPiece = board[jumpedRow][jumpedCol];
				if((turn == 1 && ((jumpedPiece == 1) || (jumpedPiece == 3))) || (turn == 2 && ((jumpedPiece == 2) || (jumpedPiece == 4)))) {
					msg = new Message("YOU CANNOT CAPTURE YOUR OWN PIECES",Message.messageType.CHECKERMOVE);
					return msg;
				}
				if(jumpedPiece == 0) {
					msg = new Message("YOU CANNOT JUMP AN EMPTY SQUARE",Message.messageType.CHECKERMOVE);
					return msg;
				}
				piece = board[fromRow][fromCol];
				board[toRow][toCol] = piece;
				board[fromRow][fromCol] = 0;

				board[jumpedRow][jumpedCol] = 0;
				capturedRow = jumpedRow;
				capturedCol = jumpedCol;

				if((toRow == 7 && piece == 1)){
					kingRow = toRow;
					kingCol = toCol;
					board[kingRow][kingCol] = 3;
				}
				if(toRow == 0 && piece == 2){
					kingRow = toRow;
					kingCol = toCol;
					board[kingRow][kingCol] = 4;
				}
				canMoveAgain = canJumpAgain(toRow, toCol);
				movesWithoutCap = 0;
				msg = new Message(fromRow,fromCol,toRow,toCol,capturedRow,capturedCol,kingRow,kingCol, Message.messageType.CHECKERMOVE);
				return msg;
			}


		}
		public boolean canJumpAgain(int row, int col) {
			if(board[row][col] == 1 ||  board[row][col] == 3){
				if(row + 2 < 8 && col + 2 < 8 && board[row + 2][col + 2] == 0) {
					if(board[row + 1][col + 1] == 2 || board[row + 1][col + 1] == 4){
						return true;
					}
				}
				if(row + 2 < 8 && col - 2 >= 0 && board[row + 2][col - 2] == 0) {
					if(board[row + 1][col - 1] == 2 || board[row + 1][col - 1] == 4){
						return true;
					}
				}
			}
			if(board[row][col] == 3){
				if(row - 2 >= 0 && col - 2 >= 0 && board[row - 2][col - 2] == 0) {
					if(board[row - 1][col - 1] == 2 || board[row - 1][col - 1] == 4){
						return true;
					}
				}
				if(row - 2 >= 0 && col + 2 < 8 && board[row - 2][col + 2] == 0) {
					if(board[row - 1][col + 1] == 2 || board[row - 1][col + 1] == 4){
						return true;
					}
				}
			}
			if(board[row][col] == 2 || board[row][col] == 4){
				if(row - 2 >= 0 && col + 2 < 8 && board[row - 2][col + 2] == 0) {
					if(board[row - 1][col + 1] == 1 || board[row - 1][col + 1] == 3){
						return true;
					}
				}
				if(row - 2 >= 0 && col - 2 >= 0 && board[row - 2][col - 2] == 0) {
					if(board[row - 1][col - 1] == 1 || board[row - 1][col - 1] == 3){
						return true;
					}
				}
			}
			if(board[row][col] == 4){
				if(row + 2 < 8 && col - 2 >= 0 && board[row + 2][col - 2] == 0) {
					if(board[row + 1][col - 1] == 1 || board[row + 1][col - 1] == 3){
						return true;
					}
				}
				if(row + 2 < 8 && col + 2 < 8 && board[row + 2][col + 2] == 0) {
					if(board[row + 1][col + 1] == 1 || board[row + 1][col + 1] == 3){
						return true;
					}
				}
			}
			return false;
		}
		public boolean playerMustJump(int turn) {
			if(turn == 1){
				for(int r = 0; r < 8; r++) {
					for(int c = 0; c < 8; c++) {
						if(board[r][c] == 1 || board[r][c] == 3){
							if(r + 2 < 8 && c + 2 < 8 && board[r + 2][c + 2] == 0) {
								if(board[r + 1][c + 1] == 2 || board[r + 1][c + 1] == 4){
									return true;
								}
							}
							if(r + 2 < 8 && c - 2 >= 0 && board[r + 2][c - 2] == 0) {
								if(board[r + 1][c - 1] == 2 || board[r + 1][c - 1] == 4){
									return true;
								}
							}
						}
						if(board[r][c] == 3){
							if(r - 2 >= 0 && c - 2 >= 0 && board[r - 2][c - 2] == 0) {
								if(board[r - 1][c - 1] == 2 || board[r - 1][c - 1] == 4){
									return true;
								}
							}
							if(r - 2 >= 0 && c + 2 < 8 && board[r - 2][c + 2] == 0) {
								if(board[r - 1][c + 1] == 2 || board[r - 1][c + 1] == 4){
									return true;
								}
							}
						}
					}
				}
			}
			else{
				for(int r = 0; r < 8; r++) {
					for(int c = 0; c < 8; c++) {
						if(board[r][c] == 2 || board[r][c] == 4){
							if(r - 2 >= 0 && c - 2 >= 0 && board[r - 2][c - 2] == 0) {
								if(board[r - 1][c - 1] == 1 || board[r - 1][c - 1] == 3){
									return true;
								}
							}
							if(r - 2 >= 0 && c + 2 < 8 && board[r - 2][c + 2] == 0) {
								if(board[r - 1][c + 1] == 1 || board[r - 1][c + 1] == 3){
									return true;
								}
							}
						}
						if(board[r][c] == 4){
							if(r + 2 < 8 && c + 2 < 8 && board[r + 2][c + 2] == 0) {
								if(board[r + 1][c + 1] == 1 || board[r + 1][c + 1] == 3){
									return true;
								}
							}
							if(r + 2 < 8 && c - 2 >= 0 && board[r + 2][c - 2] == 0) {
								if(board[r + 1][c - 1] == 1 || board[r + 1][c - 1] == 3){
									return true;
								}
							}
						}
					}
				}
			}
			return false;
		}
		private boolean canMakeSimpleMove(int row ,int col){
			if(board[row][col] == 1 || board[row][col] == 3) {
				if(row + 1 < 8 && col + 1 < 8 && board[row+1][col+1] == 0){
					return true;
				}
				if(row + 1 < 8 && col - 1 >= 0 && board[row+1][col-1] == 0){
					return true;
				}
			}
			if(board[row][col] == 3){
				if(row - 1 >= 0 && col - 1 < 8 && board[row-1][col-1] == 0){
					return true;
				}
				if(row -1 >= 0 && col + 1 < 8 && board[row-1][col+1] == 0){
					return true;
				}
			}
			if(board[row][col] == 2 ||  board[row][col] == 4){
				if(row - 1 >= 0 && col - 1 >= 0 && board[row-1][col-1] == 0){
					return true;
				}
				if(row - 1 >= 0 && col + 1 < 8 && board[row-1][col+1] == 0){
					return true;
				}
			}
			if(board[row][col] == 4){
				if(row + 1 < 8 && col - 1 >= 0 && board[row+1][col-1] == 0){
					return true;
				}
				if(row + 1 < 8 && col + 1 < 8 && board[row+1][col+1] == 0){
					return true;
				}
			}
			return false;
		}
		public boolean redLost(){
			for(int r = 0; r < 8; r++) {
				for(int c = 0; c < 8; c++) {
					if(board[r][c] == 1 || board[r][c] == 3){
						if(canMakeSimpleMove(r,c) || canJumpAgain(r,c)){
							return false;
						}
					}
				}
			}
			return true;
		}
		public boolean whiteLost(){
			for(int r = 0; r < 8; r++) {
				for(int c = 0; c < 8; c++) {
					if(board[r][c] == 2 || board[r][c] == 4){
						if(canMakeSimpleMove(r,c) || canJumpAgain(r,c)){
							return false;
						}
					}
				}
			}
			return true;
		}
	}

}