package Utils;

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import Game.BuildMaze;

public class CustomBufferedReader {
    private volatile boolean init = false;
    private Thread backgroundReaderThread = null;
    public BuildMaze buildMaze;

    public CustomBufferedReader(final BufferedReader bufferedReader) {
        backgroundReaderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                    	
                    	String input  = bufferedReader.readLine();
                    	while ((buildMaze ==null || buildMaze.pobj == null) && !init) {
                    	}
                    	init = true;
            			List<String> validMoves = Arrays.asList("0","1","2","3","4","9");
            			
            				
            				if (input == null || !validMoves.contains(input)) {
            					continue;
            				}
            
            				int number = Integer.parseInt(input);
            				if (number == 0) {
            					// move refreshes
            					System.out.println("move refreshes");
            					KeyEvent keyEvent = new KeyEvent(buildMaze.pobj, KeyEvent.KEY_PRESSED, 20, 1, KeyEvent.VK_SPACE, '?');
            					buildMaze.pobj.dispatchEvent(keyEvent);	
            				} else if (number == 1) {
            					// move west
            					System.out.println("move west");
            					KeyEvent keyEvent = new KeyEvent(buildMaze.pobj, KeyEvent.KEY_PRESSED, 20, 1, KeyEvent.VK_LEFT, '?');
            					buildMaze.pobj.dispatchEvent(keyEvent);
            				} else if (number == 2) {
            					// move south
            					System.out.println("move south");
            					KeyEvent keyEvent = new KeyEvent(buildMaze.pobj, KeyEvent.KEY_PRESSED, 20, 1, KeyEvent.VK_DOWN, '?');
            					buildMaze.pobj.dispatchEvent(keyEvent);
            				} else if (number == 3) {
            					// move east
            					System.out.println("move east");
            					KeyEvent keyEvent = new KeyEvent(buildMaze.pobj, KeyEvent.KEY_PRESSED, 20, 1, KeyEvent.VK_RIGHT, '?');
            					buildMaze.pobj.dispatchEvent(keyEvent);
            				} else if (number == 4) {
            					// move north
            					System.out.println("move north");
            					KeyEvent keyEvent = new KeyEvent(buildMaze.pobj, KeyEvent.KEY_PRESSED, 20, 1, KeyEvent.VK_UP, '?');
            					buildMaze.pobj.dispatchEvent(keyEvent);
            				} else if (number == 9) {
            					System.out.println("quit");
            					KeyEvent keyEvent = new KeyEvent(buildMaze.pobj, KeyEvent.KEY_PRESSED, 20, 1, KeyEvent.VK_ESCAPE, '?');
            					buildMaze.pobj.dispatchEvent(keyEvent);
            				}
                        System.out.println("read: " + input);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    
                }
            }
        });
        backgroundReaderThread.setDaemon(true);
        backgroundReaderThread.start();
    }
}