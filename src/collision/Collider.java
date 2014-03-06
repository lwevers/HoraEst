package collision;

import entity.Entity;

public interface Collider {
	public void index(Entity entity);
	public void query(Entity entity, Handler<Entity> handler);
}
