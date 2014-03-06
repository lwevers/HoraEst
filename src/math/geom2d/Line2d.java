package math.geom2d;


// http://stackoverflow.com/questions/99353/how-to-test-if-a-line-segment-intersects-an-axis-aligned-rectange-in-2d
// Line described by ax + by + c = 0
public class Line2d {
	public double a;
	public double b;
	public double c;
	
	public Line2d(double a, double b, double c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}
	
	public void set(double a, double b, double c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}
	
	public void through(Vector2d a, Vector2d b) {
		this.a = b.y - a.y;
		this.b = a.x - b.x;
		this.c = b.x * a.y - a.x * b.y;
	}
	
	// If result == 0, p is on the line
	// If result < 0, p is 'below' the line
	// If result > 0, p is 'above' the line
	public double classify(Vector2d p) {
		return a * p.x + b * p.y + c;
	}
}
