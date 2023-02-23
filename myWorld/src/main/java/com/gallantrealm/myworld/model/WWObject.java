package com.gallantrealm.myworld.model;

import java.io.IOException;
import java.io.Serializable;

import com.gallantrealm.myworld.client.renderer.IRenderable;
import com.gallantrealm.myworld.client.renderer.IRendering;
import com.gallantrealm.myworld.client.renderer.IVideoTextureRenderer;
import com.gallantrealm.myworld.communication.DataInputStreamX;
import com.gallantrealm.myworld.communication.DataOutputStreamX;
import com.gallantrealm.myworld.communication.Sendable;

/**
 * This is the base class for all objects in the world. Note that not all fields of this class are shared with clients -- only those that are needed for
 * rendering the client. An editor for the object may expose other fields as well via editing, and fields are available for object behaviors (which run on the
 * server).
 */
public abstract class WWObject extends WWEntity implements IRenderable, Serializable, Cloneable, Sendable {
	static final long serialVersionUID = 1L;

	static final WWObject[] DEFAULT_CHILDREN = new WWObject[0];

	public static final float TORADIAN = 0.0174532925f;
	public static final float TODEGREES = 57.29577866f;

	int id;

	// Positioning properties
	public long lastMoveTime;
	private WWVector position = new WWVector();
	private WWQuaternion rotation = new WWQuaternion();
	public WWVector rotationPoint = new WWVector();

	// Grouping properties
	public int parentId;
	boolean gluedToParent = true;
	int[] childrenIds;
	WWObject[] children;

	// Physics properties
	public boolean physical;
	public boolean phantom;
	private WWVector velocity = new WWVector();
	private WWVector angularVelocity = new WWVector();
	public float density = 1.0f;
	public boolean solid = true;
	public float elasticity;
	public float friction = 0.9f;
	public boolean freedomMoveX = true;
	public boolean freedomMoveY = true;
	public boolean freedomMoveZ = true;
	public boolean freedomRotateX = true;
	public boolean freedomRotateY = true;
	public boolean freedomRotateZ = true;
	private WWVector thrust = new WWVector();
	private WWVector thrustVelocity = new WWVector();
	private WWVector torque = new WWVector();
	private WWVector torqueVelocity = new WWVector();

	public boolean pickable = false; // indicates that the object can be focused on
	public boolean penetratable = false; // indicates that camera is okay to penetrate into the object
	public boolean monolithic = false; // indicates that all surfaces of the object are similar textured, so all surfaces can be rendered together
	public boolean fixed; // indicates that the object never, ever moves
	public int group; // indicates this object is similar texture to other objects such that it can be rendered together

	public String sound;
	public float soundVolume;
	public float soundPitch;
	public transient int soundStreamId;
	public String impactSound;
	public String slidingSound;

	public WWVector size = new WWVector(1.0f, 1.0f, 1.0f);
	public float extent = 1.732f;
	public float extentx = 1.732f;
	public float extenty = 1.732f;
	public float extentz = 1.732f;
	public SideAttributes[] sideAttributes = new SideAttributes[NSIDES];

	// Behavior properties
	public BehaviorAttributes[] behaviors;

	// Transient (rendering related) values
	// Note: the Java3d rendering has a primitive within a transform group within a branch group.
	public transient IRendering rendering;
	public boolean renderit;
	public transient long lastRenderingTime;

	public boolean alwaysRender;

	public boolean shadowless;

	public WWAction[] actions;

	transient WWVector startPosition;
	transient WWVector startRotation;
	transient WWVector stopPosition;
	transient WWVector stopRotation;

	transient WWVector[] edgePoints;
	transient Object primitive;
	transient IVideoTextureRenderer videoTextureRenderer;

	// public transient boolean visible; // indicates that the object is visible to the viewer (local world only)

	public WWObject() {
		sideAttributes = new SideAttributes[NSIDES];
		for (int i = 0; i < NSIDES; i++) {
			sideAttributes[i] = SideAttributes.defaultSurface;
		}
	}

	public WWObject(float sizeX, float sizeY, float sizeZ) {
		this();
		this.size.x = sizeX;
		this.size.y = sizeY;
		this.size.z = sizeZ;
		calculateExtents();
	}

	/**
	 * This method streams the information that needs to be sent for an update or the object's state on the client.
	 * <p>
	 * When overriding this method in subclasses, be sure to invoke the parent method LAST. Also, implement the receive method to match, or communication will
	 * be corrupted.
	 */
	@Override
	public void send(DataOutputStreamX os) throws IOException {
		os.writeInt(id);
		os.writeKnownObject(size);
		os.writeFloat(extent);
		os.writeFloat(extentx);
		os.writeFloat(extenty);
		os.writeFloat(extentz);
		os.writeKnownObject(rotationPoint);
		os.writeKnownObjectArray(sideAttributes);
		os.writeInt(parentId);
		os.writeBoolean(gluedToParent);
		os.writeKnownObjectArray(children);
		os.writeBoolean(physical);
		os.writeBoolean(phantom);
		os.writeFloat(density);
		os.writeFloat(elasticity);
		os.writeFloat(friction);
		os.writeBoolean(solid);
		os.writeBoolean(freedomMoveX);
		os.writeBoolean(freedomMoveY);
		os.writeBoolean(freedomMoveZ);
		os.writeBoolean(freedomRotateX);
		os.writeBoolean(freedomRotateY);
		os.writeBoolean(freedomRotateZ);
		os.writeBoolean(pickable);
		os.writeBoolean(penetratable);
		os.writeBoolean(monolithic);
		os.writeBoolean(fixed);
		os.writeInt(group);
		os.writeString(sound);
		os.writeFloat(soundVolume);
		os.writeFloat(soundPitch);
		os.writeString(impactSound);
		os.writeString(slidingSound);
		os.writeBoolean(alwaysRender);
//		os.writeKnownObjectArray(behaviors);
		// TODO improve efficiency of behavior streaming
		sendPositionPrivate(os);
		super.send(os);
	}

	public void sendPosition(DataOutputStreamX os) throws IOException {
		sendPositionPrivate(os);
		super.send(os);
	}

	final void sendPositionPrivate(DataOutputStreamX os) throws IOException {
		os.writeKnownObject(position);
		os.writeKnownObject(rotation);
		os.writeKnownObject(velocity);
		os.writeKnownObject(angularVelocity);
		os.writeKnownObject(thrust);
		os.writeKnownObject(thrustVelocity);
		os.writeKnownObject(torque);
		os.writeKnownObject(torqueVelocity);
		os.writeKnownObject(stopPosition);
		os.writeKnownObject(stopRotation);
		os.writeLong(lastMoveTime);
	}

	/**
	 * This method receives streamed information sent from a server for an update to the object.
	 * <p>
	 * When overriding this method in subclasses, be sure to invoke the parent method LAST.
	 *
	 * @throws ClassNotFoundException
	 */
	@Override
	public void receive(DataInputStreamX is) throws IOException {
		id = is.readInt();
		size = (WWVector)is.readKnownObject(WWVector.class);
		extent = is.readFloat();
		extentx = is.readFloat();
		extenty = is.readFloat();
		extentz = is.readFloat();
		rotationPoint = (WWVector)is.readKnownObject(WWVector.class);
		sideAttributes = (SideAttributes[]) is.readKnownObjectArray(SideAttributes.class);
		parentId = is.readInt();
		gluedToParent = is.readBoolean();
		children = (WWObject[]) is.readKnownObjectArray(WWObject.class);
		physical = is.readBoolean();
		phantom = is.readBoolean();
		density = is.readFloat();
		elasticity = is.readFloat();
		friction = is.readFloat();
		solid = is.readBoolean();
		freedomMoveX = is.readBoolean();
		freedomMoveY = is.readBoolean();
		freedomMoveZ = is.readBoolean();
		freedomRotateX = is.readBoolean();
		freedomRotateY = is.readBoolean();
		freedomRotateZ = is.readBoolean();
		pickable = is.readBoolean();
		penetratable = is.readBoolean();
		monolithic = is.readBoolean();
		fixed = is.readBoolean();
		group = is.readInt();
		sound = is.readString();
		soundVolume = is.readFloat();
		soundPitch = is.readFloat();
		impactSound = is.readString();
		slidingSound = is.readString();
		alwaysRender = is.readBoolean();
//		behaviors = (BehaviorAttributes[]) is.readKnownObjectArray(BehaviorAttributes.class);
//		if (behaviors != null) {
//			// Need to set owner in behavior objects, since they were (re)created on receiving
//			for (int i = 0; i < behaviors.length; i++) {
//				behaviors[i].behavior.owner = this;
//			}
//		}
		receivePositionPrivate(is);
		super.receive(is);
	}

	public void receivePosition(DataInputStreamX is) throws IOException, ClassNotFoundException {
		receivePositionPrivate(is);
		super.receive(is);
	}

	final void receivePositionPrivate(DataInputStreamX is) throws IOException {
		position  = (WWVector)is.readKnownObject(WWVector.class);
		rotation = (WWQuaternion)is.readKnownObject(WWQuaternion.class);
		velocity = (WWVector)is.readKnownObject(WWVector.class);
		angularVelocity = (WWVector)is.readKnownObject(WWVector.class);
		thrust = (WWVector)is.readKnownObject(WWVector.class);
		thrustVelocity = (WWVector)is.readKnownObject(WWVector.class);
		torque = (WWVector)is.readKnownObject(WWVector.class);
		torqueVelocity = (WWVector)is.readKnownObject(WWVector.class);
		stopPosition = (WWVector) is.readKnownObject(WWVector.class);
		stopRotation = (WWVector) is.readKnownObject(WWVector.class);
		lastMoveTime = is.readLong();
	}

	public final int getId() {
		return id;
	}

	/**
	 * Overridden to set world on behaviors (if any)
	 */
	@Override
	public void setWorld(WWWorld world) {
		super.setWorld(world);
		if (behaviors != null) {
			for (int i = 0; i < behaviors.length; i++) {
				behaviors[i].behavior.owner = this;
			}
		}
	}

