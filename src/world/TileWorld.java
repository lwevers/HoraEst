package world;

import java.util.Random;

public class TileWorld<T> {
	private final int width;
	private final int height;
	
	private final T[] tiles;
	
	@SuppressWarnings("unchecked")
	public TileWorld(int width, int height) {
		this.width = width;
		this.height = height;
		tiles = (T[]) new Object[width * height];
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public void set(int x, int y, T tile) {
		tiles[x + y * width] = tile;
	}
	
	public void fill(T tile) {
		for(int i = 0; i < tiles.length; i++) {
			tiles[i] = tile;
		}
	}
	
	public T get(int x, int y) {		
		return tiles[x + y * width];
	}
	
	public static <T> void randomize(TileWorld<T> world, T... tiles) {
		Random random = new Random();
		
		for(int x = 0; x < world.getWidth(); x++) {
			for(int y = 0; y < world.getHeight(); y++) {
				world.set(x, y, tiles[random.nextInt(tiles.length)]);
			}
		}
	}
}
