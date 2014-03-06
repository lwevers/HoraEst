package math.geom2d;

public class Circle2d {
	public double x;
	public double y;
	public double radius;
	
	public Circle2d(double x, double y, double radius) {
		this.x = x;
		this.y = y;
		this.radius = radius;
	}
	
	public double distance(Vector2d p) {
		double dx = this.x - p.x;
		double dy = this.x - p.y;
		
		return Math.sqrt(dx * dx + dy * dy) - this.radius;
	}
	
	public double distance(Circle2d other) {
		double dx = this.x - other.x;
		double dy = this.y - other.y;
		
		return Math.sqrt(dx * dx + dy * dy) - this.radius - other.radius;
	}
	
	public boolean intersects(Circle2d other) {
		return distance(other) < 0;
	}
	
	public boolean intersects(double x, double y) {
		double dx = this.x - x;
		double dy = this.y - y;
		
		return dx * dx + dy * dy < this.radius * this.radius;
	}
}
