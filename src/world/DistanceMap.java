package world;

import java.util.Comparator;
import java.util.PriorityQueue;

import math.geom2d.Vector2d;

public class DistanceMap {
	private Vector2d target;
	private double tileSize;
	private int[] distances;
	private int width;
	private int height;
	
	public DistanceMap(TileWorld<Boolean> tileWorld, double tileSize, Vector2d target) {
		width = tileWorld.getWidth();
		height = tileWorld.getHeight();
		
		this.target = target;
		this.tileSize = tileSize;
		
		distances = new int[width * height];
		
		for(int j = 0; j < height; j++) {
			int yi = j * width;
			for(int i = 0; i < width; i++) {
				distances[i + yi] = Integer.MAX_VALUE;
			}
		}
		
		PriorityQueue<Integer> queue = new PriorityQueue<Integer>(width, new Comparator<Integer>() {
			@Override
			public int compare(Integer a, Integer b) {
				return distances[a] - distances[b];
			}
		});
		
		distances[((int) (target.x / tileSize)) + ((int) (target.y / tileSize)) * width] = 0;
		queue.add(((int) (target.x / tileSize)) + ((int) (target.y / tileSize)) * width);

		while(!queue.isEmpty()) {
			int c = queue.poll();
			int d = distances[c] + 1;
			
			int xx = c % width;
			int yy = c / width;
			
			if(xx > 0 && !tileWorld.get(xx - 1, yy) && distances[c - 1] > d) {
				distances[c - 1] = d;
				queue.add(c - 1);
			}
			
			if(xx < width - 1 && !tileWorld.get(xx + 1, yy) && distances[c + 1] > d) {
				distances[c + 1] = d;
				queue.add(c + 1);
			}
			
			if(yy > 0 && !tileWorld.get(xx, yy - 1) && distances[c - width] > d) {
				distances[c - width] = d;
				queue.add(c - width);
			}
			
			if(yy < height - 1 && !tileWorld.get(xx, yy + 1) && distances[c + width] > d) {
				distances[c + width] = d;
				queue.add(c + width);
			}
		}
	}
	
	public int getDistance(int x, int y) {
		return distances[x + y * width];
	}
	
	public Vector2d getDirectWaypoint(Vector2d position, double radius) {
		//Vector2d waypoint = new Vector2d(((int) (position.x / tileSize)) * tileSize, ((int) (position.y / tileSize)) * tileSize);
		//Vector2d waypoint = position;
		
		Vector2d waypoint = getWaypoint(position);
		
		for(int i = 0; i < 10; i++) {
			Vector2d next = getWaypoint(waypoint);
			if(clearPath(position, next, radius)) {
				waypoint = next;
			} else {
				break;
			}
		}
		
		return waypoint;
	}
	
	public Vector2d getWaypoint(Vector2d position) {
		if(target.distance(position) < tileSize * 1) return target;
		
		int x = (int) (position.x / tileSize);
		int y = (int) (position.y / tileSize);
		
		if(x >= 0 && x < width && y >= 0 && y < height) {		
			int d = distances[x + y * width];
			
			if(x > 0 && distances[(x - 1) + y * width] < d) {
				return new Vector2d((0.5 + x - 1) * tileSize, (0.5 + y) * tileSize);
			}
			
			if(x < width - 1 && distances[(x + 1) + y * width] < d) {
				return new Vector2d((0.5 + x + 1) * tileSize, (0.5 + y) * tileSize);
			}
			
			if(y > 0 && distances[x + (y - 1) * width] < d) {
				return new Vector2d((0.5 + x) * tileSize, (0.5 + y - 1) * tileSize);
			}
			
			if(y < height - 1 && distances[x + (y + 1) * width] < d) {
				return new Vector2d((0.5 + x) * tileSize, (0.5 + y + 1) * tileSize);
			}
		}
		
		return position;
	}
	
	public boolean clearPath(Vector2d start, Vector2d end, double radius) {
		Vector2d direction = new Vector2d(start);
		direction.sub(end);
		direction.normalize();
		
		// Rotate by 90 degrees
		double t = direction.x;
		direction.x = direction.y;
		direction.y = -t;
		
		Vector2d startLeft = start.plus(direction.times(radius));
		Vector2d startRight = start.minus(direction.times(radius));
	
		Vector2d endLeft = end.plus(direction.times(radius));
		Vector2d endRight = end.minus(direction.times(radius));
		
		return inSight(startLeft, endLeft) && inSight(startRight, endRight);
	}
	
	private boolean inSight(Vector2d start, Vector2d end) {
		// Assume start and end in the world
		if(start.x < 0 || start.y < 0 || start.x > width * tileSize || start.y > width * tileSize)
			return false;
		if(end.x < 0 || end.y < 0 || end.x > width * tileSize || end.y > width * tileSize)
			return false;
		
		Vector2d direction = new Vector2d(end);
		direction.sub(start);
		
		double targetDistance = direction.length();
		direction.normalize();
		direction.mul(tileSize * 0.1);
		
		Vector2d offset = new Vector2d();
		
		double invTileSize = 1.0 / tileSize;
		while(offset.length() < targetDistance) {
			offset.add(direction);
			
			int x = (int) ((start.x + offset.x) * invTileSize);
			int y = (int) ((start.y + offset.y) * invTileSize);
			
			if(distances[x + y * width] == Integer.MAX_VALUE) {
				return false;
			}
		}
		
		return true;
	}
}
