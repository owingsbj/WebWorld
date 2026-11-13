package com.gallantrealm.myworld.model.physics;

import java.util.ArrayList;
import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.model.WWBehavior;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWQuaternion;
import com.gallantrealm.myworld.model.WWVector;
import com.gallantrealm.myworld.model.WWWorld;

/**
 * This thread performs updates to the world according to physical properties. This involves detecting collision and
 * forces between objects and adjusting the position, orientation, velocity and angular velocity to match.
 * <p>
 * Note that this thread does not actually handle moving or rotating objects due to their own velocity and angular
 * velocity. This is handled within the WWObject itself.
 * <p>
 * This is the "Old" version of physics. It applies thrust last, which allows it to better handle walking-style of
 * avatar movement.
 */
public class OldPhysicsThread extends PhysicsThread {

	private ArrayList<ObjectCollision> previousPreviousCollidedObjects = new ArrayList<ObjectCollision>();
	private ArrayList<ObjectCollision> previousCollidedObjects = new ArrayList<ObjectCollision>();
	private ArrayList<ObjectCollision> newCollidedObjects = new ArrayList<ObjectCollision>();
	private final ObjectCollisionPool collisionPool = new ObjectCollisionPool();

	private final WWVector position = new WWVector();
	private final WWQuaternion rotation = new WWQuaternion();
	private final WWVector rotationPoint = new WWVector();
	private final WWVector velocity = new WWVector();
	private final WWVector angularVelocity = new WWVector();
	private final WWVector position2 = new WWVector();
	private final WWQuaternion rotation2 = new WWQuaternion();
	private final WWVector tempPoint = new WWVector();
	private final WWVector tempPoint2 = new WWVector();
	private final WWVector overlapPoint = new WWVector();
	private final WWVector overlapVector = new WWVector();
	private final WWVector overlapPoint2 = new WWVector();
	private final WWVector overlapVector2 = new WWVector();
	private final WWVector adjustVector = new WWVector();

	private final WWVector originalPosition = new WWVector();
	private final WWQuaternion originalRotation = new WWQuaternion();
	private final WWVector originalVelocity = new WWVector();
	private final WWVector originalAngularVelocity = new WWVector();

	private final WWVector force = new WWVector();
	private final WWVector unitOverlapVector = new WWVector();
	private final WWVector mirrorVector = new WWVector();
	private final WWVector antiForceVector = new WWVector();
	private final WWVector rotatedForce = new WWVector();
	private final WWVector rotatedVelocity = new WWVector();
	private final WWVector rotatedThrust = new WWVector();
	private final WWVector rotatedThrustVelocity = new WWVector();

	public OldPhysicsThread(WWWorld world, int iterationTime, int slideStyle) {
		super(world, iterationTime);
	}

