package com.gallantrealm.myworld.model;

/**
 * An animation is a special type of behavior used to cause repeated motion of an object or group of objects. The motion
 * is only known on the client, so it doesn't partake in physics.
 */
public abstract class WWAnimation extends WWBehavior {

	static final long serialVersionUID = 1L;

	transient WWObject owner;
	transient int timer;

	/**
	 * Returns the object that this animation is associated with.
	 */
	@Override
	public WWObject getOwner() {
		return owner;
	}

	/**
	 * Returns true if the animation is running client-side. Animations typically only occur on clients.
	 */
	@Override
	public boolean isOnClient() {
		return owner.world.isOnClient();
	}

	/**
	 * Allows the model matrix of an object to be modified before rendering.  This version happens
	 * before the model matrix has been used to position child objects child objects will also be impacted.
	 */
	public void preAnimateModelMatrix(WWObject object, WWMatrix modelMatrix, long time) {
	}

	/**
	 * Allows the model matrix of an object to be modified before rendering.  This version happens
	 * after the model matrix has been used to position child objects so it will not impact the rendering
	 * of child objects.
	 */
	public void postAnimateModelMatrix(WWObject object, WWMatrix modelMatrix, long time) {
	}

	/**
	 * Allows adjusting of the position according to animation. The object passed in is either the owner object or a
	 * child of the owner. The position vector is modified to reflect adjustment by the animation.
	 */
	public abstract void getAnimatedPosition(WWObject object, WWVector position, long time);

	/**
	 * Allows adjusting of the rotation according to animation. The object passed in is either the owner object or a
	 * child of the owner. The rotation quaternion is modified to reflect adjustment by the animation.
	 */
	public abstract void getAnimatedRotation(WWObject object, WWQuaternion rotation, long time);

}
