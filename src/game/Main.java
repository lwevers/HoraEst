package game;

import static game.Components.BOX;
import static game.Components.COLLISION_RADIUS;
import static game.Components.CROSSHAIR;
import static game.Components.DESTROYED;
import static game.Components.ENEMY;
import static game.Components.FIND_PATH;
import static game.Components.LIFETIME;
import static game.Components.MOVEMENT_SPEED;
import static game.Components.MOVE_FORWARD;
import static game.Components.ON_ENTITY_COLLISION;
import static game.Components.ON_TICK;
import static game.Components.ON_WALL_COLLISION;
import static game.Components.PUSHABLE;
import static game.Components.PUSHING;
import static game.Components.RENDERABLE;
import static game.Components.ROTATION;
import static game.Components.SPAWN;
import static game.Components.TARGET;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import javax.imageio.ImageIO;

import math.Box;
import math.MovingAverage;
import math.MutableDouble;
import math.geom2d.Vector2d;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import world.DistanceMap;
import world.TileWorld;
import collision.Collider;
import collision.Handler;
import collision.HashingCollider;
import entity.Entity;
import entity.EntityComponent;

public class Main {
	public static final int COLLISION_CELL_SIZE = 64;
	public static final double TILE_SIZE = 64.0;
	public static final double INV_TILE_SIZE = 1.0 / TILE_SIZE;
	
	private static TileWorld<Boolean> world;
	private static DistanceMap distanceMap = null;
	
	private static EntityComponent ec = new EntityComponent();
	private static Entity player;
	private static Entity base;
	
	private static Vector2d temp = new Vector2d();
	
	private static double frameRate = 0.0;
	private static double pathingTime = 0.0;
	private static double collisionDetectionTime = 0.0;
	private static double collisionResponseTime = 0.0;
	
	private static Queue<Runnable> deferred = new LinkedList<Runnable>();
	
	public static void main(String[] args) throws LWJGLException, IOException {	
		initInput();
		initRenderer();
		initGame("map.png");

		long last = System.nanoTime();
		double time = 0.0;

		while(!Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
			long now = System.nanoTime();
			double delta = ((double) (now - last)) / 1000000000;
			last = now;
			
			time += delta;
			if(time > 1.0) {
				time = 0.0;
				frameRate = 1.0 / delta;
			}
			
			update(delta);
			render(player);

			// This ensures there is no mouse lag with Vsync on
			//Display.sync(60);
		}
		
		Display.destroy();
	}
	
	public static void initGame(String file) throws IOException {
		Components.register(ec);
		
		BufferedImage bi = ImageIO.read(new File(file));

		int BLACK = 255 << 24;
		int RED = BLACK + (255 << 16);
		int GREEN = BLACK + (255 << 8);
		int BLUE = BLACK + 255;
		
		player = spawnPlayer(ec, new Vector2d());
		base = spawnBase(ec, new Vector2d());
		
		world = new TileWorld<Boolean>(bi.getWidth(), bi.getHeight());
		world.fill(false);
		for(int x = 0; x < bi.getWidth(); x++) {
			for(int y = 0; y < bi.getHeight(); y++) {
				int color = bi.getRGB(x, y);
				
				if(color == BLACK) {
					world.set(x, y, true);
				}
				
				if(color == GREEN) {
					Entity spawn = new Entity(ec);
					spawn.position.set(TILE_SIZE * x + TILE_SIZE / 2, TILE_SIZE * y + TILE_SIZE / 2);
					spawn.add(RENDERABLE, true);
					spawn.add(BOX, new Box(64, 64));
					spawn.add(SPAWN, new Spawn(0.0, 0.0));
				}
				
				if(color == RED) {
					base.position.set(TILE_SIZE * x + TILE_SIZE, TILE_SIZE * y + TILE_SIZE);
				}
				
				if(color == BLUE) {
					player.position.set(TILE_SIZE * x + TILE_SIZE / 2, TILE_SIZE * y + TILE_SIZE / 2);
				}
			}
		}
		
		distanceMap = new DistanceMap(world, TILE_SIZE, base.position);
	}
	