	@Override
	public void performIteration(long timeIncrement) {
		if (timeIncrement <= 0) {
			return;
		}
//		synchronized (world) {
		world.updateWorldTime(timeIncrement);

		long worldTime = world.getWorldTime();
		float deltaTime = timeIncrement / 1000.0f;

		ArrayList<ObjectCollision> tempCollidedObjects = previousPreviousCollidedObjects;
		collisionPool.recycle(tempCollidedObjects);
		tempCollidedObjects.clear();
		previousPreviousCollidedObjects = previousCollidedObjects;
		previousCollidedObjects = newCollidedObjects;
		newCollidedObjects = tempCollidedObjects;

		WWObject[] objects = world.getObjects();
		int lastObjectIndex = world.lastObjectIndex;
		for (int i = 0; i <= lastObjectIndex; i++) {
			WWObject object = objects[i];

			// For physical objects, determine forces upon the object.  This done by summing 
			// up all forces on the object, ignoring those where freedom is restricted.
			// Only examine physical objects that are also solid.  Non-solid (liquid, gas)
			// objects are not influenced by other objects (but do influence other objects
			// by being tested in the inner loop below).
			if (object != null && object.physical && object.solid && !object.deleted) {

//					synchronized (object) { // to keep it from being modified

				long originalLastMoveTime = object.lastMoveTime;

				// Get current orientation and momentum values.
				object.getAbsolutePosition(position, worldTime);
				object.getRotationPoint(rotationPoint);
				object.getAbsoluteRotation(rotation, worldTime);
				object.getVelocity(velocity);
				object.getAngularVelocity(angularVelocity);
				WWVector thrust = object.getThrust();
				WWVector thrustVelocity = object.getThrustVelocity();
				WWVector torque = object.getTorque();
				WWVector torqueVelocity = object.getTorqueVelocity();

				position.copyInto(originalPosition);
				rotation.copyInto(originalRotation);
				velocity.copyInto(originalVelocity);
				angularVelocity.copyInto(originalAngularVelocity);

				force.zero();

				// sum in gravitational force only if object has mass							
				if (object.getDensity() > 0.0) {
					force.z -= world.getGravity();
				}

				// - Next, apply interactions of other objects that overlap the physical object
				float objectExtent = object.extent;
				for (int j = 0; j <= lastObjectIndex; j++) {
					WWObject object2 = objects[j];
					if (object2 != null && !object2.phantom && object2 != object && !object2.deleted && !object.isDescendant(object2)) {

						// First, see if the objects are close enough to possibly overlap.
						// If they are, it is worth determining if they actually do overlap
						float maxExtentx = objectExtent + object2.extentx;
						float maxExtenty = objectExtent + object2.extenty;
						float maxExtentz = objectExtent + object2.extentz;
						object2.getAbsolutePosition(position2, worldTime);
						float px = Math.abs(position2.x - position.x);
						float py = Math.abs(position2.y - position.y);
						float pz = Math.abs(position2.z - position.z);
						if (/* object2.parentId != 0 || */(px <= maxExtentx && py <= maxExtenty && pz <= maxExtentz)) {

							// Determine if the objects overlap, and the vector of overlap.  This
							// vector points in the direction of the deepest overlap, and the length of the
							// vector indicates the amount of overlap
							object2.getAbsoluteRotation(rotation2, worldTime);

							// Test object1's points penetrating object2
							object.getOverlap(object2, position, rotation, rotationPoint, position2, rotation2, worldTime, tempPoint, tempPoint2, overlapPoint, overlapVector);

							// Test object2's points penetrating object1 (important for small objects vs large meshes)
							object2.getOverlap(object, position2, rotation2, object2.getRotationPoint(), position, rotation, worldTime, tempPoint, tempPoint2, overlapPoint2, overlapVector2);

							// Keep the deepest overlap
							if (overlapVector2.isLongerThan(overlapVector)) {
								overlapVector2.copyInto(overlapVector);
								overlapPoint2.copyInto(overlapPoint);
								overlapVector.scale(-1.0f);
							}

							if (!overlapVector.isZero()) {

								ObjectCollision collision = collisionPool.obtain(object, object2, overlapVector);
								newCollidedObjects.add(collision);

								// If the object2 is solid, apply solid-to-solid physics
								if (object2.solid) {

									// Adjust the position of the objects so that they are not overlapping
									adjustVector.set(overlapVector).scale(0.5f);
									if (!object.isFreedomMoveX() || !object.isFreedomMoveY() || !object.isFreedomMoveZ()) {
										WWObject.antiRotate(position, rotation);
										WWObject.antiRotate(adjustVector, rotation);
										if (object.isFreedomMoveX()) position.x -= adjustVector.x;
										if (object.isFreedomMoveY()) position.y -= adjustVector.y;
										if (object.isFreedomMoveZ()) position.z -= adjustVector.z;
										WWObject.rotate(position, rotation);
										WWObject.rotate(adjustVector, rotation);
									} else {
										position.subtract(adjustVector);
									}

									if (!object2.physical) {
										if (!object.isFreedomMoveX() || !object.isFreedomMoveY() || !object.isFreedomMoveZ()) {
											WWObject.antiRotate(position, rotation);
											WWObject.antiRotate(adjustVector, rotation);
											if (object.isFreedomMoveX()) position.x -= adjustVector.x;
											if (object.isFreedomMoveY()) position.y -= adjustVector.y;
											if (object.isFreedomMoveZ()) position.z -= adjustVector.z;
											WWObject.rotate(position, rotation);
											WWObject.rotate(adjustVector, rotation);
										} else {
											position.subtract(adjustVector);
										}
									}

									// If the object is moving toward object2, stop or repell it (according to elasticity)
									unitOverlapVector.set(overlapVector).normalize();
									if (object.getElasticity() > 0.0) { // bounce off the object  
										mirrorVector.set(unitOverlapVector).scale(-1.0f);
										velocity.reflect(mirrorVector).scale(object.getElasticity());
									} else { // slide on the object
										antiForceVector.set(unitOverlapVector).scale(-force.length());
										force.add(antiForceVector);
										// TODO add velocity attuning (and do not stop in all dimensions)
										//velocity.scale(Math.min(1.0f, 1.0f + unitOverlapVector.x), Math.min(1.0f, 1.0f + unitOverlapVector.y), Math.min(1.0f, 1.0f + unitOverlapVector.z));
										velocity.scale(1.0f, 1.0f, 1.0f + unitOverlapVector.z);
									}

									// Adjust angular velocity as well
									// TODO implement angular velocity adjustment

								} // solid

								// If the object2 is non-solid, apply solid-to-liquid/gas physics
								else {

									// boyancy
									if (object.isFreedomMoveZ() && object2.getDensity() > 0.0) {
										// Note: pressure is determined by how deep into the object.  This is an
										// estimate here, based on the extent.  This will be correct only if the object is level (flat)
										float pressure = FastMath.max(position2.z + object2.size.z / 2.0f - position.z, 0.0f);
										float boyancy = object2.getDensity() * pressure - object.getDensity();
										if (boyancy > 0) {
											velocity.z += (boyancy * deltaTime) * 30.0;
										}
									}

								} // nonsolid

								// Depending on friction forces, slow the object movement
								float friction = FastMath.min(object.getFriction(), object2.getFriction()) * deltaTime * 10.0f;
								if (friction > 0.0) {
									velocity.average(object2.getVelocity(), friction);
									angularVelocity.average(object2.getAngularVelocity(), friction);
									// degrade forces
									force.average(WWVector.ZERO_VECTOR, friction);
								} // friction

							} // if overlapping
						} // if near each other

					} // if object != object2
				} // for object2

				// Cap force to avoid really bad behaviors
				float forceLen = force.length();
				if (forceLen > 10.0f) {
					force.scale(10.0f / forceLen);
				}

				// Apply forces to object's velocity
				rotatedForce.set(force);
				rotatedVelocity.set(velocity);
				WWObject.antiRotate(rotatedForce, rotation);
				WWObject.antiRotate(rotatedVelocity, rotation);
				if (!object.isFreedomMoveX()) {
					rotatedForce.x = 0.0f;
					rotatedVelocity.x = 0.0f;
				}
				if (!object.isFreedomMoveY()) {
					rotatedForce.y = 0.0f;
					rotatedVelocity.y = 0.0f;
				}
				if (!object.isFreedomMoveZ()) {
					rotatedForce.z = 0.0f;
					rotatedVelocity.z = 0.0f;
				}
				rotatedVelocity.x += rotatedForce.x * deltaTime;
				rotatedVelocity.y += rotatedForce.y * deltaTime;
				rotatedVelocity.z += rotatedForce.z * deltaTime;
				rotation.rotateVector(rotatedVelocity);
				velocity.set(rotatedVelocity);

				// Apply thrust to object.  Apply last to overcome any friction
				rotatedThrust.set(thrust);
				rotatedThrustVelocity.set(thrustVelocity);
				rotation.rotateVector(rotatedThrust);
				rotation.rotateVector(rotatedThrustVelocity);
				if (object.isFreedomMoveX() || object.isFreedomMoveY() || object.isFreedomMoveZ()) {
					if (rotatedThrustVelocity.x < -0.1 && rotatedThrustVelocity.x < velocity.x) {
						velocity.x = FastMath.max(rotatedThrustVelocity.x, velocity.x + rotatedThrust.x * 10.0f * deltaTime);
					} else if (rotatedThrustVelocity.x > 0.1 && rotatedThrustVelocity.x > velocity.x) {
						velocity.x = FastMath.min(rotatedThrustVelocity.x, velocity.x + rotatedThrust.x * 10.0f * deltaTime);
					}
					if (rotatedThrustVelocity.y < -0.1 && rotatedThrustVelocity.y < velocity.y) {
						velocity.y = FastMath.max(rotatedThrustVelocity.y, velocity.y + rotatedThrust.y * 10.0f * deltaTime);
					} else if (rotatedThrustVelocity.y > 0.1 && rotatedThrustVelocity.y > velocity.y) {
						velocity.y = FastMath.min(rotatedThrustVelocity.y, velocity.y + rotatedThrust.y * 10.0f * deltaTime);
					}
					if (rotatedThrustVelocity.z < -0.1 && rotatedThrustVelocity.z < velocity.z) {
						velocity.z = FastMath.max(rotatedThrustVelocity.z, velocity.z + rotatedThrust.z * 10.0f * deltaTime);
					} else if (rotatedThrustVelocity.z > 0.1 && rotatedThrustVelocity.z > velocity.z) {
						velocity.z = FastMath.min(rotatedThrustVelocity.z, velocity.z + rotatedThrust.z * 10.0f * deltaTime);
					}
				} else {
					// if the object has no freedom to move, use rotatedThrustVelocity so that it at least will move for user
					velocity.x = rotatedThrustVelocity.x;
					velocity.y = rotatedThrustVelocity.y;
					velocity.z = rotatedThrustVelocity.z;
				}

				// Apply torque to object.  Again, ignoring freedom of movement settings
//					object.antiRotate(torque, rotation); // so torque is relative to current object orientation
//					object.antiRotate(torqueVelocity, rotation); // so torque is relative to current object orientation
//				if (torque.length() != 0.0) {
				if (object.isFreedomRotateX()) {
					if (torqueVelocity.x < 0.0 && torqueVelocity.x < angularVelocity.x) {
						angularVelocity.x = FastMath.max(torqueVelocity.x, angularVelocity.x + torque.x * 10.0f * deltaTime);
					} else if (torqueVelocity.x > 0.0 && torqueVelocity.x > angularVelocity.x) {
						angularVelocity.x = FastMath.min(torqueVelocity.x, angularVelocity.x + torque.x * 10.0f * deltaTime);
					}
				} else {
					angularVelocity.x = torqueVelocity.x;
				}
				if (object.isFreedomRotateY()) {
					if (torqueVelocity.y < 0.0 && torqueVelocity.y < angularVelocity.y) {
						angularVelocity.y = FastMath.max(torqueVelocity.y, angularVelocity.y + torque.y * 10.0f * deltaTime);
					} else if (torqueVelocity.y > 0.0 && torqueVelocity.y > angularVelocity.y) {
						angularVelocity.y = FastMath.min(torqueVelocity.y, angularVelocity.y + torque.y * 10.0f * deltaTime);
					}
				} else {
					angularVelocity.y = torqueVelocity.y;
				}
				if (object.isFreedomRotateZ()) {
					if (torqueVelocity.z < 0.0 && torqueVelocity.z < angularVelocity.z) {
						angularVelocity.z = FastMath.max(torqueVelocity.z, angularVelocity.z + torque.z * 10.0f * deltaTime);
					} else if (torqueVelocity.z > 0.0 && torqueVelocity.z > angularVelocity.z) {
						angularVelocity.z = FastMath.min(torqueVelocity.z, angularVelocity.z + torque.z * 10.0f * deltaTime);
					}
				} else {
					angularVelocity.z = torqueVelocity.z;
				}
//				}

				// Update the position, rotation, velocity and angular velocity values on the object if any have changed due to
				// physical interaction with another object, but only if the object has not been moved by some other thread
				if (object.lastMoveTime == originalLastMoveTime && (!position.equals(originalPosition) || !rotation.equals(originalRotation) || !velocity.equals(originalVelocity) || !angularVelocity.equals(originalAngularVelocity))) {
					if (position.length() > 10000) {
						System.err.println("OldPhysicsThread blew object out of range. Object: " + object.getName());
					} else {
						object.setOrientation(position, rotation, velocity, angularVelocity, worldTime);
					}
				}

//					} // synchronize object

			} // if physical

			// update timers and invoke timer events on object behaviors
			if (object != null && object.behaviors != null && !object.deleted) {
				int behaviorCount = object.behaviors.length;
				for (int b = 0; b < behaviorCount; b++) {
					WWBehavior behavior = object.behaviors[b].behavior;
					if (behavior.timer > 0) {
						behavior.timer = (int) Math.max(0, behavior.timer - timeIncrement);
						if (behavior.timer == 0) {
							if (world.behaviorThread != null) {
								world.behaviorThread.queue("timer", object, null, null);
							} else {
								object.invokeBehavior("timer", null, null);
							}
						}
					}
				}
			}

		} // for object

		// fire collide and slide events
		int ncosize = newCollidedObjects.size();
		int pcosize = previousCollidedObjects.size();
		int ppcosize = previousPreviousCollidedObjects.size();
		for (int i = 0; i < ncosize; i++) {
			ObjectCollision newCollision = newCollidedObjects.get(i);
			for (int j = 0; j < pcosize; j++) {
				ObjectCollision previousCollision = previousCollidedObjects.get(j);
				if (newCollision.equals(previousCollision)) {
					newCollision.firstSlidingStreamId = previousCollision.firstSlidingStreamId;
					newCollision.secondSlidingStreamId = previousCollision.secondSlidingStreamId;
					world.slideObject(newCollision);
					newCollision.sliding = true;
					previousCollision.stillSliding = true;
				}
			}
			if (!newCollision.sliding) { // check previous previous
				for (int j = 0; j < ppcosize; j++) {
					ObjectCollision previousPreviousCollision = previousPreviousCollidedObjects.get(j);
					if (newCollision.equals(previousPreviousCollision)) {
						newCollision.firstSlidingStreamId = previousPreviousCollision.firstSlidingStreamId;
						newCollision.secondSlidingStreamId = previousPreviousCollision.secondSlidingStreamId;
						world.slideObject(newCollision);
						newCollision.sliding = true;
						previousPreviousCollision.stillSliding = true;
					}
				}
			}
			if (!newCollision.sliding) {
				world.collideObject(newCollision);
			}
		}
		for (int j = 0; j < ppcosize; j++) {
			ObjectCollision previousPreviousCollision = previousPreviousCollidedObjects.get(j);
			if (previousPreviousCollision.sliding && !previousPreviousCollision.stillSliding) {
				world.stopSlidingObject(previousPreviousCollision);
			}
		}

//		} // synchronize world

	}

}