	public final long getLastMoveTime() {
		return lastMoveTime;
	}

	// TODO make this private or atleast protected (set internally)
	public void setLastMoveTime(long lastMoveTime) {
		this.lastMoveTime = lastMoveTime;
	}

	/**
	 * Returns the rotation including contribution from parents
	 */
	public final WWQuaternion getAbsoluteRotation(long worldTime) {
		WWQuaternion r = new WWQuaternion();
		getRotation(r);
		int p = parentId;
		if (p != 0) {
			WWObject parent = world.objects[p];
			WWQuaternion parentRotation = parent.getAbsoluteRotation(worldTime);
			r.rotate(parentRotation);
		}
		return r;
	}

	/**
	 * Returns the rotation including contribution from parents
	 */
	public final WWQuaternion getAbsoluteRotation(WWQuaternion r, long worldTime) {
		getRotation(r);
		int p = parentId;
		if (p != 0) {
			WWObject parent = world.objects[p];
			WWQuaternion parentRotation = parent.getAbsoluteRotation(worldTime);
			r.rotate(parentRotation);
		}
		return r;
	}

	/**
	 * Returns the animation rotation including contribution from parents
	 */
	public final WWQuaternion getAbsoluteAnimatedRotation(long worldTime) {
		WWQuaternion r = new WWQuaternion();
		getAnimatedRotation(r, worldTime);
		int p = parentId;
		if (p != 0) {
			WWObject parent = world.objects[p];
			WWQuaternion parentRotation = parent.getAbsoluteAnimatedRotation(worldTime);
			r.rotate(parentRotation);
		}
		return r;
	}

	public final WWQuaternion getAbsoluteAnimatedRotation(WWQuaternion r, long worldTime) {
		getAnimatedRotation(r, worldTime);
		int p = parentId;
		if (p != 0) {
			WWObject parent = world.objects[p];
			WWQuaternion parentRotation = parent.getAbsoluteAnimatedRotation(worldTime);
			r.rotate(parentRotation);
		}
		return r;
	}

	public final WWQuaternion getRotation() {
		return rotation;
	}

	public final void getRotation(WWQuaternion r) {
		this.rotation.copyInto(r);
	}

	public final void getAnimatedRotation(WWQuaternion r, long worldTime) {
		getRotation(r);
		processRotationAnimators(this, r, worldTime);
	}

	/**
	 * Invoke animators for rotation for this object and its parent(s).
	 */
	final void processRotationAnimators(WWObject object, WWQuaternion r, long worldTime) {
		if (behaviors != null) {
			for (int i = 0; i < behaviors.length; i++) {
				WWBehavior behavior = behaviors[i].behavior;
				if (behavior instanceof WWAnimation) {
					WWAnimation animation = (WWAnimation) behavior;
					animation.getAnimatedRotation(object, r, worldTime);
				}
			}
		}
		if (parentId != 0) {
			world.objects[parentId].processRotationAnimators(object, r, worldTime);
		}
	}

	// TODO this can't be supported with quaternions.  Replace with slerp?
//	public final boolean isAtRotationBounds() {
//		long worldTime = getWorldTime();
//		float deltaTime = (worldTime - lastMoveTime) / 1000.0f;
//		float newRotationX = rotationX + deltaTime * aMomentumX;
//		float newRotationY = rotationY + deltaTime * aMomentumY;
//		float newRotationZ = rotationZ + deltaTime * angularVelocityZ;
//		if (startRotation != null) {
//			if (rotationX > startRotation.x && newRotationX < startRotation.x) {
//				return true;
//			} else if (rotationX < startRotation.x && newRotationX > startRotation.x) {
//				return true;
//			}
//			if (rotationY > startRotation.y && newRotationY < startRotation.y) {
//				return true;
//			} else if (rotationY < startRotation.y && newRotationY > startRotation.y) {
//				return true;
//			}
//			if (rotationZ > startRotation.z && newRotationZ < startRotation.z) {
//				return true;
//			} else if (rotationZ < startRotation.z && newRotationZ > startRotation.z) {
//				return true;
//			}
//		}
//		if (stopRotation != null) {
//			if (rotationX > stopRotation.x && newRotationX < stopRotation.x) {
//				return true;
//			} else if (rotationX < stopRotation.x && newRotationX > stopRotation.x) {
//				return true;
//			}
//			if (rotationY > stopRotation.y && newRotationY < stopRotation.y) {
//				return true;
//			} else if (rotationY < stopRotation.y && newRotationY > stopRotation.y) {
//				return true;
//			}
//			if (rotationZ > stopRotation.z && newRotationZ < stopRotation.z) {
//				return true;
//			} else if (rotationZ < stopRotation.z && newRotationZ > stopRotation.z) {
//				return true;
//			}
//		}
//		return false;
//	}

	public final void setRotation(float pitch, float roll, float yaw) {
		setOrientation(position, new WWQuaternion(pitch, roll, yaw), null, null, getWorldTime());
	}

	public final void setRotation(float[] rots) {
		setRotation(rots[0], rots[1], rots[2]);
	}

	public final void setRotation(WWQuaternion rotation) {
		setOrientation(position, rotation, null, null, getWorldTime());
	}

	public final WWVector getRotationPoint() {
		return rotationPoint;
	}

	public final void getRotationPoint(WWVector r) {
		rotationPoint.copyInto(r);
	}

	public final void setRotationPoint(float[] rots) {
		this.rotationPoint.x = rots[0];
		this.rotationPoint.y = rots[1];
		this.rotationPoint.z = rots[2];
	}

	public final void setRotationPoint(WWVector rotationPoint) {
		this.rotationPoint = rotationPoint;
	}

	public final WWVector getAbsolutePosition(long worldTime) {
		WWVector position = new WWVector();
		getAbsolutePosition(position, worldTime);
		return position;
	}

	public final WWVector getPosition() {
		return position;
	}

	public final void getPosition(WWVector position) {
		this.position.copyInto(position);
	}

	transient long lastGetAbsolutePositionTime = -1;
	transient float lastGetAbsolutePositionX, lastGetAbsolutePositionY, lastGetAbsolutePositionZ;

	/**
	 * Returns the position in real space, adding in parent position
	 */
	public final void getAbsolutePosition(WWVector position, long worldTime) {
		if (lastGetAbsolutePositionTime == worldTime) {
			position.x = lastGetAbsolutePositionX;
			position.y = lastGetAbsolutePositionY;
			position.z = lastGetAbsolutePositionZ;
		} else {
			getPosition(position);
			// if this is a member of a collection, adjust position according to parent's position/rotation
			if (parentId != 0) {
				WWObject parent = world.objects[parentId];
				WWVector parentPosition = new WWVector();
				WWQuaternion parentRotation = new WWQuaternion();
				parent.getAbsolutePosition(parentPosition, worldTime);
				parent.getRotation(parentRotation);
				WWVector parentRotationPoint = parent.getRotationPoint();
				// parent.antiTransform(position, parent.getPosition(lastMoveTime), parent.getRotation(lastMoveTime));
				transform(position, parentPosition, parentRotation, parentRotationPoint);
			}
			lastGetAbsolutePositionX = position.x;
			lastGetAbsolutePositionY = position.y;
			lastGetAbsolutePositionZ = position.z;
			lastGetAbsolutePositionTime = worldTime;
		}
	}

	/**
	 * Returns the animated position in real space, adding in parent position
	 */
	public final void getAbsoluteAnimatedPosition(WWVector position, long worldTime) {
		getAnimatedPosition(position, worldTime);
		// if this is a member of a collection, adjust position according to parent's position/rotation
		if (parentId != 0) {
			WWObject parent = world.objects[parentId];
			WWVector parentPosition = new WWVector();
			WWQuaternion parentRotation = new WWQuaternion();
			parent.getAbsoluteAnimatedPosition(parentPosition, worldTime);
			parent.getAbsoluteAnimatedRotation(parentRotation, worldTime);
			WWVector parentRotationPoint = parent.getRotationPoint();
			// parent.antiTransform(position, parent.getPosition(lastMoveTime), parent.getRotation(lastMoveTime));
			transform(position, parentPosition, parentRotation, parentRotationPoint);
		}
	}

	public final void getAnimatedPosition(WWVector position, long worldTime) {
		getPosition(position);
		processPositionAnimators(this, position, worldTime);
	}

	/**
	 * Invoke animators for position for this object and its parent(s).
	 */
	final void processPositionAnimators(WWObject object, WWVector position, long worldTime) {
		if (behaviors != null) {
			for (int i = 0; i < behaviors.length; i++) {
				WWBehavior behavior = behaviors[i].behavior;
				if (behavior instanceof WWAnimation) {
					WWAnimation animation = (WWAnimation) behavior;
					animation.getAnimatedPosition(object, position, worldTime);
				}
			}
		}
		if (parentId != 0) {
			world.objects[parentId].processPositionAnimators(object, position, worldTime);
		}
	}

	public final void setPosition(WWVector position) {
		setOrientation(position, rotation, null, null, getWorldTime());
	}

	public final void setPosition(float x, float y, float z) {
		setOrientation(new WWVector(x, y, z), rotation, null, null, getWorldTime());
	}

	public final void setPosition(float[] pos) {
		setPosition(pos[0], pos[1], pos[2]);
	}

	public final void setStartPosition(WWVector v) {
		startPosition = v.clone();
	}

	public final WWVector getStartPosition() {
		return startPosition.clone();
	}

	public final void setStartRotation(WWVector v) {
		startRotation = v.clone();
	}

	public final WWVector getStartRotation() {
		return startRotation.clone();
	}

	public final void setStopPosition(WWVector v) {
		stopPosition = v.clone();
	}

	public final WWVector getStopPosition() {
		return stopPosition.clone();
	}

