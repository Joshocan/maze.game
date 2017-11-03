package Game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;
import javax.swing.Timer;

import Game.GameRequest.GameRequestType;
import Tracker.PlayerInfo;
import Utils.NIOBuffer;

public class MazeBox extends JPanel implements ActionListener, KeyListener {
	private static final long serialVersionUID = -2906991934227777692L;
	private Timer tm;
	int size, treasure;
	int pix;
	int score;
	Font font;
	Toolkit toolkit = Toolkit.getDefaultToolkit();
	Image player, treasure_image;
	public Game game;
	Position player_position;
	int posX = 0;
	int posY = 0;
	boolean isFirstTime = true;

	public MazeBox(int s, int t) {
		tm = new Timer(100, this);
		tm.start();
		addKeyListener(this);
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		size = s;
		treasure = t;
		pix = 400 / size;
		player = toolkit.getImage("icon//Player_image.jpg");
		treasure_image = toolkit.getImage("icon//treasure.jpg");
	}

	public void actionPerformed(ActionEvent e) {
		repaint();
	}

	public void keyPressed(KeyEvent e) {
		SocketChannel channel = null;
		try {
			synchronized (this) {

				int c = e.getKeyCode();

				Entry<String, PlayerInfo> primaryServerPlayerInfo = game.getPlayerList().getPrimaryServerPlayerInfo();
				// connect to primary server

				int waitingTime = 0;
				while (true) {
					try {
						channel = SocketChannel.open();

						channel.configureBlocking(false);

						channel.connect(new InetSocketAddress(primaryServerPlayerInfo.getValue().getAddress(),
								primaryServerPlayerInfo.getValue().getPort()));

						while (!channel.finishConnect()) {
							System.out
									.println("still connecting to primary server: " + primaryServerPlayerInfo.getKey());

						}
						break;
					} catch (Exception ex) {
						channel.close();
						TimeUnit.MILLISECONDS.sleep(100);
						waitingTime += 100;
						System.out.println("waiting to connect to primary");
						if (waitingTime >= 1000) {
							waitingTime = 0;
							System.out.println("primary server is dead, cannot connect");
							channel.close();
							game.transferPrimaryRole();
							System.out.println("transfered primary role");
							System.out.println(game.getPlayerList());
							this.dispatchEvent(e);
							return;
						}

					}

				}

				if (c == KeyEvent.VK_UP) {

					System.out.println(((InetSocketAddress) channel.getLocalAddress()).getHostString() + ":"
							+ ((InetSocketAddress) channel.getLocalAddress()).getPort());
					GameRequest gameRequest = new GameRequest(GameRequestType.NORTH, game.getPlayerId());
					gameRequest.setPlayerInfo(new PlayerInfo(game.getAddress(), game.getPort()));
					NIOBuffer.send(channel, gameRequest);
					NIOBuffer nioBuffer = new NIOBuffer();

					boolean isListening = true;

					while (isListening) {
						if (nioBuffer.isReadLength()) {
							nioBuffer.recv(channel, GameResponse.class);
						} else {
							GameResponse gameResponse = nioBuffer.recv(channel, GameResponse.class);
							game.setGameState(gameResponse.getGameState());
							game.setPlayerList(gameResponse.getPlayerList());

							System.out.println(gameResponse);

							isListening = false;
						}

					}
				}
				if (c == KeyEvent.VK_DOWN) {
					System.out.println(((InetSocketAddress) channel.getLocalAddress()).getHostString() + ":"
							+ ((InetSocketAddress) channel.getLocalAddress()).getPort());
					GameRequest gameRequest = new GameRequest(GameRequestType.SOUTH, game.getPlayerId());
					gameRequest.setPlayerInfo(new PlayerInfo(game.getAddress(), game.getPort()));
					NIOBuffer.send(channel, gameRequest);
					NIOBuffer nioBuffer = new NIOBuffer();

					boolean isListening = true;

					while (isListening) {
						if (nioBuffer.isReadLength()) {
							nioBuffer.recv(channel, GameResponse.class);
						} else {
							GameResponse gameResponse = nioBuffer.recv(channel, GameResponse.class);
							game.setGameState(gameResponse.getGameState());
							game.setPlayerList(gameResponse.getPlayerList());

							System.out.println(gameResponse);

							isListening = false;
						}

					}
				}
				if (c == KeyEvent.VK_LEFT) {
					System.out.println(((InetSocketAddress) channel.getLocalAddress()).getHostString() + ":"
							+ ((InetSocketAddress) channel.getLocalAddress()).getPort());
					GameRequest gameRequest = new GameRequest(GameRequestType.WEST, game.getPlayerId());
					gameRequest.setPlayerInfo(new PlayerInfo(game.getAddress(), game.getPort()));
					NIOBuffer.send(channel, gameRequest);
					NIOBuffer nioBuffer = new NIOBuffer();

					boolean isListening = true;

					while (isListening) {
						if (nioBuffer.isReadLength()) {
							nioBuffer.recv(channel, GameResponse.class);
						} else {
							GameResponse gameResponse = nioBuffer.recv(channel, GameResponse.class);
							game.setGameState(gameResponse.getGameState());
							game.setPlayerList(gameResponse.getPlayerList());

							System.out.println(gameResponse);

							isListening = false;
						}

					}
				}
				if (c == KeyEvent.VK_RIGHT) {

					System.out.println(((InetSocketAddress) channel.getLocalAddress()).getHostString() + ":"
							+ ((InetSocketAddress) channel.getLocalAddress()).getPort());
					GameRequest gameRequest = new GameRequest(GameRequestType.EAST, game.getPlayerId());
					gameRequest.setPlayerInfo(new PlayerInfo(game.getAddress(), game.getPort()));
					NIOBuffer.send(channel, gameRequest);
					NIOBuffer nioBuffer = new NIOBuffer();

					boolean isListening = true;

					while (isListening) {
						if (nioBuffer.isReadLength()) {
							nioBuffer.recv(channel, GameResponse.class);
						} else {
							GameResponse gameResponse = nioBuffer.recv(channel, GameResponse.class);
							game.setGameState(gameResponse.getGameState());
							game.setPlayerList(gameResponse.getPlayerList());

							System.out.println(gameResponse);

							isListening = false;
						}

					}
				}
				if (c == KeyEvent.VK_ESCAPE) {

					System.out.println(((InetSocketAddress) channel.getLocalAddress()).getHostString() + ":"
							+ ((InetSocketAddress) channel.getLocalAddress()).getPort());
					GameRequest gameRequest = new GameRequest(GameRequestType.QUIT, game.getPlayerId());
					gameRequest.setPlayerInfo(new PlayerInfo(game.getAddress(), game.getPort()));
					NIOBuffer.send(channel, gameRequest);
					NIOBuffer nioBuffer = new NIOBuffer();

					boolean isListening = true;

					while (isListening) {
						if (nioBuffer.isReadLength()) {
							nioBuffer.recv(channel, GameResponse.class);
						} else {
							GameResponse gameResponse = nioBuffer.recv(channel, GameResponse.class);
							game.setGameState(gameResponse.getGameState());
							game.setPlayerList(gameResponse.getPlayerList());

							System.out.println(gameResponse);
							System.exit(0);
							isListening = false;
						}

					}
				}
				if (c == KeyEvent.VK_SPACE) {

					System.out.println(((InetSocketAddress) channel.getLocalAddress()).getHostString() + ":"
							+ ((InetSocketAddress) channel.getLocalAddress()).getPort());
					GameRequest gameRequest = new GameRequest(GameRequestType.REFRESH, game.getPlayerId());
					gameRequest.setPlayerInfo(new PlayerInfo(game.getAddress(), game.getPort()));
					NIOBuffer.send(channel, gameRequest);
					NIOBuffer nioBuffer = new NIOBuffer();

					boolean isListening = true;

					while (isListening) {
						if (nioBuffer.isReadLength()) {
							nioBuffer.recv(channel, GameResponse.class);
						} else {
							GameResponse gameResponse = nioBuffer.recv(channel, GameResponse.class);
							game.setGameState(gameResponse.getGameState());
							game.setPlayerList(gameResponse.getPlayerList());

							System.out.println(gameResponse);
							isListening = false;
						}

					}
				}

				 channel.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("primary server died during communication");
			try {
				 channel.close();
				game.transferPrimaryRole();
			} catch (ClassNotFoundException | IOException | InterruptedException e1) {
				e1.printStackTrace();
			}
			this.dispatchEvent(e);
		}
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
	}

	public void paint(Graphics g) {
		super.paint(g);
		this.setBackground(Color.WHITE);
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				g.setColor(Color.black);
				g.drawRect(j * pix, i * pix, pix, pix);
			}
		}
		for (int i = 0; i < game.gameState.treasures.size(); i++) {
			g.drawImage(treasure_image, game.gameState.treasures.get(i).getY() * pix,
					game.gameState.treasures.get(i).getX() * pix, pix, pix, null);

		}
		player_position = game.gameState.playerPositionsMap.get(game.getPlayerId());

		posX = player_position.getX();
		posY = player_position.getY();
		g.drawString(game.getPlayerId(), posY * pix, (posX + 1) * pix);

	}
}
