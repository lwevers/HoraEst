package game;

public class Spawn {
	public double remaining;
	public double cooldown;
	
	public Spawn(double initial, double cooldown) {
		this.remaining = initial;
		this.cooldown = cooldown;
	}
	
	public boolean update(double delta) {
		remaining -= delta;
		if(remaining <= 0.0) {
			remaining = cooldown;
			return true;
		}
		return false;
	}
}
