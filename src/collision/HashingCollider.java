package collision;

import static game.Components.COLLISION_RADIUS;

import java.util.ArrayList;
import java.util.List;

import math.geom2d.Vector2d;
import entity.Entity;

public class HashingCollider implements Collider {
	private double inverseCellSize;

	private List<ArrayList<Entity>> buckets;
	private int mask = 0x1;
	
	public HashingCollider(int cellSize, int bucketBits) {
		inverseCellSize = 1.0 / cellSize;
		
		for(int i = 1; i < bucketBits; i++) {
			mask |= mask << 1;
		}
		
		buckets = new ArrayList<ArrayList<Entity>>(mask + 1);
		
		for(int i = 0; i <= mask; i++) {
			buckets.add(i, new ArrayList<Entity>());
		}
	}
	
	@Override
	public void index(Entity entity) {
		Vector2d p = entity.position;
		double r = entity.get(COLLISION_RADIUS);
		
		int left 	= (int) ((p.x - r) * inverseCellSize);
		int right 	= (int) ((p.x + r) * inverseCellSize) + 1;
		int top 	= (int) ((p.y - r) * inverseCellSize);
		int bottom 	= (int) ((p.y + r) * inverseCellSize) + 1;
		
		for(int x = left; x < right; x++) {
			int hx = 47437549 * x;
			for(int y = top; y < bottom; y++) {
				int hash = (hx ^ 124543561 * y) & mask;
				buckets.get(hash).add(entity);
			}
		}
	}
	
	@Override
	public void query(Entity entity, Handler<Entity> handler) {
		Vector2d p = entity.position;
		double r = entity.get(COLLISION_RADIUS);
		
		int left 	= (int) ((p.x - r) * inverseCellSize);
		int right 	= (int) ((p.x + r) * inverseCellSize) + 1;
		int top 	= (int) ((p.y - r) * inverseCellSize);
		int bottom 	= (int) ((p.y + r) * inverseCellSize) + 1;

		entity.processed = entity;
		
		for(int x = left; x < right; x++) {
			int hx = 47437549 * x;
			for(int y = top; y < bottom; y++) {
				int hash = (hx ^ 124543561 * y) & mask;
				
				ArrayList<Entity> bucket = buckets.get(hash);
				
				for(int i = 0; i < bucket.size(); i++) {
					Entity other = bucket.get(i);
					if(other.processed != entity) {
						double rr = r + other.get(COLLISION_RADIUS);
						
						if(other.position.distanceSquared(p) < rr * rr) {
							handler.handle(other);
						}
						
						other.processed = entity;
					}
				}
			}
		}
	}

}
