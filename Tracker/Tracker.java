package Tracker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import Tracker.TrackerRequest.TrackerRequestType;

public class Tracker {
	private static int port;
	private static int n;
	private static int k;

	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("Usage: java Tracker [port-number] [N] [K]");
			System.exit(1);
		}

		port = Integer.parseInt(args[0]);
		n = Integer.parseInt(args[1]);
		k = Integer.parseInt(args[2]);
		PlayerList playList = new PlayerList(n, k);
		boolean listening = true;

		try (ServerSocket serverSocket = new ServerSocket(port);) {

			while (listening) {
				try (Socket clientSocket = serverSocket.accept();
						ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
						ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());) {
					TrackerRequest trackerRequest;
					String playerAddrss = clientSocket.getInetAddress().getHostAddress();
					int playerPort = clientSocket.getPort();

					while ((trackerRequest = (TrackerRequest)in.readObject()) != null) {
						System.out.println(trackerRequest);
						if (trackerRequest.getTrackerRequestType() == TrackerRequestType.JOIN) {
							PlayerInfo playerInfo = new PlayerInfo(playerAddrss, playerPort);

							playList.addPlayInfoById(trackerRequest.getPlayId(), playerInfo);
							System.out.println(playList);
							out.writeObject(playList);
							break;
						}
						
						if (trackerRequest.getTrackerRequestType() == TrackerRequestType.PLAYER_LEAVE) {
							playList.removePlayInfoById(trackerRequest.getPlayId());
							System.out.println(playList);
							break;
						}
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
				catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
