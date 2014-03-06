package math;

public class Intersection {
	/**
	 * @requires left < right
	 * @requires top < bottom
	 * @return
	 */
	public double distanceSquaredAabbPoint(double left, double right, double top, double bottom, double x, double y) {
		double dx = 0.0;
		double dy = 0.0;
		
		if(x < left) {
			dx = left - x;
		} else if(x > right) {
			dx = x - right;
		}
		
		if(y < top) {
			dy = top - y;
		} else if(y > bottom) {
			dy = y - bottom;
		}
		
		return dx * dx + dy * dy;
	}
	
	public boolean intersectsAabbSphere(double left, double right, double top, double bottom, double x, double y, double radius) {
		return distanceSquaredAabbPoint(left, right, top, bottom, x, y) < radius * radius;
	}
}
