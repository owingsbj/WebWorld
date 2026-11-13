package com.gallantrealm.myworld.model.physics;

import java.util.ArrayList;
import java.util.List;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWVector;

/**
 * Manages a pool of reusable {@link ObjectCollision} instances to avoid object allocations
 * in the physics thread iterations.
 */
public class ObjectCollisionPool {

	private final ArrayList<ObjectCollision> pool = new ArrayList<ObjectCollision>();

	/**
	 * Obtains an {@link ObjectCollision} instance from the pool if available,
	 * or creates a new one if empty, initialized with the given arguments.
	 */
	public ObjectCollision obtain(WWObject firstObject, WWObject secondObject, WWVector overlapVector) {
		ObjectCollision collision;
		if (!pool.isEmpty()) {
			collision = pool.remove(pool.size() - 1);
			collision.firstObject = firstObject;
			collision.secondObject = secondObject;
			if (collision.overlapVector == null) {
				collision.overlapVector = overlapVector.clone();
			} else {
				collision.overlapVector.set(overlapVector);
			}
			collision.sliding = false;
			collision.stillSliding = false;
			collision.firstSlidingStreamId = 0;
			collision.secondSlidingStreamId = 0;
		} else {
			collision = new ObjectCollision(firstObject, secondObject, overlapVector);
		}
		return collision;
	}

	/**
	 * Recycles a single {@link ObjectCollision} back into the pool.
	 */
	public void recycle(ObjectCollision collision) {
		if (collision != null) {
			pool.add(collision);
		}
	}

	/**
	 * Recycles all {@link ObjectCollision} instances in the provided list into the pool.
	 */
	public void recycle(List<ObjectCollision> collisions) {
		if (collisions != null) {
			int size = collisions.size();
			for (int i = 0; i < size; i++) {
				pool.add(collisions.get(i));
			}
		}
	}

	/**
	 * Clears the pool.
	 */
	public void clear() {
		pool.clear();
	}
}
