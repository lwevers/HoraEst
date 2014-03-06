package game;

import entity.Component;
import entity.Entity;
import entity.EntityComponent;
import math.Box;
import math.MovingAverage;
import math.MutableDouble;
import math.geom2d.Vector2d;

public class Components {
	public static final Component<Box> BOX = new Component<Box>();
	public static final Component<Double> COLLISION_RADIUS = new Component<Double>();
	public static final Component<Boolean> CROSSHAIR = new Component<Boolean>();
	public static final Component<Boolean> DESTROYED = new Component<Boolean>();
	public static final Component<Boolean> ENEMY = new Component<Boolean>();
	public static final Component<MutableDouble> LIFETIME = new Component<MutableDouble>();
	public static final Component<MovementLimiter> MOVEMENT_LIMIT = new Component<MovementLimiter>();
	public static final Component<Double> MOVEMENT_SPEED = new Component<Double>();
	public static final Component<Boolean> MOVE_FORWARD = new Component<Boolean>();
	public static final Component<EntityCollisionHandler> ON_ENTITY_COLLISION = new Component<EntityCollisionHandler>();
	public static final Component<TickHandler> ON_TICK = new Component<TickHandler>();
	public static final Component<WallCollisionHandler> ON_WALL_COLLISION = new Component<WallCollisionHandler>();
	public static final Component<Boolean> PUSHING = new Component<Boolean>();
	public static final Component<MovingAverage<Vector2d>> PUSHABLE = new Component<MovingAverage<Vector2d>>();
	public static final Component<Boolean> RENDERABLE = new Component<Boolean>();
	public static final Component<MutableDouble> ROTATION = new Component<MutableDouble>();
	public static final Component<Entity> TARGET = new Component<Entity>();
	public static final Component<Boolean> FIND_PATH = new Component<Boolean>();
	public static final Component<Spawn> SPAWN = new Component<Spawn>();
	
	public static void register(EntityComponent ec) {
		ec.register(BOX);
		ec.register(COLLISION_RADIUS);
		ec.register(CROSSHAIR);
		ec.register(DESTROYED);
		ec.register(ENEMY);
		ec.register(FIND_PATH);
		ec.register(LIFETIME);
		ec.register(MOVEMENT_LIMIT);
		ec.register(MOVEMENT_SPEED);
		ec.register(MOVE_FORWARD);
		ec.register(ON_ENTITY_COLLISION);
		ec.register(ON_TICK);
		ec.register(ON_WALL_COLLISION);
		ec.register(PUSHING);
		ec.register(PUSHABLE);
		ec.register(RENDERABLE);
		ec.register(ROTATION);
		ec.register(TARGET);
		ec.register(SPAWN);
		
	}
}
