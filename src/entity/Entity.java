package entity;

import java.util.HashMap;
import java.util.Map;

import math.geom2d.Vector2d;

public class Entity {
	private Map<Component<?>, Object> componentValues = new HashMap<Component<?>, Object>();
	private EntityComponent entityComponent;
	
	public Vector2d position = new Vector2d(0.0, 0.0);
	public Entity processed = null;

	public Entity(EntityComponent ec) {
		this.entityComponent = ec;
	}
	
	public <T> boolean has(Component<T> component) {
		return componentValues.containsKey(component);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(Component<T> component) {
		return (T) componentValues.get(component);
	}
	
	public <T> void add(Component<T> component, T value) {
		componentValues.put(component, value);
		entityComponent.index(this, component);
	}
	
	public <T> void remove(Component<T> component) {
		componentValues.remove(component);
	}
	
	public void destroy() {
		for(Component<?> component : componentValues.keySet()) {
			entityComponent.remove(this, component);
		}
	}
}
