package Game;

import java.awt.AWTException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import Game.GameRequest.GameRequestType;
import Tracker.PlayerInfo;
import Tracker.PlayerList;
import Tracker.TrackerRequest;
import Tracker.TrackerRequest.TrackerRequestType;
import Utils.CustomBufferedReader;
import Utils.DeepCopy;
import Utils.NIOBuffer;
import Utils.Util;

public class Game {
	public enum PlayerType {
		PRIMARY(0), BACKUP(1), NORMAL(2);
		private int type;

		private PlayerType(int type) {
			this.type = type;
		}

		private int getType() {
			return type;
		}
	}

	private String address;
	private int port;
	private String playerId;

	private String trackerAddress;
	private int trackerPort;

	private PlayerList playerList;

	private int score;
	private Position position;

	 PlayerType playerType;

	public GameState gameState = new GameState();

	ServerSocketChannel serverSocketChannel;
	Selector selector;

	BuildMaze buildMaze;

	CustomBufferedReader customBufferedReader;

	// only Primary Server have this two maps
	public Map<String, GameThread> threadPoolMap = new HashMap<>();
	Map<InetSocketAddress, NIOBuffer> nioBufferMap = new HashMap<>();

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public PlayerList getPlayerList() {
		return playerList;
	}

	public void setPlayerList(PlayerList playerList) {
		this.playerList = playerList;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public GameState getGameState() {
		return gameState;
	}

	public void setGameState(GameState gameState) {
		this.gameState = gameState;
	}

	public void startServer() throws IOException {
		// init players
		if (gameState.playerPositionsMap.isEmpty()) {
			addNewPlayer(playerId);
			this.position = this.gameState.getPlayerPositionsMap().get(playerId);

			while (this.gameState.treasures.size() < this.playerList.getK()) {
				List<Position> occupiedPositions = new ArrayList<Position>(this.gameState.getTreasures());

				// new Treasure will not be in the same cell as other players
				// and
				// other treasures
				occupiedPositions.addAll(this.gameState.getPlayerPositionsMap().values());
				this.gameState.treasures
						.add(Util.generateRandomPosition(0, this.playerList.getN() - 1, occupiedPositions));
			}
			System.out.println(this.gameState);
		}

	}

	public void addNewPlayer(String playerId) {
		List<Position> occupiedPositions = new ArrayList<Position>(this.gameState.getTreasures());
		occupiedPositions.addAll(this.gameState.getPlayerPositionsMap().values());
		Position position = Util.generateRandomPosition(0, this.playerList.getN() - 1, occupiedPositions);
		this.gameState.getPlayerPositionsMap().put(playerId, position);
		this.gameState.getPlayerScoresMap().put(playerId, 0);
	}

	public void removePlayer(String playerId) {
		playerList.removePlayInfoById(playerId);
		gameState.removePlayerById(playerId);
	}

	private void listenForRequest() throws IOException, ClassNotFoundException, InterruptedException {
		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.bind(new InetSocketAddress(this.getAddress(), this.getPort()));
		serverSocketChannel.configureBlocking(false);

		selector = Selector.open();
		SelectionKey socketServerSelectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		boolean listening = true;

		while (listening) {
			System.out.println("type: " + playerType);
			selector.select();

			Set<SelectionKey> selectionKeys = selector.selectedKeys();
			Iterator<SelectionKey> selectKeysIterator = selectionKeys.iterator();

			while (selectKeysIterator.hasNext()) {
				SelectionKey myKey = selectKeysIterator.next();

				if (myKey.isAcceptable()) {

					SocketChannel clientSocketChannel = serverSocketChannel.accept();
					if (clientSocketChannel == null) {
						System.out.println("clientSocketChannel NULL!!!");
						continue;
					}
					clientSocketChannel.configureBlocking(false);

					clientSocketChannel.register(selector, SelectionKey.OP_READ);
					System.out.println(clientSocketChannel);
					System.out.println("Connection Accepted: " + clientSocketChannel.getLocalAddress());
				} else if (myKey.isReadable()) {
						// convert to game request
						SocketChannel clientSocketChannel = (SocketChannel) myKey.channel();

						InetSocketAddress inetSocketAddress = (InetSocketAddress) clientSocketChannel
								.getRemoteAddress();
						System.out.println("reading from " + inetSocketAddress);

						NIOBuffer nioBuffer = null;
						if (nioBufferMap.containsKey(inetSocketAddress)) {
							nioBuffer = nioBufferMap.get(inetSocketAddress);
						} else {
							nioBuffer = new NIOBuffer();
							nioBufferMap.put(inetSocketAddress, nioBuffer);
						}
						if (nioBuffer.isReadLength()) {
							nioBuffer.recv(clientSocketChannel, GameRequest.class);
							continue;
						}

						GameRequest gameRequest = nioBuffer.recv(clientSocketChannel, GameRequest.class);

						System.out.println("Message received: " + gameRequest);
						// update GUI for JOIN, QUIT, MOVE
						// if join, new Thread
						if (playerType == PlayerType.PRIMARY) {

							if (gameRequest.getGameRequestType() == GameRequestType.JOIN) {

								GameThread gameThread = new GameThread(clientSocketChannel, gameRequest, this,
										gameRequest.getPlayId());
								threadPoolMap.put(gameRequest.getPlayId(), gameThread);
								playerList.addPlayInfoById(gameRequest.getPlayId(), gameRequest.getPlayerInfo());
								gameThread.start();

							} else if (gameRequest.getGameRequestType() == GameRequestType.PRIMARY_DEAD) {
								if (!threadPoolMap.containsKey(gameRequest.getPlayId())) {
									GameThread gameThread = new GameThread(clientSocketChannel, gameRequest, this,
											gameRequest.getPlayId());
									gameThread.start();
									threadPoolMap.put(gameRequest.getPlayId(), gameThread);
								}

								GameThread gameThread = threadPoolMap.get(gameRequest.getPlayId());
								gameThread.setGame(this);
								gameThread.setGameRequest(gameRequest);
								gameThread.setSocketChannel(clientSocketChannel);
								synchronized (gameThread) {
									gameThread.notify();
								}

							} else if (gameRequest.getGameRequestType() == GameRequestType.QUIT) {
								if (!threadPoolMap.containsKey(gameRequest.getPlayId())) {
									GameThread gameThread = new GameThread(clientSocketChannel, gameRequest, this,
											gameRequest.getPlayId());
									gameThread.start();
									threadPoolMap.put(gameRequest.getPlayId(), gameThread);
								}

								GameThread gameThread = threadPoolMap.get(gameRequest.getPlayId());
								gameThread.setGame(this);
								gameThread.setGameRequest(gameRequest);
								gameThread.setSocketChannel(clientSocketChannel);
								synchronized (gameThread) {
									gameThread.notify();
								}

							} else if (gameRequest.getGameRequestType() == GameRequestType.EAST
									|| gameRequest.getGameRequestType() == GameRequestType.SOUTH
									|| gameRequest.getGameRequestType() == GameRequestType.NORTH
									|| gameRequest.getGameRequestType() == GameRequestType.WEST) {

								if (!threadPoolMap.containsKey(gameRequest.getPlayId())) {
									GameThread gameThread = new GameThread(clientSocketChannel, gameRequest, this,
											gameRequest.getPlayId());
									gameThread.start();
									threadPoolMap.put(gameRequest.getPlayId(), gameThread);
								}

								GameThread gameThread = threadPoolMap.get(gameRequest.getPlayId());
								gameThread.setGame(this);
								gameThread.setGameRequest(gameRequest);
								gameThread.setSocketChannel(clientSocketChannel);
								synchronized (gameThread) {
									System.out.println("notify ESNW");
									gameThread.notify();
								}

							} else if (gameRequest.getGameRequestType() == GameRequestType.REFRESH) {

								if (!threadPoolMap.containsKey(gameRequest.getPlayId())) {
									GameThread gameThread = new GameThread(clientSocketChannel, gameRequest, this,
											gameRequest.getPlayId());
									gameThread.start();
									threadPoolMap.put(gameRequest.getPlayId(), gameThread);
								}

								GameThread gameThread = threadPoolMap.get(gameRequest.getPlayId());
								gameThread.setGame(this);
								gameThread.setGameRequest(gameRequest);
								gameThread.setSocketChannel(clientSocketChannel);
								synchronized (gameThread) {
									System.out.println("notify REFRESH");
									gameThread.notify();
								}

							}
						} else if (playerType == PlayerType.BACKUP) {
							if (gameRequest == null) {
								System.out.println(clientSocketChannel.getRemoteAddress());
								continue;
							}
							if (gameRequest.getGameRequestType() == GameRequestType.SYNC_BACKUP) {

								playerList = gameRequest.getPlayerList();
								gameState = gameRequest.getGameState();
								GameResponse gameResponse = new GameResponse();
								gameResponse.setAck(true);
								NIOBuffer.send(clientSocketChannel, gameResponse);
								clientSocketChannel.close();
							} else if (gameRequest.getGameRequestType() == GameRequestType.PRIMARY_DEAD) {
								String prevPrimaryServer = playerList.getPrimaryServerPlayerInfo().getKey();
								String prevBackupServer = playerList.getBackupServerPlayerInfo().getKey();
								if (!prevPrimaryServer.equals(gameRequest.getPlayId())) {
									continue;
								}
								List<String> deadPlayers = new ArrayList<>();
								System.out.println("in bu ps is : " + playerList);
								playerList.removePlayInfoById(prevPrimaryServer);
								gameState.removePlayerById(prevPrimaryServer);
								deadPlayers.add(prevPrimaryServer);
								// tell normal player
								SocketChannel channel = null;
								boolean found = false;
								for (Entry<String, PlayerInfo> entry : playerList.getPlayInfos().entrySet()) {
									if (found || playerList.getPlayInfos().size() == 1) {
										break;
									}
									if (entry.getKey().equals(prevBackupServer)) {
										continue;
									}
									int waitingTime = 0;
									boolean isConnected = false;
									while (true) {
										try {
											channel = SocketChannel.open();

											channel.configureBlocking(false);

											channel.connect(new InetSocketAddress(entry.getValue().getAddress(),
													entry.getValue().getPort()));

											while (!channel.finishConnect()) {
												System.out.println("still connecting to normal player");

											}
											System.out.println("find live normal player: " + entry.getKey());
											isConnected = true;
											break;
										} catch (Exception e) {
											System.out.println("close channel at 363");
											channel.close();
											TimeUnit.MILLISECONDS.sleep(100);
											waitingTime += 100;
											System.out.println("waiting to connect to normal");
											if (waitingTime >= 300) {
												waitingTime = 0;
												playerList.removePlayInfoById(entry.getKey());
												gameState.removePlayerById(entry.getKey());
												deadPlayers.add(entry.getKey());
												System.out.println("serach live normal player");
												break;
											}

										}

									}

									if (!isConnected) {
										continue;
									}

									GameRequest newGameRequest = new GameRequest(GameRequestType.BACKUP_DEAD, playerId);
									System.out.println("send!!!!");
									newGameRequest.setGameState(gameState);
									newGameRequest.setPlayerList(playerList);
									NIOBuffer.send(channel, newGameRequest);
									nioBuffer = new NIOBuffer();

									long startTime = System.currentTimeMillis();
									while ((System.currentTimeMillis() - startTime) < 1000) {
										System.out.println("waiting 390");
										if (nioBuffer.isReadLength()) {
											nioBuffer.recv(channel, GameResponse.class);
										} else {
											GameResponse newGameResponse = nioBuffer.recv(channel, GameResponse.class);
											if (newGameResponse.getAck()) {
												System.out.println("close channel at 433");
												channel.close();
												found = true;
												break;
											}
										}

									}
								}
								GameResponse newGameResponse = new GameResponse();
								newGameResponse.setAck(true);
								newGameResponse.setGameState(gameState);
								newGameResponse.setPlayerList(playerList);
								NIOBuffer.send(clientSocketChannel, newGameResponse);
								playerType = PlayerType.PRIMARY;
								System.out.println("act as primary");
								notifyTracker(deadPlayers);

							}
						} else if (playerType == PlayerType.NORMAL) {
							if (gameRequest.getGameRequestType() == GameRequestType.HEALTH_CHECK_PING) {
								GameResponse gameResponse = new GameResponse();
								gameResponse.setAck(true);
								NIOBuffer.send(clientSocketChannel, gameResponse);
							} else if (gameRequest.getGameRequestType() == GameRequestType.BACKUP_DEAD) {
								playerList = gameRequest.getPlayerList();
								gameState = gameRequest.getGameState();
								GameResponse gameResponse = new GameResponse();
								gameResponse.setAck(true);
								NIOBuffer.send(clientSocketChannel, gameResponse);
								playerType = PlayerType.BACKUP;
								System.out.println("act as backup");
								clientSocketChannel.close();
							}
						}
					}
				}
			selectKeysIterator.remove();
		}
	}

	public void updateBackupServer() throws IOException, ClassNotFoundException, InterruptedException {
		System.out.println("update backup");
		System.out.println(playerList);
		Entry<String, PlayerInfo> backupServerPlayerInfo = this.playerList.getBackupServerPlayerInfo();
		if (!backupServerPlayerInfo.getKey().equals(playerId)) { // not self
			System.out.println(
					backupServerPlayerInfo.getValue().getAddress() + ":" + backupServerPlayerInfo.getValue().getPort());
			GameRequest gameRequest = new GameRequest(GameRequestType.SYNC_BACKUP, playerId);
			gameRequest.setGameState(gameState);
			gameRequest.setPlayerList(playerList);
			SocketChannel channel = null;
			int waitingTime = 0;
			while (true) {
				try {
					channel = SocketChannel.open();

					channel.configureBlocking(false);

					channel.connect(new InetSocketAddress(backupServerPlayerInfo.getValue().getAddress(),
							backupServerPlayerInfo.getValue().getPort()));

					while (!channel.finishConnect()) {
						System.out.println("still connecting to backup server");

					}
					break;
				} catch (Exception e) {
					channel.close();
					TimeUnit.MILLISECONDS.sleep(100);
					waitingTime += 100;
					System.out.println("waiting to connect to backup");
					if (waitingTime >= 3000) {
						waitingTime = 0;
						System.out.println("backup server is dead, cannot connect");
						transferBackupRole();
						updateBackupServer();
						return;
					}

				}

			}
			synchronized (this) {
				NIOBuffer.send(channel, gameRequest);
			}
			NIOBuffer nioBuffer = new NIOBuffer();

			boolean isListening = true;

			long startTime = System.currentTimeMillis();
			while (isListening && (System.currentTimeMillis() - startTime) < 2000) {
				if (nioBuffer.isReadLength()) {
					nioBuffer.recv(channel, GameResponse.class);
				} else {
					GameResponse gameResponse = nioBuffer.recv(channel, GameResponse.class);

					System.out.println(gameResponse);
					if (gameResponse.getAck()) {
						channel.close();
						isListening = false;
					}
				}
			}
			if (isListening == true) {
				System.out.println("backup is dead");
				transferBackupRole();
				updateBackupServer();
				return;
			} else {
				return;
			}

		}
	}

	public void transferPrimaryRole() throws IOException, InterruptedException, ClassNotFoundException {
		System.out.println("start transfer primary role");
		synchronized (this) {

			Entry<String, PlayerInfo> primaryServerPlayerInfo = this.playerList.getPrimaryServerPlayerInfo();
			Entry<String, PlayerInfo> backupServerPlayerInfo = this.playerList.getBackupServerPlayerInfo();
			String prevPrimaryServer = primaryServerPlayerInfo.getKey();
			String preBackUpServer = backupServerPlayerInfo.getKey();
			boolean found = false;
			SocketChannel channel = null;
			// playerList.removePlayInfoById(primaryServerPlayerInfo.getKey());

			for (Entry<String, PlayerInfo> entry : playerList.getPlayInfos().entrySet()) {
				if (found) {
					break;
				}
				if (entry.getKey().equals(prevPrimaryServer)) {
					continue;
				}
				int waitingTime = 0;
				boolean isConnected = false;
				while (true) {
					try {
						channel = SocketChannel.open();
						channel.configureBlocking(false);
						channel.connect(
								new InetSocketAddress(entry.getValue().getAddress(), entry.getValue().getPort()));

						System.out.println("ps is: " + entry.getKey());
						while (!channel.finishConnect()) {
							System.out.println("still connecting to primary server");

						}
						System.out.println("find live server: " + entry.getKey());
						isConnected = true;
						break;
					} catch (Exception e) {
						System.out.println("close channel at 623");
						channel.close();
						TimeUnit.MILLISECONDS.sleep(100);
						waitingTime += 100;
						System.out.println("waiting to connect to primary");
						if (waitingTime >= 1000) {
							waitingTime = 0;
							// playerList.removePlayInfoById(entry.getKey());
							System.out.println("serach live server");
							break;
						}
					}
				}
				if (!isConnected) {
					continue;
				}

				if (playerId.equals(entry.getKey())) {
					System.out.println("in here 559");
					List<String> deadPlayers = new ArrayList<>();
					System.out.println("in bu ps is : " + playerList);
					playerList.removePlayInfoById(prevPrimaryServer);
					gameState.removePlayerById(prevPrimaryServer);
					deadPlayers.add(prevPrimaryServer);
					// tell normal player
					channel = null;

					found = false;
					PlayerList temPlayerList = (PlayerList) DeepCopy.copy(playerList);
					for (Entry<String, PlayerInfo> entry2 : temPlayerList.getPlayInfos().entrySet()) {
						if (found || playerList.getPlayInfos().size() == 1) {
							playerType = PlayerType.PRIMARY;
							break;
						}
						if (entry2.getKey().equals(preBackUpServer)) {
							continue;
						}
						waitingTime = 0;
						boolean isConnected2 = false;
						while (true) {
							try {
								channel = SocketChannel.open();
								channel.configureBlocking(false);
								System.out.println(entry2.getKey());
								channel.connect(new InetSocketAddress(entry2.getValue().getAddress(),
										entry2.getValue().getPort()));

								while (!channel.finishConnect()) {
									System.out.println("still connecting to normal player");

								}
								System.out.println("find live normal player: " + entry2.getKey());
								isConnected2 = true;
								break;
							} catch (Exception e) {
								System.out.println("close channel at 596");
								channel.close();
								TimeUnit.MILLISECONDS.sleep(100);
								waitingTime += 100;
								System.out.println("waiting to connect to normal");
								if (waitingTime >= 1000) {
									waitingTime = 0;
									playerList.removePlayInfoById(entry2.getKey());
									gameState.removePlayerById(entry2.getKey());
									deadPlayers.add(entry2.getKey());
									System.out.println("serach live normal player");
									break;
								}

							}

						}

						if (!isConnected2) {
							continue;
						}

						System.out.println("send####");
						System.out.println(channel.getRemoteAddress());
						GameRequest newGameRequest = new GameRequest(GameRequestType.BACKUP_DEAD, playerId);
						newGameRequest.setGameState(gameState);
						newGameRequest.setPlayerList(playerList);
						NIOBuffer.send(channel, newGameRequest);
						NIOBuffer nioBuffer = new NIOBuffer();

						long startTime = System.currentTimeMillis();
						while ((System.currentTimeMillis() - startTime) < 1000) {
							if (nioBuffer.isReadLength()) {
								nioBuffer.recv(channel, GameResponse.class);
							} else {
								GameResponse newGameResponse = nioBuffer.recv(channel, GameResponse.class);
								if (newGameResponse.getAck()) {
									System.out.println("close channel at 4331");
									playerType = PlayerType.PRIMARY;
									channel.close();
									found = true;
									break;
								}
							}

						}
						if (channel.isOpen()) {
							channel.close();
						}
					}
					System.out.println("in here 632");
					notifyTracker(deadPlayers);
					return;
				}
				System.out.println("in here 637");
				GameRequest gameRequest = new GameRequest(GameRequestType.PRIMARY_DEAD, prevPrimaryServer);
				System.out.println("here" + playerId);
				System.out.println(gameRequest);

				NIOBuffer.send(channel, gameRequest);

				System.out.println("5" + found);
				NIOBuffer nioBuffer = new NIOBuffer();

				Long startTime = null;
				while (!found && (startTime == null || (System.currentTimeMillis() - startTime) < 2000)) {
					System.out.println("inside loop");
					Thread.sleep(100);
					if (startTime == null) {
						startTime = System.currentTimeMillis();
					}
					if (nioBuffer.isReadLength()) {
						nioBuffer.recv(channel, GameResponse.class);
					} else {
						GameResponse gameResponse = nioBuffer.recv(channel, GameResponse.class);
						playerList = gameResponse.getPlayerList();
						gameState = gameResponse.getGameState();

						System.out.println("received 659");
						found = true;
						break;
					}
				}
			}
			System.out.println("close channel at 655");
			channel.close();
		}
	}

	public void transferBackupRole() throws IOException, InterruptedException, ClassNotFoundException {
		System.out.println("start transfer backup role");
		Entry<String, PlayerInfo> primaryServerPlayerInfo = this.playerList.getPrimaryServerPlayerInfo();
		Entry<String, PlayerInfo> backupServerPlayerInfo = this.playerList.getBackupServerPlayerInfo();
		String prevPrimaryServer = primaryServerPlayerInfo.getKey();
		String preBackUpServer = backupServerPlayerInfo.getKey();
		List<String> deadPlayers = new ArrayList<>();
		
		playerList.removePlayInfoById(preBackUpServer);
		gameState.removePlayerById(preBackUpServer);
		
		notifyTracker(preBackUpServer);
		
		PlayerList temPlayerList = (PlayerList) DeepCopy.copy(playerList);
		SocketChannel channel = null;
		for (Entry<String, PlayerInfo> entry : temPlayerList.getPlayInfos().entrySet()) {
			if (entry.getKey().equals(preBackUpServer) || entry.getKey().equals(prevPrimaryServer)) {
				continue;
			}
			int waitingTime = 0;
			boolean isConnected = false;
			while (true) {
				try {
					channel = SocketChannel.open();
					channel.configureBlocking(false);
					channel.connect(
							new InetSocketAddress(entry.getValue().getAddress(), entry.getValue().getPort()));

					System.out.println("ps is: " + entry.getKey());
					while (!channel.finishConnect()) {
						System.out.println("still connecting to primary server");

					}
					System.out.println("find live normal for new backup : " + entry.getKey());
					isConnected = true;
					break;
				} catch (Exception e) {
					channel.close();
					TimeUnit.MILLISECONDS.sleep(100);
					waitingTime += 100;
					System.out.println("waiting to connect to normal for new backup");
					if (waitingTime >= 1000) {
						playerList.removePlayInfoById(entry.getKey());
						gameState.removePlayerById(entry.getKey());
						deadPlayers.add(entry.getKey());
						waitingTime = 0;
						System.out.println("serach live normal for new backup");
						break;
					}
				}
			}
			if (!isConnected) {
				continue;
			}
			
			GameRequest newGameRequest = new GameRequest(GameRequestType.BACKUP_DEAD, preBackUpServer);
			newGameRequest.setGameState(gameState);
			newGameRequest.setPlayerList(playerList);
			NIOBuffer.send(channel, newGameRequest);
			NIOBuffer nioBuffer = new NIOBuffer();

			long startTime = System.currentTimeMillis();
			while ((System.currentTimeMillis() - startTime) < 1000) {
				if (nioBuffer.isReadLength()) {
					nioBuffer.recv(channel, GameResponse.class);
				} else {
					GameResponse newGameResponse = nioBuffer.recv(channel, GameResponse.class);
					if (newGameResponse.getAck()) {
						
						channel.close();
						notifyTracker(deadPlayers);
						return;
					}
				}

			}
			if (channel.isOpen()) {
				channel.close();
			} 
		}

	}

	public void notifyTracker(List<String> playerIds) {
		for (String playerId : playerIds) {
			try (Socket socket = new Socket(trackerAddress, trackerPort);) {
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

				TrackerRequest trackerRequest = new TrackerRequest(TrackerRequestType.PLAYER_LEAVE, playerId);
				out.writeObject(trackerRequest);

				socket.close();

			} catch (IOException e) {
				System.exit(1);
			}
		}

	}

	public void notifyTracker(String playerId) {
		try (Socket socket = new Socket(trackerAddress, trackerPort);) {
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

			TrackerRequest trackerRequest = new TrackerRequest(TrackerRequestType.PLAYER_LEAVE, playerId);
			out.writeObject(trackerRequest);

			socket.close();

		} catch (IOException e) {
			System.exit(1);
		}

	}

	private void contactPrimaryServer() throws IOException, ClassNotFoundException, InterruptedException {
		System.out.println("contact primary server");

		SocketChannel channel = null;
		int waitingTime = 0;
		boolean found = false;
		boolean isConnected = false;
		for (Entry<String, PlayerInfo> entry : this.playerList.getPlayInfos().entrySet()) {
			isConnected = false;
			if (found) {
				break;
			}

			while (true) {

				try {
					channel = SocketChannel.open();

					channel.configureBlocking(false);

					channel.connect(new InetSocketAddress(entry.getValue().getAddress(), entry.getValue().getPort()));

					while (!channel.finishConnect()) {
						System.out.println("still connecting to primary server " + entry.getKey());

					}
					isConnected = true;
					break;
				} catch (Exception e) {
					channel.close();
					TimeUnit.MILLISECONDS.sleep(100);
					waitingTime += 100;
					System.out.println("waiting to connect to primary");
					if (waitingTime >= 1000) {
						waitingTime = 0;
						System.out.println("primary server is dead, cannot connect " + entry.getKey());
						break;
					}
				}
			}
			System.out.println("isConnected " + isConnected);
			if (!isConnected) {
				continue;
			}

			System.out.println(((InetSocketAddress) channel.getLocalAddress()).getHostString() + ":"
					+ ((InetSocketAddress) channel.getLocalAddress()).getPort());

			GameRequest gameRequest = new GameRequest(GameRequestType.JOIN, playerId);
			gameRequest.setPlayerInfo(new PlayerInfo(address, port));
			NIOBuffer.send(channel, gameRequest);
			NIOBuffer nioBuffer = new NIOBuffer();

			boolean isListening = true;
			long startTime = System.currentTimeMillis();
			while (isListening && System.currentTimeMillis() - startTime < 1000) {
				if (nioBuffer.isReadLength()) {
					nioBuffer.recv(channel, GameResponse.class);
				} else {
					GameResponse gameResponse = nioBuffer.recv(channel, GameResponse.class);
					if (gameResponse.getAck()) {
						gameState = gameResponse.getGameState();
						playerList = gameResponse.getPlayerList();
						System.out.println(gameResponse);
						found = true;
						break;
					}
				}
				Thread.sleep(50);
			}
			channel.close();
		}

	}

	private void start() throws ClassNotFoundException, IOException, InterruptedException, AWTException {
		startServer();
		System.out.println(playerId + " is listening for moves.");
		BufferedReader reader = null;
		reader = new BufferedReader(new InputStreamReader(System.in));
		customBufferedReader = new CustomBufferedReader(reader);

		Entry<String, PlayerInfo> primaryServerPlayerInfo = this.playerList.getPrimaryServerPlayerInfo();
		Entry<String, PlayerInfo> backupServerPlayerInfo = this.playerList.getBackupServerPlayerInfo();
		if (primaryServerPlayerInfo.getKey().equals(playerId)) {
			playerType = PlayerType.PRIMARY;
			// initialize GUI
			buildMaze = new BuildMaze(playerList.getN(), playerList.getK(), playerId, gameState.getPlayerScoresMap());
			buildMaze.pobj.game = this;
			buildMaze.scoreobj.game = this;
			customBufferedReader.buildMaze = buildMaze;
		} else {
			try {
				contactPrimaryServer();
				// initialize GUI
				buildMaze = new BuildMaze(playerList.getN(), playerList.getK(), playerId,
						gameState.getPlayerScoresMap());
				buildMaze.pobj.game = this;
				buildMaze.scoreobj.game = this;
				customBufferedReader.buildMaze = buildMaze;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("joined!");
		System.out.println(playerList);
		System.out.println(playerList.getBackupServerPlayerInfo().getKey());
		if (playerType != PlayerType.PRIMARY) {
			if (playerList.getBackupServerPlayerInfo().getKey().equals(playerId)) {
				playerType = PlayerType.BACKUP;
			} else {
				playerType = PlayerType.NORMAL;
			}
		}

		listenForRequest();

	}

	public static void main(String[] args) throws IOException, InterruptedException, AWTException {
		if (args.length != 3) {
			System.err.println("Usage: java Game [IP-address] [port-number] [player-id]");
			System.exit(1);
		}

		Game game = new Game();
		game.trackerAddress = args[0];
		game.trackerPort = Integer.parseInt(args[1]);
		// Assume playerId will not clash
		game.playerId = args[2];

		try (Socket socket = new Socket(game.trackerAddress, game.trackerPort);

				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());) {

			try {
				game.port = socket.getLocalPort();
				game.address = socket.getLocalAddress().getHostAddress();
				TrackerRequest trackerRequest = new TrackerRequest(TrackerRequestType.JOIN, game.playerId);
				out.writeObject(trackerRequest);
				game.playerList = (PlayerList) in.readObject();

				System.out.println(game.playerList);
				socket.close();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " + game.address + ":" + game.port);
			System.exit(1);
		}

		try {
			game.start();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	@Override
	public String toString() {
		return "Game [address=" + address + ", port=" + port + ", playerId=" + playerId + ", playerList=" + playerList
				+ ", score=" + score + ", position=" + position + ", gameState=" + gameState + "]";
	}
}