	public final void setStopRotation(WWVector v) {
		stopRotation = v.clone();
	}

	public final WWVector getStopRotation() {
		return stopRotation.clone();
	}

	public final boolean isPhysical() {
		return physical;
	}

	/**
	 * A physical object is influenced by gravity and by the interaction of other objects. (An object does NOT need to be physical to influence other objects,
	 * however. By making an object physical, it will use time on the server and client to simulate the interactions with other objects.)
	 */
	public final void setPhysical(boolean physical) {
		this.physical = physical;
	}

	public final WWVector getVelocity() {
		return velocity;
	}

	public final void getVelocity(WWVector velocity) {
		this.velocity.copyInto(velocity);
	}

	public final void setVelocity(WWVector velocity) {
		setOrientation(position, rotation, velocity, null, getWorldTime());
	}

	public final float getVelocityLength() {
		return (float) Math.sqrt(velocity.x * velocity.x + velocity.y * velocity.y + velocity.z * velocity.z);
	}

	public final WWVector getAngularVelocity() {
		return angularVelocity;
	}

	public final void getAngularVelocity(WWVector aVelocity) {
		this.angularVelocity.copyInto(aVelocity);
	}

	public final void setAngularVelocity(WWVector aVelocity) {
		setOrientation(position, rotation, null, aVelocity, getWorldTime());
	}

	public final float getAngularVelocityLength() {
		return (float) Math.sqrt(angularVelocity.x * angularVelocity.x + angularVelocity.y * angularVelocity.y + angularVelocity.z * angularVelocity.z);
	}

	public final WWVector getThrust() {
		return thrust;
	}

	public final void setThrust(WWVector thrust) {
		this.thrust = thrust;
	}

	public final WWVector getThrustVelocity() {
		return thrustVelocity;
	}

	public final void setThrustVelocity(WWVector thrustVelocity) {
		this.thrustVelocity = thrustVelocity;
	}

	public final WWVector getTorque() {
		return torque;
	}

	public final void setTorque(WWVector torque) {
		this.torque = torque;
	}

	public final WWVector getTorqueVelocity() {
		return torqueVelocity;
	}

	public final void setTorqueVelocity(WWVector torqueVelocity) {
		this.torqueVelocity = torqueVelocity;
	}

	/**
	 * Use this method to set all position, rotation, and momentum values in one call. This avoid undo overhead from detection of updated objects. Note that
	 * individual setPosition, setRotation, setVelocity, and setAMomentum calls will internally call this method.
	 */
	public final void setOrientation(WWVector newPosition, WWQuaternion newRotation, WWVector newVelocity, WWVector newAMomentum, long newMoveTime) {

		/*
		 * ignoring glue behavior from child to parent
		 */
//		// If this is a child and the child is glued to the parent, orient the parent
//		// instead. It will move all of its children accordingly.
//		if (parentId != 0 && gluedToParent && world != null) {
//			WWVector deltaPosition = newPosition.clone();
//			deltaPosition.subtract(getPosition(lastMoveTime));
//			WWVector deltaVelocity = newVelocity.clone();
//		  	deltaVelocity.subtract(getVelocity());
//		  	WWQuaternion deltaRotation = newRotation.clone();
//		  	deltaRotation.subtract(getRotation(lastMoveTime));
//		  	WWVector deltaAMomentum = newAMomentum.clone();
//			deltaAMomentum.subtract(getAMomentum());
//			WWObject parent = world.objects[parentId];
//			WWVector  newParentPosition = parent.getPosition(lastMoveTime);
//			newParentPosition.add(deltaPosition);
//			WWVector newParentVelocity = parent.getVelocity(); // TODO rotate/position parent relative to child delta rotate
//			parent.setOrientation(newParentPosition, newRotation, newParentVelocity, newAMomentum, newMoveTime); // TODO determine if some dynamic properties should be ignored for glued children return;
//		}
//
//		WWVector[] savedChildPositions = null;
//		if (childrenIds != null && world != null) {
//
//			// Remember the position of the contained objects relative to the old
//			// parent's position. The child positions will be updated later after the parent
//			// has been moved
//			savedChildPositions = new WWVector[childrenIds.length];
//			for (int i = 0; i < childrenIds.length; i++) {
//				WWObject child = world.objects[childrenIds[i]];
//				if (child != null) {
//					// adjust the child position based on the rotation of the parent
//					WWVector childPosition = child.getPosition(lastMoveTime);
//					antiTransform(childPosition, getPosition(lastMoveTime), getRotation(lastMoveTime), lastMoveTime);
//					savedChildPositions[i] = childPosition;
//				}
//			}
//
//			// Rotate the contained objects
//			WWVector deltaRotation = newRotation.clone();
//			deltaRotation.subtract(getRotation(lastMoveTime));
//			for (int i = 0; i < childrenIds.length; i++) {
//				WWObject child = world.objects[childrenIds[i]];
//				if (child != null) {
//					// adjust the child rotation to match the amount that the parent will rotate
//					WWVector newChildRotation = child.getRotation(lastMoveTime);
//					newChildRotation.add(deltaRotation);
//					child.rotationX = newChildRotation.x;
//					child.rotationY = newChildRotation.y;
//					child.rotationZ = newChildRotation.z;
//					child.lastMoveTime = newMoveTime;
//				}
//			}
//
//		}

		newPosition.copyInto(this.position);
		this.lastGetAbsolutePositionTime = -1;
		newRotation.copyInto(this.rotation);
		if (newVelocity != null) {
			newVelocity.copyInto(this.velocity);
		}
		if (newAMomentum != null) {
			newAMomentum.copyInto(this.angularVelocity);
		}
		this.lastMoveTime = newMoveTime;

//		if (childrenIds != null && world != null) {
//			// Update the position of the contained objects relative to the new
//			// parent's position.
//			for (int i = 0; i < childrenIds.length; i++) {
//				WWObject child = world.objects[childrenIds[i]];
//				if (child != null) {
//					// adjust the child position based on the rotation of the parent
//					WWVector childPosition = savedChildPositions[i];
//					transform(childPosition, getPosition(newMoveTime), getRotation(newMoveTime), newMoveTime);
//					child.positionX = childPosition.x;
//					child.positionY = childPosition.y;
//					child.positionZ = childPosition.z;
//					child.lastMoveTime = newMoveTime;
//				}
//			}
//		}

		// Since extents for fixed objects are tuned to current rotation, recalculate them
		if (fixed && !phantom) {
			calculateExtents();
		}
	}

	public final float getDensity() {
		return density;
	}

	public final void setDensity(float density) {
		this.density = density;
	}

	public final float getElasticity() {
		return elasticity;
	}

	public final void setElasticity(float elasticity) {
		this.elasticity = elasticity;
	}

	public final float getFriction() {
		return friction;
	}

	public final void setFriction(float friction) {
		this.friction = friction;
	}

	public final boolean isSolid() {
		return solid;
	}

	public final void setSolid(boolean solid) {
		this.solid = solid;
	}

	public final boolean isFreedomMoveX() {
		return freedomMoveX;
	}

	public final void setFreedomMoveX(boolean freedomMoveX) {
		this.freedomMoveX = freedomMoveX;
	}

	public final boolean isFreedomMoveY() {
		return freedomMoveY;
	}

	public final void setFreedomMoveY(boolean freedomMoveY) {
		this.freedomMoveY = freedomMoveY;
	}

	public final boolean isFreedomMoveZ() {
		return freedomMoveZ;
	}

	public final void setFreedomMoveZ(boolean freedomMoveZ) {
		this.freedomMoveZ = freedomMoveZ;
	}

	public final void setFreedomMove(boolean[] freedoms) {
		this.freedomMoveX = freedoms[0];
		this.freedomMoveY = freedoms[1];
		this.freedomMoveZ = freedoms[2];
	}

	public final boolean isFreedomRotateX() {
		return freedomRotateX;
	}

	public final void setFreedomRotateX(boolean freedomRotateX) {
		this.freedomRotateX = freedomRotateX;
	}

	public final boolean isFreedomRotateY() {
		return freedomRotateY;
	}

	public final void setFreedomRotateY(boolean freedomRotateY) {
		this.freedomRotateY = freedomRotateY;
	}

	public final boolean isFreedomRotateZ() {
		return freedomRotateZ;
	}

	public final void setFreedomRotateZ(boolean freedomRotateZ) {
		this.freedomRotateZ = freedomRotateZ;
	}

	public final void setFreedomRotate(boolean[] freedoms) {
		this.freedomRotateX = freedoms[0];
		this.freedomRotateY = freedoms[1];
		this.freedomRotateZ = freedoms[2];
	}

	/**
	 * If true, the object can be picked (usually by clicking on it).
	 */
	public final boolean isPickable() {
		return pickable;
	}

	public final void setPickable(boolean pickable) {
		this.pickable = pickable;
	}

	/**
	 * If true, the camera is allowed to penetrate the object. Defaults to true.
	 */
	public final boolean isPenetratable() {
		return penetratable;
	}

	public final void setPenetratable(boolean penetratable) {
		this.penetratable = penetratable;
	}

	/**
	 * If true, the object is considered to have similar properties for all surfaces. This can speed up drawing of an object by allowing the drawing of all
	 * surface to be combined into a single call to the graphics engine.
	 */
	public final boolean isMonolithic() {
		return monolithic;
	}

	public final void setMonolithic(boolean monolithic) {
		this.monolithic = monolithic;
		updateRendering();
	}

	/**
	 * A sound that is always played by the object.
	 */
	public final String getSound() {
		return sound;
	}

	public final void setSound(String sound, float volume, float pitch) {
		this.sound = sound;
		this.soundVolume = volume;
		this.soundPitch = pitch;
		updateSound();
	}

