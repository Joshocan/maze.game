package Game;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import Game.GameRequest.GameRequestType;
import Utils.NIOBuffer;
import Utils.Util;

public class GameThread extends Thread {
	private SocketChannel socketChannel = null;
	private GameRequest gameRequest = null;
	private Game game = null;
	private String playerId;
	public boolean running = true;

	public GameThread(SocketChannel socketChannel, GameRequest gameRequest, Game game, String playerId) {
		super();
		this.socketChannel = socketChannel;
		this.gameRequest = gameRequest;
		this.game = game;
		this.playerId = playerId;
	}

	public SocketChannel getSocketChannel() {
		return socketChannel;
	}

	public void setSocketChannel(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}

	public GameRequest getGameRequest() {
		return gameRequest;
	}

	public void setGameRequest(GameRequest gameRequest) {
		this.gameRequest = gameRequest;
	}

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public void run() {
		try {

			while (running) {
				synchronized (game) {
					boolean ack = false;
					System.out.println("Thread: " + gameRequest);
					if (gameRequest.getGameRequestType() == GameRequestType.JOIN) {
						ack  = true;
						game.addNewPlayer(playerId);
					} else if (gameRequest.getGameRequestType() == GameRequestType.QUIT) {
						if (gameRequest.getPlayId()
								.equals(game.getPlayerList().getPrimaryServerPlayerInfo().getKey())) {
							try {
								game.transferPrimaryRole();
							} catch (ClassNotFoundException e) {
								e.printStackTrace();
							}
							System.exit(0);
						}
						game.removePlayer(gameRequest.getPlayId());
						game.threadPoolMap.remove(gameRequest.getPlayId());
						running = false;
						game.notifyTracker(gameRequest.getPlayId());

					} else if (gameRequest.getGameRequestType() == GameRequestType.REFRESH) {

						// no need doing anything
						/*
						 * Position position =
						 * game.getGameState().getPlayerPositionsMap()
						 * .get(gameRequest.getPlayId()); int posX =
						 * position.getX(); int posY = position.getY(); Position
						 * temp_player_position = new Position(posX, posY);
						 */

						// no need doing anything
					} else if (gameRequest.getGameRequestType() == GameRequestType.PRIMARY_DEAD) {
						ack = true;
						// no need doing anything
					}

					else if (gameRequest.getGameRequestType() == GameRequestType.EAST
							|| gameRequest.getGameRequestType() == GameRequestType.SOUTH
							|| gameRequest.getGameRequestType() == GameRequestType.NORTH
							|| gameRequest.getGameRequestType() == GameRequestType.WEST) {
						// check if it's a valid move
						// calculating score
						// generating treasure
						// send back gamestate and playerlist

						if (gameRequest.getGameRequestType() == GameRequestType.NORTH) {

							Position position = game.getGameState().getPlayerPositionsMap()
									.get(gameRequest.getPlayId());
							int posX = position.getX();
							int posY = position.getY();
							posX--;
							if (posX < 0) {
								posX = 0;
							}

							Position temp_player_position = new Position(posX, posY);
							ack = game.gameState.playerPositionsMap.values().contains(temp_player_position);
							if (!ack) {
								game.gameState.playerPositionsMap.put(gameRequest.getPlayId(), temp_player_position);
								int score = score_calculate(temp_player_position, game.gameState.treasures,
										game.gameState.playerScoresMap.get(gameRequest.getPlayId()));
								game.gameState.playerScoresMap.put(gameRequest.getPlayId(), score);
							}
						}

						if (gameRequest.getGameRequestType() == GameRequestType.SOUTH) {

							Position position = game.getGameState().getPlayerPositionsMap()
									.get(gameRequest.getPlayId());
							int posX = position.getX();
							int posY = position.getY();
							posX++;
							if (posX > game.getPlayerList().getN() - 1) {
								posX = game.getPlayerList().getN() - 1;
							}
							Position temp_player_position = new Position(posX, posY);
							ack = game.gameState.playerPositionsMap.values().contains(temp_player_position);
							if (!ack) {
								game.gameState.playerPositionsMap.put(gameRequest.getPlayId(), temp_player_position);
								int score = score_calculate(temp_player_position, game.gameState.treasures,
										game.gameState.playerScoresMap.get(gameRequest.getPlayId()));
								game.gameState.playerScoresMap.put(gameRequest.getPlayId(), score);
							}
						}

						if (gameRequest.getGameRequestType() == GameRequestType.WEST) {

							Position position = game.getGameState().getPlayerPositionsMap()
									.get(gameRequest.getPlayId());
							int posX = position.getX();
							int posY = position.getY();
							posY--;
							if (posY < 0) {
								posY = 0;
							}
							Position temp_player_position = new Position(posX, posY);
							ack = game.gameState.playerPositionsMap.values().contains(temp_player_position);
							if (!ack) {
								game.gameState.playerPositionsMap.put(gameRequest.getPlayId(), temp_player_position);
								int score = score_calculate(temp_player_position, game.gameState.treasures,
										game.gameState.playerScoresMap.get(gameRequest.getPlayId()));
								game.gameState.playerScoresMap.put(gameRequest.getPlayId(), score);

							}
						}

						if (gameRequest.getGameRequestType() == GameRequestType.EAST) {
							Position position = game.getGameState().getPlayerPositionsMap()
									.get(gameRequest.getPlayId());
							int posX = position.getX();
							int posY = position.getY();
							posY++;
							if (posY > game.getPlayerList().getN() - 1) {
								posY = game.getPlayerList().getN() - 1;
							}
							Position temp_player_position = new Position(posX, posY);
							ack = game.gameState.playerPositionsMap.values().contains(temp_player_position);
							if (!ack) {
								game.gameState.playerPositionsMap.put(gameRequest.getPlayId(), temp_player_position);
								int score = score_calculate(temp_player_position, game.gameState.treasures,
										game.gameState.playerScoresMap.get(gameRequest.getPlayId()));
								game.gameState.playerScoresMap.put(gameRequest.getPlayId(), score);
							}
						}

						// synchronized (game) {

						// }
					}

					GameResponse gameResponse = new GameResponse();
					gameResponse.setGameState(game.getGameState());
					gameResponse.setPlayerList(game.getPlayerList());
					gameResponse.setAck(ack);
					try {
						if (game.getPlayerList().getPlayInfos().size() != 1
								&& !(game.getPlayerList().getPlayInfos().size() == 2
										&& gameRequest.getGameRequestType() == GameRequestType.JOIN)) {
							game.updateBackupServer();
						}
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}

					NIOBuffer.send(socketChannel, gameResponse);
					if (socketChannel.isOpen()) {
						socketChannel.close();
					}
					System.out.println("move done!");
					System.out.println(game.gameState);
					System.out.println(game.getPlayerList());
				}

				synchronized (this) {
					this.wait();
				}

			}

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public int score_calculate(Position player_position, List<Position> treasure_position, int score) {

		System.out.println("Player Position is: " + player_position.getX() + "," + player_position.getY());
		System.out.println("treasure positions: " + treasure_position);
		for (int i = 0; i < treasure_position.size(); i++) {
			if ((int) treasure_position.get(i).getX() == player_position.getX()) {
				if ((int) treasure_position.get(i).getY() == player_position.getY()) {
					score++;
					game.gameState.treasures.remove(treasure_position.get(i));
					List<Position> occupiedPositions = new ArrayList<Position>(game.gameState.getTreasures());
					occupiedPositions.addAll(game.gameState.getPlayerPositionsMap().values());
					game.gameState.treasures
							.add(Util.generateRandomPosition(0, game.getPlayerList().getN() - 1, occupiedPositions));

				}
			}
		}
		System.out.println("Score is " + score);
		game.setScore(score);
		return score;
	}

}
