package math;

public class MovingAverage<T> {
	public T accumulator;
	public double count;
	
	public MovingAverage(T accumulator) {
		this.accumulator = accumulator;
		this.count = 0.0;
	}
}
