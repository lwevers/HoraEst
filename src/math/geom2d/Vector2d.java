package math.geom2d;

import math.FastMath;

public class Vector2d {
	public double x;
	public double y;
	
	public Vector2d() {
		this.x = 0.0;
		this.y = 0.0;
	}
	
	public Vector2d(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Vector2d(Vector2d v) {
		this.x = v.x;
		this.y = v.y;
	}

	public void set(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public void set(Vector2d source) {
		x = source.x;
		y = source.y;
	}
	
	public void add(Vector2d v) {
		x += v.x;
		y += v.y;
	}
	
	public Vector2d plus(Vector2d v) {
		return new Vector2d(x + v.x, y + v.y);
	}
	
	public void sub(Vector2d v) {
		x -= v.x;
		y -= v.y;
	}
	
	public Vector2d minus(Vector2d v) {
		return new Vector2d(x - v.x, y - v.y);
	}
	
	public void mul(double scalar) {
		x *= scalar;
		y *= scalar;
	}
	
	public Vector2d times(double scalar) {
		return new Vector2d(x * scalar, y * scalar);
	}
	
	public void div(Vector2d v) {
		x /= v.x;
		y /= v.y;
	}
	
	public double lengthSquared() {
		return x * x + y * y;
	}
	
	public double length() {
		return Math.sqrt(lengthSquared());
	}
	
	public void normalize() {
		mul(1.0 / length());
	}
	
	public double distance(Vector2d other) {
		return Math.sqrt(distanceSquared(other));
	}
	
	public double distanceSquared(Vector2d other) {
		double dx = other.x - x;
		double dy = other.y - y;
		return dx * dx + dy * dy;
	}
	
	public void fromAngle(double angle) {
		x = Math.sin(angle);
		y = Math.cos(angle);
	}
	
	public double toAngle() {
		return Math.acos(x / length());
		
		//return FastMath.atan2(x, y);  //Math.atan2(x, y);
	}
	
	public double angle(Vector2d other) {
		return FastMath.atan2(x - other.x, y - other.y);
	}
	
	public void rotate(double angle) {
		double a = toAngle() + angle;
		double l = length();
		fromAngle(a);
		mul(l);
	}
	
	// Sets 'a' a fixed distance from b, at the current angle
	public void setDistance(Vector2d v, double distance) {
		// a = normal(a - b) * distance + b
		double dx = x - v.x;
		double dy = y - v.y;
		
		double l = distance / Math.sqrt(dx * dx + dy * dy);
		
		x = dx * l + v.x;
		y = dy * l + v.y;
	}
	
	public void setDistanceIfCurrentlySmaller(Vector2d v, double distance) {
		double dx = x - v.x;
		double dy = y - v.y;
		double dd = dx * dx + dy * dy;
		
		if(dd < distance * distance) {
			double l = distance / Math.sqrt(dd);
		
			x = dx * l + v.x;
			y = dy * l + v.y;
		}
	}
}
