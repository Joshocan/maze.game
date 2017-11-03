package Game;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

public class BuildMaze extends JFrame {
	private static final long serialVersionUID = -5188412257134488238L;
	JFrame f2;
	public MazeBox pobj;
	public ScoreCalculate scoreobj;
	JLabel lb1,lb2,lb3;
	Game game;
	public BuildMaze(int maze_size,int treasure,String player_id,Map<String, Integer> scoreMap) {
		f2 = new JFrame();
		lb1=new JLabel();
		lb2=new JLabel();
		lb3=new JLabel();
		pobj = new MazeBox(maze_size, treasure);
		System.out.println("scoremap is "+scoreMap);
		scoreobj=new ScoreCalculate(scoreMap);
		f2.setTitle(player_id);	
		f2.setSize(800, 600);
		f2.setLocationRelativeTo(null);
		f2.setLayout(null);
		f2.setVisible(true);
		f2.setResizable(false);
		f2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pobj.setBounds(340, 50, 410, 410);
		scoreobj.setBounds(00,00,300,300);
		lb2.setBounds(00, 00, 100, 100);
		lb2.setText("SCORE OF  "+player_id);
		f2.add(pobj);
		f2.add(lb2);
		f2.add(scoreobj);
		f2.revalidate();
	}
	public static void main(String ars[]) {
		int n=0,k=0;
		String id=null;
		Map<String, Integer> scoreMap=new HashMap<>();
		new BuildMaze(n,k,id,scoreMap);
	}
}
