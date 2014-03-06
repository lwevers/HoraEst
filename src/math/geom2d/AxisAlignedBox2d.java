package math.geom2d;


public class AxisAlignedBox2d {
	public double left;
	public double right;
	public double top;
	public double bottom;
	
	// left <= right
	// top <= bottom
	public AxisAlignedBox2d(double left, double right, double top, double bottom) {
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
	}
	
	public double distanceSquared(Vector2d p) {
		double dx = 0.0;
		double dy = 0.0;
		
		if(p.x < left) {
			dx = left - p.x;
		} else if(p.x > right) {
			dx = p.x - right;
		}
		
		if(p.y < top) {
			dy = top - p.y;
		} else if(p.y > bottom) {
			dy = p.y - bottom;
		}
		
		return dx * dx + dy * dy;
	}
	
	public boolean intersects(Line2d line) {
		throw new RuntimeException("Not implemented");
	}
}
