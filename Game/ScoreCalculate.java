package Game;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.Timer;

public class ScoreCalculate extends JPanel implements ActionListener {
	private static final long serialVersionUID = -571639031027396133L;
	private Timer tm;
	Game game;
	Map<String, Integer> playerScoresMap;
	public ScoreCalculate(Map<String, Integer> scoresMap ) {
		tm = new Timer(100, this);
		tm.start();	
		playerScoresMap=scoresMap;
	}
	public void actionPerformed(ActionEvent e) {
		repaint();
	}
	public void paint(Graphics g) {
		super.paint(g);
		int pix = 400 / 40;
		g.drawString(game.gameState.getPlayerScoresMap().toString(), pix, pix);
		g.drawString(game.playerType.toString(), pix, pix+100);
		g.drawString(game.getPlayerId(), pix+100, pix+100);
		
		
	}
}