	public final void updateSound() {
		if (sound != null && soundVolume > 0) {
			WWVector absolutePosition = new WWVector();
			getAbsolutePosition(absolutePosition, world.getWorldTime());
			if (soundStreamId == 0) {
				soundStreamId = world.rendering.getRenderer().getSoundGenerator().startPlayingSound(getSound(), 2, absolutePosition, soundVolume, soundPitch);
			} else {
				soundStreamId = world.rendering.getRenderer().getSoundGenerator().adjustPlayingSound(soundStreamId, absolutePosition, soundVolume, soundPitch);
			}
		} else {
			if (soundStreamId != 0) {
				world.rendering.getRenderer().getSoundGenerator().stopPlayingSound(soundStreamId);
				soundStreamId = 0;
			}
		}
	}

	/**
	 * A sound that is played when something hits the object.
	 */
	public final String getImpactSound() {
		return impactSound;
	}

	public final void setImpactSound(String impactSound) {
		this.impactSound = impactSound;
	}

	/**
	 * A sound that is played when something moves across/through the object.
	 */
	public final String getSlidingSound() {
		return slidingSound;
	}

	public final void setSlidingSound(String slidingSound) {
		this.slidingSound = slidingSound;
	}

	/**
	 * Returns true if the object is in motion in any way. For WWObject, this means moving or rotating. If the object is a member of a collection and the
	 * collection is dynamic, the object is also dynamic. Subclasses can add additional property tests that indicate the object is dynamically changing in some
	 * way.
	 */
	public final boolean isDynamic() {
		if (sideAttributes != null) {
			for (int side = 0; side < NSIDES; side++) {
				if (sideAttributes[side] != null && sideAttributes[side] != SideAttributes.defaultSurface) {
					float textureVelocityX = sideAttributes[side].texture.velocityX;
					float textureVelocityY = sideAttributes[side].texture.velocityY;
					float textureAMomentum = sideAttributes[side].texture.aMomentum;
					if (textureVelocityX != 0 || textureVelocityY != 0 || textureAMomentum != 0) {
						return true;
					}
				}
			}
		}
		boolean dynamic = velocity.x != 0.0 || velocity.y != 0.0 || velocity.z != 0.0 || angularVelocity.z != 0.0 || angularVelocity.y != 0.0 || angularVelocity.z != 0.0;
		if (!dynamic && world != null && parentId != 0) {
			WWObject parent = world.objects[parentId];
			dynamic = parent.isDynamic();
		}
		return dynamic;
	}

	public final long getLastRenderingTime() {
		return lastRenderingTime;
	}

	@Override
	public final IRendering getRendering() {
		return rendering;
	}

	@Override
	public void updateRendering() {
		if (rendering != null) {
			rendering.updateRendering();
		}
	}

	/**
	 * Updates position, rotation and texture offsets of the object based on the
	 * current time (passed in as worldTime).
	 */
	public void updateDynamicProperties(long timeIncrement) {
			float deltaTime = timeIncrement / 1000.0f;

			//System.out.println("WWObject.updateDynamicProperties velocity: "+getVelocityLength());

			// update position and rotation
			position.x += velocity.x * deltaTime;
			position.y += velocity.y * deltaTime;
			position.z += velocity.z * deltaTime;
//				if (startPosition != null) {
//					if (positionX > startPosition.x && position.x < startPosition.x) {
//						position.x = startPosition.x;
//					} else if (positionX < startPosition.x && position.x > startPosition.x) {
//						position.x = startPosition.x;
//					}
//					if (positionY > startPosition.y && position.y < startPosition.y) {
//						position.y = startPosition.y;
//					} else if (positionY < startPosition.y && position.y > startPosition.y) {
//						position.y = startPosition.y;
//					}
//					if (positionZ > startPosition.z && position.z < startPosition.z) {
//						position.z = startPosition.z;
//					} else if (positionZ < startPosition.z && position.z > startPosition.z) {
//						position.z = startPosition.z;
//					}
//				}
//				if (stopPosition != null) {
//					if (positionX > stopPosition.x && position.x < stopPosition.x) {
//						position.x = stopPosition.x;
//					} else if (positionX < stopPosition.x && position.x > stopPosition.x) {
//						position.x = stopPosition.x;
//					}
//					if (positionY > stopPosition.y && position.y < stopPosition.y) {
//						position.y = stopPosition.y;
//					} else if (positionY < stopPosition.y && position.y > stopPosition.y) {
//						position.y = stopPosition.y;
//					}
//					if (positionZ > stopPosition.z && position.z < stopPosition.z) {
//						position.z = stopPosition.z;
//					} else if (positionZ < stopPosition.z && position.z > stopPosition.z) {
//						position.z = stopPosition.z;
//					}
//				}
			if (angularVelocity.x != 0 || angularVelocity.y != 0 || angularVelocity.z != 0) {
				float angle = getAngularVelocityLength() * deltaTime;
				rotation.spin(angle, angularVelocity.x, angularVelocity.y, angularVelocity.z);
	//			if (startRotation != null) {
	//				if (rotationX > startRotation.x && rotation.x < startRotation.x) {
	//					rotation.x = startRotation.x;
	//				} else if (rotationX < startRotation.x && rotation.x > startRotation.x) {
	//					rotation.x = startRotation.x;
	//				}
	//				if (rotationY > startRotation.y && rotation.y < startRotation.y) {
	//					rotation.y = startRotation.y;
	//				} else if (rotationY < startRotation.y && rotation.y > startRotation.y) {
	//					rotation.y = startRotation.y;
	//				}
	//				if (rotationZ > startRotation.z && rotation.z < startRotation.z) {
	//					rotation.z = startRotation.z;
	//				} else if (rotationZ < startRotation.z && rotation.z > startRotation.z) {
	//					rotation.z = startRotation.z;
	//				}
	//			}
	//			if (stopRotation != null) {
	//				if (rotationX > stopRotation.x && rotation.x < stopRotation.x) {
	//					rotation.x = stopRotation.x;
	//				} else if (rotationX < stopRotation.x && rotation.x > stopRotation.x) {
	//					rotation.x = stopRotation.x;
	//				}
	//				if (rotationY > stopRotation.y && rotation.y < stopRotation.y) {
	//					rotation.y = stopRotation.y;
	//				} else if (rotationY < stopRotation.y && rotation.y > stopRotation.y) {
	//					rotation.y = stopRotation.y;
	//				}
	//				if (rotationZ > stopRotation.z && rotation.z < stopRotation.z) {
	//					rotation.z = stopRotation.z;
	//				} else if (rotationZ < stopRotation.z && rotation.z > stopRotation.z) {
	//					rotation.z = stopRotation.z;
	//				}
	//			}
			}

			// update textures offsets
			for (int i = 0; i < NSIDES; i++) {
				SideAttributes side = sideAttributes[i];
				if (side != SideAttributes.defaultSurface) {
					side.texture.offsetX += side.texture.velocityX * deltaTime;
					side.texture.offsetY += side.texture.velocityY * deltaTime;
					side.texture.rotation += side.texture.aMomentum * deltaTime;
				}
			}


	}

	public final WWVector getSize() {
		return size;
	}

	/**
	 * This extent is the distance from the centerpoint
	 * of the object to the farthest edge of the object.
	 */
	public final float getExtent() {
		return extent;
	}

	/**
	 * Determines if this object overlaps the give object. If so, return a vector pointing directly to the location of the overlap. The length of the vector
	 * depends on the depth of penetration of the object. This vector can be used to apply physical properties to the object as a result of the two objects
	 * making contact.
	 * <p>
	 * For situations where the object is contained within another object, the vector's direction is taken from the velocity vector of the object. This is
	 * somewhat arbitrary, but hints to the physics thread as to the direction in which to move the objects such that they are not overlapping.
	 * <p>
	 * Note: the world time is passed into this method to improve accuracy. The physics simulation depends on applying all physics for all objects in the world
	 * at exact points in time.
	 */
	public final WWVector getOverlapVector(WWObject object, long worldTime) {

		WWVector position = getPosition();
		WWQuaternion rotation = getRotation();
		WWVector rotationPoint = getRotationPoint();
		WWVector objectPosition = object.getPosition();
		WWQuaternion objectRotation = object.getRotation();
		WWVector tempPoint = new WWVector();
		WWVector tempPoint2 = new WWVector();
		WWVector overlapPoint = new WWVector();
		WWVector overlapVector = new WWVector();

		getOverlap(object, position, rotation, rotationPoint, objectPosition, objectRotation, worldTime, tempPoint, tempPoint2, overlapPoint, overlapVector);

		return overlapVector;

	}

	private transient WWVector[] lastTransformedEdgePoints;
	private transient WWVector lastOverlapPosition;
	private transient WWQuaternion lastOverlapRotation;