	public static void initInput() {
		Mouse.setGrabbed(true);
	}
	
	public static void initRenderer() throws LWJGLException {
		// Display.setDisplayMode(new DisplayMode(800, 600));
		Display.setFullscreen(true);
		Display.setVSyncEnabled(false);
		Display.create();
		
		GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		
		GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);
		GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, Display.getWidth(), 0, Display.getHeight(), 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	}
	
	static double counter = 0.0;
	public static void update(final double delta) {
		counter += delta;
		
		//if(counter > 0.1) {
			counter = 0.0;
			double pathingStart = time();
			for(Entity e : ec.where(FIND_PATH)) {
				e.get(ROTATION).value = distanceMap.getDirectWaypoint(e.position, e.get(COLLISION_RADIUS)).angle(e.position);
			}
			pathingTime = elapsed(pathingStart);
		//}
		
		for(Entity e : ec.where(MOVE_FORWARD)) {
			move(e, 0.0, e.get(MOVEMENT_SPEED) * delta);
		}
		
		if(!Keyboard.isKeyDown(Keyboard.KEY_Q)) {
			for(Entity e : ec.where(ON_TICK)) {
				e.get(ON_TICK).onTick(delta);
			}
		}
		
		if(Keyboard.isKeyDown(Keyboard.KEY_0)) {
			world.set((int) (player.position.x / TILE_SIZE), (int) (player.position.y / TILE_SIZE), true);
		}
		
		/*
		double collisionDetectionStart = time();
		Collider collider = new HashingCollider(COLLISION_CELL_SIZE, 12);
		for(Entity e : ec.where(COLLISION_RADIUS)) {
			collider.index(e);
		}
		collisionDetectionTime = elapsed(collisionDetectionStart);
		*/
		
		double collisionResponseStart = time();
		Collider collider = new HashingCollider(COLLISION_CELL_SIZE, 12);
		for(Entity e : ec.where(COLLISION_RADIUS)) {
			collider.index(e);
		}
		
		for(final Entity e : ec.where(ON_ENTITY_COLLISION)) {
			collider.query(e, new Handler<Entity>() {
				@Override
				public void handle(Entity o) {
					e.get(ON_ENTITY_COLLISION).handle(o);
				}
			});
		}
		
		for(int i = 0; i < 1; i++) {
			//collider = new HashingCollider(COLLISION_CELL_SIZE, 12);
			for(Entity e : ec.where(COLLISION_RADIUS)) {
				collider.index(e);
			}
			
			for(Entity e : ec.where(PUSHABLE)) {
				e.get(PUSHABLE).accumulator.set(e.position);
				e.get(PUSHABLE).count = 1.0;
			}		
			
			for(final Entity e : ec.where(COLLISION_RADIUS)) {
				final Vector2d position = e.position;
				final double radius = e.get(COLLISION_RADIUS);
				
				if(e.has(PUSHING)) {
					collider.query(e, new Handler<Entity>() {
						@Override
						public void handle(Entity o) {
							MovingAverage<Vector2d> ma = o.get(PUSHABLE);
							
							if(ma != null) {
								temp.set(o.position);
								temp.setDistance(position, radius + o.get(COLLISION_RADIUS));
								
								ma.accumulator.add(temp);
								ma.count += 1.0;
							}
						}
					});
				}
			}
			
			for(Entity e : ec.where(PUSHABLE)) {
				Vector2d p = e.get(PUSHABLE).accumulator;
				p.mul(1.0 / e.get(PUSHABLE).count);
				
				e.position.set(p);
				
				resolveWallCollision(e);
			}
		}
		collisionResponseTime = elapsed(collisionResponseStart);
		
		for(Entity e : ec.where(SPAWN)) {
			if(e.get(SPAWN).update(delta)) {
				spawnEnemy(ec, e.position, player);
			}
		}
		
		for(Entity e : ec.where(LIFETIME)) {
			e.get(LIFETIME).value -= delta;
			if(e.get(LIFETIME).value <= 0.0) {
				e.add(DESTROYED, true);
			}
		}
		
		while(!deferred.isEmpty()) {
			deferred.poll().run();
		}
		
		for(Iterator<Entity> iter = ec.where(DESTROYED).iterator(); iter.hasNext(); ) {
			Entity e = iter.next();
			iter.remove();
			e.destroy();
		}
	}
	
	public static void render(Entity camera) {
		double start = time();
		
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		double angleConversion = 180.0 / Math.PI;
		
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glPushMatrix();
			GL11.glTranslated(Display.getWidth() / 2, Display.getHeight() / 2, 0.0);
			GL11.glRotated(camera.get(ROTATION).value * angleConversion, 0.0, 0.0, 1.0);
			GL11.glTranslated(-camera.position.x, -camera.position.y, 0.0);
			
			double drawDistanceSquared = Display.getWidth() * Display.getWidth() * 0.25 + Display.getHeight() * Display.getHeight() * 0.25;
			double drawDistance = Math.sqrt(drawDistanceSquared);
			
			// Draw world

			int screenLeft = (int) ((camera.position.x - drawDistance) / TILE_SIZE);
			int screenRight = (int) (screenLeft + drawDistance * 2 / TILE_SIZE) + 1;
			int screenBottom = (int) ((camera.position.y - drawDistance) / TILE_SIZE);
			int screenTop = (int) (screenBottom + drawDistance * 2 / TILE_SIZE) + 1;
			
			if(screenLeft < 0) screenLeft = 0;
			if(screenRight >= world.getWidth()) screenRight = world.getWidth();
			if(screenBottom < 0) screenBottom = 0;
			if(screenTop >= world.getHeight()) screenTop = world.getHeight();
			
			//GL11.glColor3d(0.0, 0.0, 0.0);
			//GL11.glBegin(GL11.GL_QUADS);
			GL11.glColor3d(1.0, 1.0, 1.0);
			GL11.glBegin(GL11.GL_QUADS);
			for(int x = screenLeft; x < screenRight; x++) {
				for(int y = screenBottom; y < screenTop; y++) {
					
					if(!world.get(x, y)) {
						double left = x * TILE_SIZE;
						double right = left + TILE_SIZE;
						double top = y * TILE_SIZE;
						double bottom = top + TILE_SIZE;
		
						GL11.glVertex2d(left, top);
						GL11.glVertex2d(right, top);
						GL11.glVertex2d(right, bottom);
						GL11.glVertex2d(left, bottom);
					}
					
					/*
					int d = distanceMap.getDistance(x, y);
					
					double left = x * TILE_SIZE;
					double right = left + TILE_SIZE;
					double top = y * TILE_SIZE;
					double bottom = top + TILE_SIZE;
					
					double color = 1.0 - 0.02 * d;
					if(color < 0.0) color = 0.0;
					
					GL11.glColor3d(color, color, color);
					GL11.glBegin(GL11.GL_QUADS);
					GL11.glVertex2d(left, top);
					GL11.glVertex2d(right, top);
					GL11.glVertex2d(right, bottom);
					GL11.glVertex2d(left, bottom);
					GL11.glEnd();
					*/
				}
			}
			GL11.glEnd();
			
			
			GL11.glColor3d(0.0, 0.0, 0.0);
			GL11.glBegin(GL11.GL_LINES);
				for(int i = 0; i <= world.getWidth(); i++) {
					GL11.glVertex2d(i * TILE_SIZE, 0.0);
					GL11.glVertex2d(i * TILE_SIZE, TILE_SIZE * world.getHeight());
				}
				
				for(int i = 0; i <= world.getHeight(); i++) {
					GL11.glVertex2d(0.0, i * TILE_SIZE);
					GL11.glVertex2d(TILE_SIZE * world.getWidth(), i * TILE_SIZE);
				}
			GL11.glEnd();
			
			/*
			for(Entity e : ec.where(FIND_PATH)) {
				Vector2d waypoint = distanceMap.getDirectWaypoint(e.position, e.get(COLLISION_RADIUS));
			
				GL11.glColor4d(0.0, 0.0, 0.0, 0.2f);
				GL11.glBegin(GL11.GL_LINES);
					GL11.glVertex2d(e.position.x, e.position.y);
					GL11.glVertex2d(waypoint.x, waypoint.y);
				GL11.glEnd();
			}
			*/
			
			for(Entity e : ec.where(RENDERABLE)) {
				if(e.position.distanceSquared(camera.position) < drawDistanceSquared) {
					GL11.glPushMatrix();
					
					GL11.glTranslated(e.position.x, e.position.y, 0.0);
					
					if(e.has(ROTATION)) {
						GL11.glRotated(-e.get(ROTATION).value * angleConversion, 0.0, 0.0, 1.0);
					}
					
					if(e == camera) {
						GL11.glColor3f(0.0f, 0.0f, 1.0f);
					} else {
						int d = 0; // distanceMap.getDistance((int) (e.position.x / TILE_SIZE), (int) (e.position.y / TILE_SIZE));
						GL11.glColor3d(1.3 - 0.05 * d, 0.0, 1.3f - 0.05 * d);
					}
					
					if(e.has(BOX)) {
						Box box = e.get(BOX);
						
						double left   = 0.5 * -box.width;
						double right  = 0.5 *  box.width;
						double top    = 0.5 * -box.height;
						double bottom = 0.5 *  box.height;
						
						GL11.glBegin(GL11.GL_QUADS);
							GL11.glVertex2d(left, top);
							GL11.glVertex2d(right, top);
							GL11.glVertex2d(right, bottom);
							GL11.glVertex2d(left, bottom);
						GL11.glEnd();
					}
					
					if(e.has(CROSSHAIR)) {
						GL11.glColor4d(0.0, 0.0, 0.0, 0.2f);
						GL11.glBegin(GL11.GL_LINES);
							GL11.glVertex2d(0.0, 0.0);
							GL11.glVertex2d(0.0, 10000.0);
						GL11.glEnd();
					}
					
					GL11.glPopMatrix();
				}
			}
		GL11.glPopMatrix();
		
		GL11.glColor4d(1.0, 1.0, 1.0, 1.0);
		DrawText.drawString("framerate " + frameRate, 0, 0);
		DrawText.drawString("pathfinding " + pathingTime * 1000 + " ms", 0, 20);
		DrawText.drawString("collision detection " + collisionDetectionTime * 1000 + " ms", 0, 40);
		DrawText.drawString("collision response " + collisionResponseTime * 1000 + " ms", 0, 60);
		DrawText.drawString("render " + elapsed(start) * 1000 + " ms", 0, 80);
		
		Display.update();
		
	}

	public static void directMove(Entity e, double angle, double distance) {
		temp.fromAngle(angle + e.get(ROTATION).value);
		temp.mul(distance);
		e.position.add(temp);
	}
	
	public static void move(Entity e, double angle, double distance) {
		double max = e.get(COLLISION_RADIUS) * 0.5;
		
		boolean hasCollided = false;
		
		while(distance > max && !hasCollided) {
			directMove(e, angle, max);
			distance -= max;
			hasCollided = resolveWallCollision(e);
		}
		
		if(!hasCollided) {
			directMove(e, angle, distance);
			resolveWallCollision(e);
		}
	}
	
	public static boolean resolveWallCollision(Entity e) {
		boolean collision = false;
		
		double r = e.get(COLLISION_RADIUS);
		
		// Bound object within the world
		if(e.position.x - r < 0) {
			e.position.x = r;
			collision = true;
		} else if(e.position.x + r >= world.getWidth() * TILE_SIZE) {
			e.position.x = world.getWidth() * TILE_SIZE - r;
			collision = true;
		} else if(e.position.y - r < 0) {
			e.position.y = r;
			collision = true;
		} else if(e.position.y + r >= world.getHeight() * TILE_SIZE) {
			e.position.y = world.getHeight() * TILE_SIZE - r;
			collision = true;
		}
		
		// Determine in which tile the object is
		int x = (int) Math.floor(e.position.x * INV_TILE_SIZE);
		int y = (int) Math.floor(e.position.y * INV_TILE_SIZE);

		// Note: this does not work if the entity moves into the wall beyond its collision radius
		int xl = (int) Math.floor((e.position.x - r) * INV_TILE_SIZE);
		int xr = (int) Math.floor((e.position.x + r) * INV_TILE_SIZE);
		if(xl < 0 || (y >= 0 && y < world.getHeight() && xl < world.getWidth() && world.get(xl, y))) {
			e.position.x = TILE_SIZE * (xl + 1) + r;
			collision = true;
		} else {
			if(xr >= world.getWidth() || (y >= 0 && y < world.getHeight() && world.get(xr, y))) {
				e.position.x = TILE_SIZE * xr - r;
				collision = true;
			}
		}
		
		int yt = (int) Math.floor((e.position.y - r) * INV_TILE_SIZE);
		int yb = (int) Math.floor((e.position.y + r) * INV_TILE_SIZE);
		if(yt < 0 || (x >= 0 && x < world.getWidth() && yt < world.getHeight() && world.get(x, yt))) {
			e.position.y = TILE_SIZE * (yt + 1) + r;
			collision = true;
		} else {
			if(yb >= world.getHeight() || (x >= 0 && x < world.getWidth() && world.get(x, yb))) {
				e.position.y = TILE_SIZE * yb - r;
				collision = true;
			}
		}
		
		if(collision && e.has(ON_WALL_COLLISION)) {
			e.get(ON_WALL_COLLISION).handle();
		}
		
		if(!collision) {
			if(world.get(xl, yt)) {
				temp.set(x, y);
				temp.mul(TILE_SIZE);
				e.position.setDistanceIfCurrentlySmaller(temp, r);
			}
			
			if(world.get(xr, yt)) {
				temp.set(x + 1, y);
				temp.mul(TILE_SIZE);
				e.position.setDistanceIfCurrentlySmaller(temp, r);
			}
			
			if(world.get(xl, yb)) {
				temp.set(x, y + 1);
				temp.mul(TILE_SIZE);
				e.position.setDistanceIfCurrentlySmaller(temp, r);
			}
			
			if(world.get(xr, yb)) {
				temp.set(x + 1, y + 1);
				temp.mul(TILE_SIZE);
				e.position.setDistanceIfCurrentlySmaller(temp, r);
			}
		}
		
		return collision;
	}
	
	public static double time() {
		return 0.000000001 * System.nanoTime();
	}
	
	public static double elapsed(double start) {
		return time() - start;
	}
	
	public static Entity spawnPlayer(final EntityComponent ec, Vector2d position) {
		final Entity player = new Entity(ec);
		player.add(MOVEMENT_SPEED, 300.0);
		player.add(ROTATION, new MutableDouble(0.0));
		player.add(RENDERABLE, true);
		player.add(BOX, new Box(32, 16));
		player.add(CROSSHAIR, true);
		player.add(COLLISION_RADIUS, 24.0);
		player.add(PUSHING, true);
		player.add(ON_TICK, new TickHandler() {
			double remaining = 0.0;
			double cooldown = .0;
			
			@Override
			public void onTick(double delta) {
				player.get(ROTATION).value += 0.002 * Mouse.getDX();
				
				if(Keyboard.isKeyDown(Keyboard.KEY_UP) || Keyboard.isKeyDown(Keyboard.KEY_W)) {
					move(player, 0.0, delta * player.get(MOVEMENT_SPEED));
				}
				
				if(Keyboard.isKeyDown(Keyboard.KEY_DOWN) || Keyboard.isKeyDown(Keyboard.KEY_S)) {
					move(player, Math.PI, delta * player.get(MOVEMENT_SPEED));
				}
				
				if(Keyboard.isKeyDown(Keyboard.KEY_LEFT) || Keyboard.isKeyDown(Keyboard.KEY_A)) {
					move(player, -Math.PI / 2, delta * player.get(MOVEMENT_SPEED));
				}
				
				if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT) || Keyboard.isKeyDown(Keyboard.KEY_D)) {
					move(player, Math.PI / 2, delta * player.get(MOVEMENT_SPEED));
				}
				
				remaining -= delta;
				if(Mouse.isButtonDown(0)) {
					if(remaining <= 0.0) {
						for(int i = 0; i < 100; i++)
						spawnBullet(ec, player.position, Math.random() * 2 * Math.PI);
						remaining = cooldown;
					}
				}
			}
		});
		
		return player;
	}
	
	public static Entity spawnBase(EntityComponent ec, Vector2d position) {
		Entity base = new Entity(ec);
		base.add(RENDERABLE, true);
		base.add(BOX, new Box(128, 128));
		base.add(COLLISION_RADIUS, 64.0);
		base.add(ON_ENTITY_COLLISION, new EntityCollisionHandler() {
			@Override
			public void handle(Entity e) {
				if(e.has(ENEMY)) {
					e.add(DESTROYED, true);
				}
			}
		});
		
		return base;
	}
	
	public static Entity spawnBullet(final EntityComponent ec, Vector2d position, double rotation) {
		final Entity bullet = new Entity(ec);
		bullet.position.set(position);
		bullet.add(RENDERABLE, true);
		bullet.add(BOX, new Box(8, 8));
		bullet.add(MOVEMENT_SPEED, 1000.0);
		bullet.add(COLLISION_RADIUS, 8.0);
		bullet.add(MOVE_FORWARD, true);
		bullet.add(LIFETIME, new MutableDouble(2.0));
		bullet.add(ROTATION, new MutableDouble(rotation));
		bullet.add(ON_WALL_COLLISION, new WallCollisionHandler() {
			@Override
			public void handle() {
				bullet.add(DESTROYED, true);
				//bullet.get(ROTATION).value = Math.random() * Math.PI * 2;
			}
		});
		/*
		bullet.add(ON_TICK, new TickHandler() {
			@Override
			public void onTick(double delta) {
				bullet.get(ROTATION).value += (Math.random() - 0.5) * delta;
			}
		});
		*/
		bullet.add(ON_ENTITY_COLLISION, new EntityCollisionHandler() {
			@Override
			public void handle(Entity e) {
				if(e.has(ENEMY) && !e.has(DESTROYED)) {
					e.add(DESTROYED, true);
					bullet.add(DESTROYED, true);
				}
			}
		});
		return bullet;
	}
	
	public static Entity spawnEnemy(EntityComponent ec, Vector2d position, Entity target) {
		double size = 0.5; //Math.random() * 1.2 + 0.2;
		
		final Entity e = new Entity(ec);
		e.position.set(position);
		e.add(MOVEMENT_SPEED, 150.0 / size);
		e.add(MOVE_FORWARD, true);
		e.add(FIND_PATH, true);
		e.add(ROTATION, new MutableDouble(0.0));
		e.add(RENDERABLE, true);
		e.add(BOX, new Box(32 * size, 16 * size));
		e.add(TARGET, target);
		e.add(COLLISION_RADIUS, 24.0 * size);
		e.add(PUSHABLE, new MovingAverage<Vector2d>(new Vector2d(0.0, 0.0)));
		e.add(PUSHING, true);
		e.add(ENEMY, true);
		
		return e;
	}
}
