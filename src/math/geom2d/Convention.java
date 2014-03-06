package math.geom2d;

public interface Convention {
	// Implementations should encourage mutation
	// over instantiation to avoid garbage collection issues
	
	// Code concerning multiple classes should
	// be put in the largest class according to:
	// Vector2d < Line2d < Circle2d < AxisAlignedBox2d
}