	public final void getOverlap(WWObject object, WWVector position, WWQuaternion rotation, WWVector rotationPoint, WWVector objectPosition, WWQuaternion objectRotation, long worldTime, WWVector tempPoint, WWVector tempPoint2,
								 WWVector overlapPoint, WWVector overlapVector) {
		// To simplify overlap testing, check several key points. These are the eight corners, twelve
		// half edge points, and six center side points of the box. Test each of these.

		// First, transform the edge points by current rotation and position. These points are cached just in case
		// they are needed on the next overlap
		WWVector[] transformedEdgePoints;
		int edgePointsLength;
		if (parentId == 0 && lastTransformedEdgePoints != null && (fixed || (lastOverlapPosition.equals(position) && lastOverlapRotation.equals(rotation)))) {
			transformedEdgePoints = lastTransformedEdgePoints;
			edgePointsLength = edgePoints.length;
		} else {
			WWVector[] constEdgePoints = getEdgePoints();
			edgePointsLength = constEdgePoints.length;
			transformedEdgePoints = lastTransformedEdgePoints;
			if (transformedEdgePoints == null) {
				transformedEdgePoints = new WWVector[edgePointsLength];
			}
			for (int i = 0; i < edgePointsLength; i++) {
				WWVector edgePoint = transformedEdgePoints[i];
				if (edgePoint == null) {
					edgePoint = constEdgePoints[i].clone();
				} else {
					constEdgePoints[i].copyInto(edgePoint);
				}
				transform(edgePoint, position, rotation, rotationPoint);
				transformedEdgePoints[i] = edgePoint;
			}
			lastTransformedEdgePoints = transformedEdgePoints;
			lastOverlapPosition = position.clone();
			lastOverlapRotation = rotation.clone();
		}

		overlapVector.zero();
		for (int i = 0; i < edgePointsLength; i++) {
			WWVector edgePoint = transformedEdgePoints[i];
			// first test point with extents to avoid a costlier penetration calculation
			if (object.extentx + objectPosition.x > edgePoint.x && -object.extentx + objectPosition.x < edgePoint.x && //
					object.extenty + objectPosition.y > edgePoint.y && -object.extenty + objectPosition.y < edgePoint.y && //
					object.extentz + objectPosition.z > edgePoint.z && -object.extentz + objectPosition.z < edgePoint.z) {
				object.getPenetration(edgePoint, objectPosition, objectRotation, worldTime, tempPoint, tempPoint2);
				if (tempPoint2.isLongerThan(overlapVector)) {
					tempPoint2.copyInto(overlapVector);
					transformedEdgePoints[i].copyInto(overlapPoint);
					return; // less accurate but faster
				}
			}
		}

		// Note: If objects still penetrate walls, the only thing to do is to speed up the physics thread and iterate more often.
		// I've tried just about everything else. Keep in mind that with enough speed, any object can quantum-jump!
	}

	/**
	 * Returns a vector giving the amount of penetration of a point within the object, or null if the point does not penetrate.
	 */
	public abstract void getPenetration(WWVector point, WWVector position, WWQuaternion rotation, long worldTime, WWVector tempPoint, WWVector penetrationVector);

	/**
	 * Returns an array of points describing the edges of the object
	 */
	protected WWVector[] getEdgePoints() {
		if (edgePoints == null) {
			float sx2 = size.x / 2.0f;
			float sy2 = size.y / 2.0f;
			float sz2 = size.z / 2.0f;
			edgePoints = new WWVector[]{
					// - six center side points, starting with base, then front (for speed)
					new WWVector(0, 0, -sz2), new WWVector(0, -sy2, 0), new WWVector(sx2, 0, 0), new WWVector(-sx2, 0, 0), new WWVector(0, sy2, 0), new WWVector(0, 0, sz2),
					// - eight corners
					new WWVector(sx2, sy2, sz2), new WWVector(-sx2, sy2, sz2), new WWVector(sx2, -sy2, sz2), new WWVector(-sx2, -sy2, sz2), new WWVector(sx2, sy2, -sz2), new WWVector(-sx2, sy2, -sz2), new WWVector(sx2, -sy2, -sz2),
					new WWVector(-sx2, -sy2, -sz2),
					// - twelve half edge points
					new WWVector(sx2, sy2, 0), new WWVector(-sx2, sy2, 0), new WWVector(sx2, -sy2, 0), new WWVector(-sx2, -sy2, 0), new WWVector(sx2, 0, sz2), new WWVector(-sx2, 0, sz2), new WWVector(sx2, 0, -sz2),
					new WWVector(-sx2, 0, -sz2), new WWVector(0, sy2, sz2), new WWVector(0, -sy2, sz2), new WWVector(0, sy2, -sz2), new WWVector(0, -sy2, -sz2)};
		}
		return edgePoints;
	}

	/**
	 * Rotate a point according to a given rotation.
	 */
	public static final void rotate(WWVector point, WWQuaternion rotation) {
		rotation.rotateVector(point);
	}

	/**
	 * Anti-rotate a point, removing a given rotation. Note that this is mainly useful to perform on velocity/force vectors.
	 */
	public static final void antiRotate(WWVector point, WWQuaternion rotation) {
		rotation.antirotateVector(point);
	}

	/**
	 * Transform a point according to a rotation and position at a given time.
	 */
	public static final void transform(WWVector point, WWVector position, WWQuaternion rotation, WWVector rotationPoint) {
		rotation.rotateVector(point);
		point.add(position);
	}

	/**
	 * Anti-transform a point, removing the a given position and rotation.
	 */
	public static final void antiTransform(WWVector point, WWVector position, WWQuaternion rotation, long time) {
		point.subtract(position);
		rotation.clone().invert().rotateVector(point);
	}

	@Override
	public WWObject clone() {
		WWObject clone = cloneNoBehavior();
		if (behaviors != null) {
			// Need to clone behaviors and set owner in cloned behaviors to the cloned owner
			clone.behaviors = behaviors.clone();
			for (int i = 0; i < behaviors.length; i++) {
				clone.behaviors[i] = (BehaviorAttributes) behaviors[i].clone();
				clone.behaviors[i].behavior.owner = clone;
			}
		}
		return clone;
	}

	/**
	 * Same as clone, but behaviors aren't cloned.
	 *
	 * @return
	 * @throws CloneNotSupportedException
	 */
	public WWObject cloneNoBehavior() {
		WWObject clone = (WWObject) super.clone();
		if (world != null) {
			long currenttime = getWorldTime();
			clone.setLastModifyTime(currenttime);
			clone.position = position.clone();
			clone.rotation = rotation.clone();
		}
		clone.velocity = velocity.clone();
		clone.angularVelocity = angularVelocity.clone();
		clone.thrust = thrust.clone();
		clone.thrustVelocity = thrustVelocity.clone();
		clone.torque = torque.clone();
		clone.torqueVelocity = torqueVelocity.clone();
		sideAttributes = new SideAttributes[NSIDES];
		for (int i = 0; i < NSIDES; i++) {
			if (clone.sideAttributes[i] == SideAttributes.defaultSurface) {
				sideAttributes[i] = SideAttributes.defaultSurface;
			} else {
				sideAttributes[i] = (SideAttributes) clone.sideAttributes[i].clone();
			}
		}

		clone.behaviors = null;
		return clone;
	}

	public void copyFrom(WWObject newObject) {
		// this.id = newObject.id;
		this.lastMoveTime = newObject.lastMoveTime;
		newObject.position.copyInto(this.position);
		newObject.rotation.copyInto(this.rotation);

		// Grouping properties
		this.parentId = newObject.parentId;
		this.gluedToParent = newObject.gluedToParent;
		this.children = newObject.children; // no need to clone as any change will cause copy of array

		// Physics properties
		this.physical = newObject.physical;
		this.phantom = newObject.phantom;
		newObject.velocity.copyInto(this.velocity);
		newObject.angularVelocity.copyInto(this.angularVelocity);
		this.density = newObject.density;
		this.solid = newObject.solid;
		this.elasticity = newObject.elasticity;
		this.friction = newObject.friction;
		this.freedomMoveX = newObject.freedomMoveX;
		this.freedomMoveY = newObject.freedomMoveY;
		this.freedomMoveZ = newObject.freedomMoveZ;
		this.freedomRotateX = newObject.freedomRotateX;
		this.freedomRotateY = newObject.freedomRotateY;
		this.freedomRotateZ = newObject.freedomRotateZ;
		newObject.thrust.copyInto(this.thrust);
		newObject.thrustVelocity.copyInto(this.thrustVelocity);
		newObject.torque.copyInto(this.torque);
		newObject.torqueVelocity.copyInto(this.torqueVelocity);

		this.pickable = newObject.pickable;
		this.penetratable = newObject.penetratable;

		this.sound = newObject.sound;
		this.impactSound = newObject.impactSound;
		this.slidingSound = newObject.slidingSound;

		// this.behaviors = newObject.behaviors;

		// this.rendering = newObject.rendering;
		// this.lastRenderingTime = newObject.lastRenderingTime;

		this.stopPosition = newObject.stopPosition;
		this.stopRotation = newObject.stopRotation;

	}

	public final int getParentId() {
		return parentId;
	}

	public final void setParentId(int parentId) {
		this.parentId = parentId;
	}

	public final WWObject getParent() {
		if (parentId != 0 && world != null) {
			return world.getObject(parentId);
		}
		return null;
	}

	public final void setParentId(WWObject parent) {
		if (parent == null) {
			this.parentId = 0;
		} else {
			this.parentId = parent.getId();
		}
	}

	public final boolean isDescendantOf(WWObject parent) {
		int tempId = parentId;
		while (tempId != 0) {
			if (parent.id == tempId) {
				return true;
			}
			tempId = world.objects[tempId].parentId;
		}
		return false;
	}

	public final void setPhantom(boolean phantom) {
		this.phantom = phantom;
	}

	/**
	 * Indicates that the object is completely ghost-like, not partaking in physics of any kind.
	 */
	public final boolean isPhantom() {
		return phantom;
	}

	public final boolean isGluedToParent() {
		return gluedToParent;
	}

	public final void setGluedToParent(boolean gluedToParent) {
		this.gluedToParent = gluedToParent;
	}

	public final WWObject[] getChildren() {
		migrateChildIds();
		if (children == null) {
			return DEFAULT_CHILDREN;
		}
		return children;
	}

	// convert childrenIds over to children. We're phasing out childrenIds.
	private void migrateChildIds() {
		if (childrenIds != null && world != null) {
			children = new WWObject[childrenIds.length];
			for (int i = 0; i < childrenIds.length; i++) {
				children[i] = world.getObject(childrenIds[i]);
			}
			childrenIds = null;
		}
	}

	public final void setChildren(WWObject[] children) {
		this.children = children;
	}

