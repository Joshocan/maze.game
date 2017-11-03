package Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import Game.Position;

public class Util {
	public static Position generateRandomPosition(int start, int end, List<Position> positions) {
		int x,y;
		if (positions.size() == 0) {
			x = Util.getRandomWithExclusion(new Random(), start, end);
			y = Util.getRandomWithExclusion(new Random(), start, end);
		} else {
			Integer[] excludedX = Util.getExcludeX(positions, end+1);
			x = Util.getRandomWithExclusion(new Random(), start, end, excludedX);
			List<Position> tempPositions = getPositionsOnSameRow(positions, x);
			Integer[] excludedY = Util.getExcludeY(tempPositions);
			y = Util.getRandomWithExclusion(new Random(), start, end, excludedY);
			
		}
		
		return new Position(x, y);
	}
	
	public static List<Position> getPositionsOnSameRow(List<Position> positions, int x) {
		List<Position> res = new ArrayList<>();
		for (Position position : positions) {
			if (position.getX() == x) {
				res.add(position);
			}
		}
		return res;
	}
	
	public static Integer[] getExcludeX(List<Position> positions,int size) {
		Map<Integer, Integer> xCount = new HashMap<Integer, Integer>();
		
		Set<Integer> excludedX = new HashSet<>();

		for (Position position : positions) {
			if (!xCount.containsKey(position.getX())) {
				xCount.put(position.getX(), 1);
			}
			else {
				xCount.put(position.getX(), xCount.get(position.getX())+1);
			}
		}
		for (Entry<Integer, Integer> entry : xCount.entrySet()) {
			if (entry.getValue() == size) {
				excludedX.add(entry.getKey());
			}
		}
		
		Integer[] xArray = new Integer[excludedX.size()];
		int count = 0;
		for (Integer integer : excludedX) {
			xArray[count] = integer;
			count++;
		}
		
		return xArray;
	}
	
	public static Integer[] getExcludeY(List<Position> positions) {
		Set<Integer> excludedY = new HashSet<>();

		
		for (Position position : positions) {
			excludedY.add(position.getY());
		}
		
		Integer[] yArray = new Integer[excludedY.size()];
		int count = 0;
		for (Integer integer : excludedY) {
			yArray[count] = integer;
			count++;
		}
		
		return yArray;
	}
	
	public static int getRandomWithExclusion(Random rnd, int start, int end, Integer... exclude) {
	    int random = start + rnd.nextInt(end - start + 1 - exclude.length);
	    for (Integer ex : exclude) {
	        if (random < ex) {
	            break;
	        }
	        random++;
	    }
	    return random;
	}
}
