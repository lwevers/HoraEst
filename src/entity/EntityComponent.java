package entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EntityComponent {
	private Map<Component<?>, Set<Entity>> components = new HashMap<Component<?>, Set<Entity>>();
	
	public void register(Component<?> component) {
		components.put(component, new HashSet<Entity>());
	}
	
	public void index(Entity entity, Component<?> component) {
		components.get(component).add(entity);
	}
	
	public void remove(Entity entity, Component<?> component) {
		components.get(component).remove(entity);
	}
	
	public Set<Entity> where(Component<?> component) {
		return components.get(component);
	}
}