	public final WWObject getChild(String name) {
		migrateChildIds();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				WWObject childObject = children[i];
				if (name.equals(childObject.getName())) {
					return childObject;
				}
			}
		}
		return null;
	}

	public final void addChild(WWObject child) {
		migrateChildIds();
		if (!child.isDescendantOf(this)) {
			if (child.parentId != 0) {
				throw new RuntimeException(""+child+" is already a child of "+child.getParent());
			}
			WWObject[] newChildren;
			if (children == null) {
				newChildren = new WWObject[1];
			} else {
				newChildren = new WWObject[children.length + 1];
				for (int i = 0; i < children.length; i++) {
					newChildren[i] = children[i];
				}
			}
			newChildren[newChildren.length - 1] = child;
			children = newChildren;
			if (this.world != null) {
				child.parentId = id;
				world.addObject(child);
			}
		}
	}

	public final void removeChild(WWObject child) {
		migrateChildIds();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				if (children[i] == child) {
					WWObject[] newChildren = new WWObject[children.length - 1];
					for (int j = 0; j < i; j++) {
						newChildren[j] = children[j];
					}
					for (int j = i; j < children.length - 1; j++) {
						newChildren[j] = children[j + 1];
					}
					children = newChildren;
					if (this.world != null) {
						world.removeObject(child.id);
					}
					return;
				}
			}
		}
	}

	public final WWObject getDescendant(String name) {
		migrateChildIds();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				WWObject childObject = children[i];
				if (name.equals(childObject.getName())) {
					return childObject;
				}
				WWObject descendant = childObject.getDescendant(name);
				if (descendant != null) {
					return descendant;
				}
			}
		}
		return null;
	}

	public final boolean isDescendant(WWObject object) {
		migrateChildIds();
		if (children != null) {
			for (WWObject child : getChildren()) {
				if (child == object) {
					return true;
				}
				if (child.isDescendant(object)) {
					return true;
				}
			}
		}
		return false;
	}

	public final int addBehavior(final byte[] behaviorClassBinary) {
		BehaviorAttributes behaviorAttributes = new BehaviorAttributes(behaviorClassBinary);
		return addBehavior(behaviorAttributes);
	}

	public final int addBehavior(final String behaviorClassName) {
		BehaviorAttributes behaviorAttributes = new BehaviorAttributes(behaviorClassName);
		return addBehavior(behaviorAttributes);
	}

	public final int addBehavior(final WWBehavior behavior) {
		behavior.owner = this;
		BehaviorAttributes behaviorAttributes = new BehaviorAttributes(behavior);
		return addBehavior(behaviorAttributes);
	}

	private final int addBehavior(BehaviorAttributes behaviorAttributes) {
		BehaviorAttributes[] newBehaviors;
		if (behaviors == null) {
			newBehaviors = new BehaviorAttributes[1];
		} else {
			newBehaviors = new BehaviorAttributes[behaviors.length + 1];
			for (int i = 0; i < behaviors.length; i++) {
				newBehaviors[i] = behaviors[i];
			}
		}
		behaviorAttributes.instantiateBehavior();
		newBehaviors[newBehaviors.length - 1] = behaviorAttributes;
		behaviors = newBehaviors;
		return behaviors.length - 1;
	}

	public final void removeBehavior(int behaviorIndex) {
		BehaviorAttributes[] newBehaviors = new BehaviorAttributes[behaviors.length - 1];
		for (int i = 0; i < behaviorIndex; i++) {
			newBehaviors[i] = behaviors[i];
		}
		for (int i = behaviorIndex + 1; i < behaviors.length; i++) {
			newBehaviors[i - 1] = behaviors[i];
		}
		behaviors = newBehaviors;
	}

	public final int getBehaviorCount() {
		if (behaviors == null) {
			return 0;
		}
		return behaviors.length;
	}

	public final WWBehavior getBehavior(int behaviorIndex) {
		return behaviors[behaviorIndex].behavior;
	}

	public final WWBehavior getBehavior(Class behaviorClass) {
		if (behaviors == null) {
			return null;
		}
		for (int i = 0; i < behaviors.length; i++) {
			WWBehavior behavior = behaviors[i].behavior;
			if (behavior.getClass() == behaviorClass) {
				return behavior;
			}
		}
		return null;
	}

	public final WWBehavior[] getBehaviors() {
		if (behaviors == null) {
			return new WWBehavior[0];
		}
		WWBehavior[] b = new WWBehavior[behaviors.length];
		for (int i = 0; i < behaviors.length; i++) {
			b[i] = behaviors[i].behavior;
		}
		return b;
	}

	/**
	 * Invoked by BehaviorThread to launch behaviors events on the object. If the object has a parent and the behaviors do not override, the parent object will
	 * also be invoked for the behavior event.
	 */
	public final void invokeBehavior(String command, WWEntity agent, Object params) {
		try {
			boolean overrideParent = false;
			if (behaviors != null) {
				if (command.equals("touch")) {
					for (int i = 0; i < behaviors.length; i++) {
						behaviors[i].behavior.world = world;
						WWWorld.TouchParams tparams = (WWWorld.TouchParams) params;
						overrideParent |= behaviors[i].behavior.touchEvent(this, agent, tparams.surface, tparams.x, tparams.y);
					}
				} else if (command.equals("press")) {
					for (int i = 0; i < behaviors.length; i++) {
						behaviors[i].behavior.world = world;
						WWWorld.TouchParams tparams = (WWWorld.TouchParams) params;
						overrideParent |= behaviors[i].behavior.pressEvent(this, agent, tparams.surface, tparams.x, tparams.y);
					}
				} else if (command.equals("drag")) {
					for (int i = 0; i < behaviors.length; i++) {
						behaviors[i].behavior.world = world;
						WWWorld.TouchParams tparams = (WWWorld.TouchParams) params;
						overrideParent |= behaviors[i].behavior.dragEvent(this, agent, tparams.surface, tparams.x, tparams.y);
					}
				} else if (command.equals("release")) {
					for (int i = 0; i < behaviors.length; i++) {
						behaviors[i].behavior.world = world;
						WWWorld.TouchParams tparams = (WWWorld.TouchParams) params;
						overrideParent |= behaviors[i].behavior.releaseEvent(this, agent, tparams.surface, tparams.x, tparams.y);
					}
				} else if (command.equals("collide")) {
					for (int i = 0; i < behaviors.length; i++) {
						behaviors[i].behavior.world = world;
						overrideParent |= behaviors[i].behavior.collideEvent(this, (WWObject) agent, (WWVector) params);
					}
				} else if (command.equals("slide")) {
					for (int i = 0; i < behaviors.length; i++) {
						behaviors[i].behavior.world = world;
						overrideParent |= behaviors[i].behavior.slideEvent(this, (WWObject) agent, (WWVector) params);
					}
				} else if (command.equals("stopSlide")) {
					for (int i = 0; i < behaviors.length; i++) {
						behaviors[i].behavior.world = world;
						overrideParent |= behaviors[i].behavior.stopSlideEvent(this, (WWObject) agent, (WWVector) params);
					}
				} else if (command.equals("timer")) {
					for (int i = 0; i < behaviors.length; i++) {
						behaviors[i].behavior.world = world;
						overrideParent |= behaviors[i].behavior.timerEvent(this);
					}
				}
			}
			if (!overrideParent && parentId != 0 && world != null) {
				world.objects[parentId].invokeBehavior(command, agent, params);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	public final void dropRendering() {
		if (rendering != null) {
//			rendering.destroy();
		}
		rendering = null;
	}

//	public final float getsize.x() {
//		return size.x;
//	}
//
//	public final float getSizeY() {
//		return size.y;
//	}
//
//	public final float getSizeZ() {
//		return size.z;
//	}

	public final void setSize(WWVector v) {
		size = v;
		calculateExtents();
		edgePoints = null;
		updateRendering();;
	}

	public final void setSize(float x, float y, float z) {
		size.x = x;
		size.y = y;
		size.z = z;
		calculateExtents();
		edgePoints = null;
		updateRendering();
	}

	public void setSize(float[] dims) {
		setSize(dims[0], dims[1], dims[2]);
	}

	/**
	 * Overridable is subclasses to make smarter extent calculations, avoiding more complicated math when determining overlaps.
	 */
	protected void calculateExtents() {
		if (fixed && parentId == 0) {
			// TODO rotate the size to use as extents rather than the logic below.  It is more accurate and reduces physics load.
			extent = (float) Math.sqrt(size.x * size.x + size.y * size.y + size.z * size.z) / 2.0f;
			extentx = extent;
			extenty = extent;
			extentz = extent;
		} else {
			extent = (float) Math.sqrt(size.x * size.x + size.y * size.y + size.z * size.z) / 2.0f;
			extentx = extent;
			extenty = extent;
			extentz = extent;
		}
	}

	/**
	 * Note: this is not intended for use by behaviors. It is provided for import/export.
	 */
	public final SideAttributes[] getSideAttributesArray() {
		return sideAttributes;
	}

	/**
	 * Note: this is not intended for use by behaviors. It is provided for import/export.
	 */
	public final void setSideAttributesArray(SideAttributes[] sideAttributes) {
		this.sideAttributes = sideAttributes;
	}

	protected final SideAttributes getEditableSideAttributes(int side) {
		if (side == SIDE_ALL) {
			if (sideAttributes[SIDE_ALL].isDefault) {
				sideAttributes[SIDE_ALL] = new SideAttributes();
				for (int i = SIDE_ALL + 1; i < NSIDES; i++) {
					if (sideAttributes[i].isDefault) {
						sideAttributes[i] = sideAttributes[SIDE_ALL];
					}
				}
			}
		} else {
			if (sideAttributes[side].isDefault || sideAttributes[side] == sideAttributes[SIDE_ALL]) {
				sideAttributes[side] = (SideAttributes) sideAttributes[SIDE_ALL].clone();
				sideAttributes[side].isDefault = false;
				monolithic = false;
			}
		}
		return sideAttributes[side];
	}

	private final WWColor getColor(int side) {
		return sideAttributes[side].color;
	}

	public final WWColor getColor() {
		return getColor(SIDE_ALL);
	}

	public final WWColor getColorTop() {
		return getColor(SIDE_TOP);
	}

	public final WWColor getColorBottom() {
		return getColor(SIDE_BOTTOM);
	}

	public final WWColor getColorSide1() {
		return getColor(SIDE_SIDE1);
	}

	public final WWColor getColorSide2() {
		return getColor(SIDE_SIDE2);
	}

	public final WWColor getColorSide3() {
		return getColor(SIDE_SIDE3);
	}

	public final WWColor getColorSide4() {
		return getColor(SIDE_SIDE4);
	}

	public final WWColor getColorInsideTop() {
		return getColor(SIDE_INSIDE_TOP);
	}

	public final WWColor getColorInsideBottom() {
		return getColor(SIDE_INSIDE_BOTTOM);
	}

	public final WWColor getColorInside1() {
		return getColor(SIDE_INSIDE1);
	}

	public final WWColor getColorInside2() {
		return getColor(SIDE_INSIDE2);
	}

	public final WWColor getColorInside3() {
		return getColor(SIDE_INSIDE3);
	}

	public final WWColor getColorInside4() {
		return getColor(SIDE_INSIDE4);
	}

	public final WWColor getColorCutout1() {
		return getColor(SIDE_CUTOUT1);
	}

	public final WWColor getColorCutout2() {
		return getColor(SIDE_CUTOUT2);
	}

	private final void setColor(int side, WWColor color) {
		getEditableSideAttributes(side).color = color;
	}

	public final void setColor(WWColor color) {
		setColor(SIDE_ALL, color);
	}

	public final void setColorTop(WWColor color) {
		setColor(SIDE_TOP, color);
	}

	public final void setColorBottom(WWColor color) {
		setColor(SIDE_BOTTOM, color);
	}

	public final void setColorSide1(WWColor color) {
		setColor(SIDE_SIDE1, color);
	}

	public final void setColorSide2(WWColor color) {
		setColor(SIDE_SIDE2, color);
	}

	public final void setColorSide3(WWColor color) {
		setColor(SIDE_SIDE3, color);
	}

	public final void setColorSide4(WWColor color) {
		setColor(SIDE_SIDE4, color);
	}

	public final void setColorInsideTop(WWColor color) {
		setColor(SIDE_INSIDE_TOP, color);
	}

	public final void setColorInsideBottom(WWColor color) {
		setColor(SIDE_INSIDE_BOTTOM, color);
	}

	public final void setColorInside1(WWColor color) {
		setColor(SIDE_INSIDE1, color);
	}

	public final void setColorInside2(WWColor color) {
		setColor(SIDE_INSIDE2, color);
	}

	public final void setColorInside3(WWColor color) {
		setColor(SIDE_INSIDE3, color);
	}

	public final void setColorInside4(WWColor color) {
		setColor(SIDE_INSIDE4, color);
	}

	public final void setColorCutout1(WWColor color) {
		setColor(SIDE_CUTOUT1, color);
	}

	public final void setColorCutout2(WWColor color) {
		setColor(SIDE_CUTOUT2, color);
	}

	private final WWTexture getTexture(int side) {
		WWTexture texture = new WWTexture();
		SideAttributes sideAttributes = getEditableSideAttributes(side);
		return sideAttributes.texture;
	}

	public final WWTexture getTexture() {
		return getTexture(SIDE_ALL);
	}

	public final WWTexture getTextureTop() {
		return getTexture(SIDE_TOP);
	}

	public final WWTexture getTextureBottom() {
		return getTexture(SIDE_BOTTOM);
	}

	public final WWTexture getTextureSide1() {
		return getTexture(SIDE_SIDE1);
	}

	public final WWTexture getTextureSide2() {
		return getTexture(SIDE_SIDE2);
	}

	public final WWTexture getTextureSide3() {
		return getTexture(SIDE_SIDE3);
	}

	public final WWTexture getTextureSide4() {
		return getTexture(SIDE_SIDE4);
	}

	public final WWTexture getTextureInsideTop() {
		return getTexture(SIDE_INSIDE_TOP);
	}

	public final WWTexture getTextureInsideBottom() {
		return getTexture(SIDE_INSIDE_BOTTOM);
	}

	public final WWTexture getTextureInside1() {
		return getTexture(SIDE_INSIDE1);
	}

	public final WWTexture getTextureInside2() {
		return getTexture(SIDE_INSIDE2);
	}

	public final WWTexture getTextureInside3() {
		return getTexture(SIDE_INSIDE3);
	}

	public final WWTexture getTextureInside4() {
		return getTexture(SIDE_INSIDE4);
	}

	public final WWTexture getTextureCutout1() {
		return getTexture(SIDE_CUTOUT1);
	}

	public final WWTexture getTextureCutout2() {
		return getTexture(SIDE_CUTOUT2);
	}

	private final void setTexture(int side, WWTexture texture) {
		SideAttributes sideAttributes = getEditableSideAttributes(side);
		sideAttributes.texture = texture;
	}

	public final void setTexture(WWTexture texture) {
		setTexture(SIDE_ALL, texture);
	}

	/**
	 * This is a simple texture setter, that uses the same texture
	 * on all sides of the object, and scales the texture so that it looks
	 * uniform on all sides.
	 */
	public final void setTexture(String textureName) {
		getEditableSideAttributes(SIDE_ALL).texture.name = textureName;
		getEditableSideAttributes(SIDE_TOP).texture.scaleX = 10 / size.x;
		getEditableSideAttributes(SIDE_TOP).texture.scaleY = 10 / size.y;
		getEditableSideAttributes(SIDE_BOTTOM).texture.scaleX = 10 / size.x;
		getEditableSideAttributes(SIDE_BOTTOM).texture.scaleY = 10 / size.y;
		getEditableSideAttributes(SIDE_SIDE1).texture.scaleX = 10 / size.x;
		getEditableSideAttributes(SIDE_SIDE2).texture.scaleX =  10 / size.y;
		getEditableSideAttributes(SIDE_SIDE3).texture.scaleX =  10 / size.x;
		getEditableSideAttributes(SIDE_SIDE4).texture.scaleX =  10 / size.y;
		getEditableSideAttributes(SIDE_SIDE1).texture.scaleY = 10 / size.z;
		getEditableSideAttributes(SIDE_SIDE2).texture.scaleY = 10 / size.z;
		getEditableSideAttributes(SIDE_SIDE3).texture.scaleY = 10 / size.z;
		getEditableSideAttributes(SIDE_SIDE4).texture.scaleY = 10 / size.z;
	}

	public final void setTextureTop(WWTexture texture) {
		setTexture(SIDE_TOP, texture);
	}

	public final void setTextureBottom(WWTexture texture) {
		setTexture(SIDE_BOTTOM, texture);
	}

	public final void setTextureSide1(WWTexture texture) {
		setTexture(SIDE_SIDE1, texture);
	}

	public final void setTextureSide2(WWTexture texture) {
		setTexture(SIDE_SIDE2, texture);
	}

	public final void setTextureSide3(WWTexture texture) {
		setTexture(SIDE_SIDE3, texture);
	}

	public final void setTextureSide4(WWTexture texture) {
		setTexture(SIDE_SIDE4, texture);
	}

	public final void setTextureInsideTop(WWTexture texture) {
		setTexture(SIDE_INSIDE_TOP, texture);
	}

	public final void setTextureInsideBottom(WWTexture texture) {
		setTexture(SIDE_INSIDE_BOTTOM, texture);
	}

	public final void setTextureInside1(WWTexture texture) {
		setTexture(SIDE_INSIDE1, texture);
	}

	public final void setTextureInside2(WWTexture texture) {
		setTexture(SIDE_INSIDE2, texture);
	}

	public final void setTextureInside3(WWTexture texture) {
		setTexture(SIDE_INSIDE3, texture);
	}

	public final void setTextureInside4(WWTexture texture) {
		setTexture(SIDE_INSIDE4, texture);
	}

	public final void setTextureCutout1(WWTexture texture) {
		setTexture(SIDE_CUTOUT1, texture);
	}

	public final void setTextureCutout2(WWTexture texture) {
		setTexture(SIDE_CUTOUT2, texture);
	}

	private final float getTransparency(int side) {
		return sideAttributes[side].transparency;
	}

	public final float getTransparency() {
		return getTransparency(SIDE_ALL);
	}

	public final float getTransparencyTop() {
		return getTransparency(SIDE_TOP);
	}

	public final float getTransparencyBottom() {
		return getTransparency(SIDE_BOTTOM);
	}

	public final float getTransparencySide1() {
		return getTransparency(SIDE_SIDE1);
	}

	public final float getTransparencySide2() {
		return getTransparency(SIDE_SIDE2);
	}

	public final float getTransparencySide3() {
		return getTransparency(SIDE_SIDE3);
	}

	public final float getTransparencySide4() {
		return getTransparency(SIDE_SIDE4);
	}

	public final float getTransparencyInsideTop() {
		return getTransparency(SIDE_INSIDE_TOP);
	}

	public final float getTransparencyInsideBottom() {
		return getTransparency(SIDE_INSIDE_BOTTOM);
	}

	public final float getTransparencyInside1() {
		return getTransparency(SIDE_INSIDE1);
	}

	public final float getTransparencyInside2() {
		return getTransparency(SIDE_INSIDE2);
	}

	public final float getTransparencyInside3() {
		return getTransparency(SIDE_INSIDE3);
	}

	public final float getTransparencyInside4() {
		return getTransparency(SIDE_INSIDE4);
	}

	public final float getTransparencyCutout1() {
		return getTransparency(SIDE_CUTOUT1);
	}

	public final float getTransparencyCutout2() {
		return getTransparency(SIDE_CUTOUT2);
	}

	private final void setTransparency(int side, float transparency) {
		getEditableSideAttributes(side).transparency = transparency;
	}

	public final void setTransparency(float transparency) {
		setTransparency(SIDE_ALL, transparency);
	}

	public final void setTransparencyTop(float transparency) {
		setTransparency(SIDE_TOP, transparency);
	}

	public final void setTransparencyBottom(float transparency) {
		setTransparency(SIDE_BOTTOM, transparency);
	}

	public final void setTransparencySide1(float transparency) {
		setTransparency(SIDE_SIDE1, transparency);
	}

	public final void setTransparencySide2(float transparency) {
		setTransparency(SIDE_SIDE2, transparency);
	}

	public final void setTransparencySide3(float transparency) {
		setTransparency(SIDE_SIDE3, transparency);
	}

	public final void setTransparencySide4(float transparency) {
		setTransparency(SIDE_SIDE4, transparency);
	}

	public final void setTransparencyInsideTop(float transparency) {
		setTransparency(SIDE_INSIDE_TOP, transparency);
	}

	public final void setTransparencyInsideBottom(float transparency) {
		setTransparency(SIDE_INSIDE_BOTTOM, transparency);
	}

	public final void setTransparencyInside1(float transparency) {
		setTransparency(SIDE_INSIDE1, transparency);
	}

	public final void setTransparencyInside2(float transparency) {
		setTransparency(SIDE_INSIDE2, transparency);
	}

	public final void setTransparencyInside3(float transparency) {
		setTransparency(SIDE_INSIDE3, transparency);
	}

	public final void setTransparencyInside4(float transparency) {
		setTransparency(SIDE_INSIDE4, transparency);
	}

	public final void setTransparencyCutout1(float transparency) {
		setTransparency(SIDE_CUTOUT1, transparency);
	}

	public final void setTransparencyCutout2(float transparency) {
		setTransparency(SIDE_CUTOUT2, transparency);
	}

	private final float getShininess(int side) {
		return sideAttributes[side].shininess;
	}

	public final float getShininess() {
		return getShininess(SIDE_ALL);
	}

	public final float getShininessTop() {
		return getShininess(SIDE_TOP);
	}

	public final float getShininessBottom() {
		return getShininess(SIDE_BOTTOM);
	}

	public final float getShininessSide1() {
		return getShininess(SIDE_SIDE1);
	}

	public final float getShininessSide2() {
		return getShininess(SIDE_SIDE2);
	}

	public final float getShininessSide3() {
		return getShininess(SIDE_SIDE3);
	}

	public final float getShininessSide4() {
		return getShininess(SIDE_SIDE4);
	}

	public final float getShininessInsideTop() {
		return getShininess(SIDE_INSIDE_TOP);
	}

	public final float getShininessInsideBottom() {
		return getShininess(SIDE_INSIDE_BOTTOM);
	}

	public final float getShininessInside1() {
		return getShininess(SIDE_INSIDE1);
	}

	public final float getShininessInside2() {
		return getShininess(SIDE_INSIDE2);
	}

	public final float getShininessInside3() {
		return getShininess(SIDE_INSIDE3);
	}

	public final float getShininessInside4() {
		return getShininess(SIDE_INSIDE4);
	}

	public final float getShininessCutout1() {
		return getShininess(SIDE_CUTOUT1);
	}

	public final float getShininessCutout2() {
		return getShininess(SIDE_CUTOUT2);
	}

	private final void setShininess(int side, float shininess) {
		getEditableSideAttributes(side).shininess = shininess;
	}

	public final void setShininess(float shininess) {
		getEditableSideAttributes(SIDE_ALL).shininess = shininess;
	}

	public final void setShininessTop(float shininess) {
		getEditableSideAttributes(SIDE_TOP).shininess = shininess;
	}

	public final void setShininessBottom(float shininess) {
		getEditableSideAttributes(SIDE_BOTTOM).shininess = shininess;
	}

	public final void setShininessSide1(float shininess) {
		getEditableSideAttributes(SIDE_SIDE1).shininess = shininess;
	}

	public final void setShininessSide2(float shininess) {
		getEditableSideAttributes(SIDE_SIDE2).shininess = shininess;
	}

	public final void setShininessSide3(float shininess) {
		getEditableSideAttributes(SIDE_SIDE3).shininess = shininess;
	}

	public final void setShininessSide4(float shininess) {
		getEditableSideAttributes(SIDE_SIDE4).shininess = shininess;
	}

	public final void setShininessInsideTop(float shininess) {
		getEditableSideAttributes(SIDE_INSIDE_TOP).shininess = shininess;
	}

	public final void setShininessInsideBottom(float shininess) {
		getEditableSideAttributes(SIDE_INSIDE_BOTTOM).shininess = shininess;
	}

	public final void setShininessInside1(float shininess) {
		getEditableSideAttributes(SIDE_INSIDE1).shininess = shininess;
	}

	public final void setShininessInside2(float shininess) {
		getEditableSideAttributes(SIDE_INSIDE2).shininess = shininess;
	}

	public final void setShininessInside3(float shininess) {
		getEditableSideAttributes(SIDE_INSIDE3).shininess = shininess;
	}

	public final void setShininessInside4(float shininess) {
		getEditableSideAttributes(SIDE_INSIDE4).shininess = shininess;
	}

	public final void setShininessCutout1(float shininess) {
		getEditableSideAttributes(SIDE_CUTOUT1).shininess = shininess;
	}

	public final void setShininessCutout2(float shininess) {
		getEditableSideAttributes(SIDE_CUTOUT2).shininess = shininess;
	}

	private final boolean isFullBright(int side) {
		return sideAttributes[side].fullBright;
	}

	public final boolean isFullBright() {
		return isFullBright(SIDE_ALL);
	}

	public final boolean isFullBrightTop() {
		return isFullBright(SIDE_TOP);
	}

	public final boolean isFullBrightBottom() {
		return isFullBright(SIDE_BOTTOM);
	}

	public final boolean isFullBrightSide1() {
		return isFullBright(SIDE_SIDE1);
	}

	public final boolean isFullBrightSide2() {
		return isFullBright(SIDE_SIDE2);
	}

	public final boolean isFullBrightSide3() {
		return isFullBright(SIDE_SIDE3);
	}

	public final boolean isFullBrightSide4() {
		return isFullBright(SIDE_SIDE4);
	}

	public final boolean isFullBrightInsideTop() {
		return isFullBright(SIDE_INSIDE_TOP);
	}

	public final boolean isFullBrightInsideBottom() {
		return isFullBright(SIDE_INSIDE_BOTTOM);
	}

	public final boolean isFullBrightInside1() {
		return isFullBright(SIDE_INSIDE1);
	}

	public final boolean isFullBrightInside2() {
		return isFullBright(SIDE_INSIDE2);
	}

	public final boolean isFullBrightInside3() {
		return isFullBright(SIDE_INSIDE3);
	}

	public final boolean isFullBrightInside4() {
		return isFullBright(SIDE_INSIDE4);
	}

	public final boolean isFullBrightCutout1() {
		return isFullBright(SIDE_CUTOUT1);
	}

	public final boolean isFullBrightCutout2() {
		return isFullBright(SIDE_CUTOUT2);
	}

	private final void setFullBright(int side, boolean fullBright) {
		getEditableSideAttributes(side).fullBright = fullBright;
	}

	public final void setFullBright(boolean fullBright) {
		setFullBright(SIDE_ALL, fullBright);
	}

	public final void setFullBrightTop(boolean fullBright) {
		setFullBright(SIDE_TOP, fullBright);
	}

	public final void setFullBrightBottom(boolean fullBright) {
		setFullBright(SIDE_BOTTOM, fullBright);
	}

	public final void setFullBrightSide1(boolean fullBright) {
		setFullBright(SIDE_SIDE1, fullBright);
	}

	public final void setFullBrightSide2(boolean fullBright) {
		setFullBright(SIDE_SIDE2, fullBright);
	}

	public final void setFullBrightSide3(boolean fullBright) {
		setFullBright(SIDE_SIDE3, fullBright);
	}

	public final void setFullBrightSide4(boolean fullBright) {
		setFullBright(SIDE_SIDE4, fullBright);
	}

	public final void setFullBrightInsideTop(boolean fullBright) {
		setFullBright(SIDE_INSIDE_TOP, fullBright);
	}

	public final void setFullBrightInsideBottom(boolean fullBright) {
		setFullBright(SIDE_INSIDE_BOTTOM, fullBright);
	}

	public final void setFullBrightInside1(boolean fullBright) {
		setFullBright(SIDE_INSIDE1, fullBright);
	}

	public final void setFullBrightInside2(boolean fullBright) {
		setFullBright(SIDE_INSIDE2, fullBright);
	}

	public final void setFullBrightInside3(boolean fullBright) {
		setFullBright(SIDE_INSIDE3, fullBright);
	}

	public final void setFullBrightInside4(boolean fullBright) {
		setFullBright(SIDE_INSIDE4, fullBright);
	}

	public final void setFullBrightCutout1(boolean fullBright) {
		setFullBright(SIDE_CUTOUT1, fullBright);
	}

	public final void setFullBrightCutout2(boolean fullBright) {
		setFullBright(SIDE_CUTOUT2, fullBright);
	}

	public final boolean isVideoTexture() {
		return (videoTextureRenderer != null);
	}

	public final boolean isPlaying() {
		return videoTextureRenderer.isPlaying();
	}

	public final void play() {
		videoTextureRenderer.play();
	}

	public final void pause() {
		videoTextureRenderer.pause();
	}

	public final void stop() {
		videoTextureRenderer.stop();
	}

	public final void setFixed(boolean fixed) {
		this.fixed = fixed;
		calculateExtents();
	}

	public final boolean isFixed() {
		return fixed;
	}

	public final void setGroup(int group) {
		this.group = group;
	}

	public final int getGroup() {
		return group;
	}

	public boolean isShadowless() {
		return shadowless;
	}

	public void setShadowless(boolean shadowless) {
		this.shadowless = shadowless;
	}

	public void setActions(WWAction[] actions) {
		this.actions = actions;
	}

	public WWAction[] getActions() {
		return actions;
	}

	public void playSound(String soundName, float volume) {
		if (getRendering() != null) {
			rendering.getRenderer().getSoundGenerator().playSound(soundName, 1, this.getPosition(), volume, 1.0f);
		}
	}


}