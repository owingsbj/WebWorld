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
 * forces between objects and adjusting the position, orientation, velocity and angular momentum to match.
 * <p>
 * Note that this thread does not actually handle moving or rotating objects due to their own velocity and angular
 * momentum. This is handled within the WWObject itself.
 * <p>
 * This is the "Old" version of physics. It applies thrust last, which allows it to better handle walking-style of
 * avatar movement.
 */
public class OldPhysicsThread extends PhysicsThread {

	private ArrayList<ObjectCollision> previousCollidedObjects = new ArrayList<ObjectCollision>();
	private ArrayList<ObjectCollision> newCollidedObjects = new ArrayList<ObjectCollision>();

	private final WWVector position = new WWVector();
	private final WWQuaternion rotation = new WWQuaternion();
	private final WWVector rotationPoint = new WWVector();
	private final WWVector velocity = new WWVector();
	private final WWVector aMomentum = new WWVector();
	private final WWVector position2 = new WWVector();
	private final WWQuaternion rotation2 = new WWQuaternion();
	private final WWVector tempPoint = new WWVector();
	private final WWVector tempPoint2 = new WWVector();
	private final WWVector overlapPoint = new WWVector();
	private final WWVector overlapVector = new WWVector();

	private final WWVector originalPosition = new WWVector();
	private final WWQuaternion originalRotation = new WWQuaternion();
	private final WWVector originalVelocity = new WWVector();
	private final WWVector originalAMomentum = new WWVector();

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
//		synchronized (world) {
		world.updateWorldTime(timeIncrement);

		long worldTime = world.getWorldTime();
		float deltaTime = timeIncrement / 1000.0f;

		ArrayList<ObjectCollision> tempCollidedObjects = previousCollidedObjects;
		previousCollidedObjects = newCollidedObjects;
		newCollidedObjects = tempCollidedObjects;
		newCollidedObjects.clear();

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
				object.getAngularVelocity(aMomentum);
				WWVector thrust = object.getThrust();
				WWVector thrustVelocity = object.getThrustVelocity();
				WWVector torque = object.getTorque();
				WWVector torqueVelocity = object.getTorqueVelocity();

				position.copyInto(originalPosition);
				rotation.copyInto(originalRotation);
				velocity.copyInto(originalVelocity);
				aMomentum.copyInto(originalAMomentum);

				force.zero();

				// sum in gravitational force only if object has mass							
				if (object.getDensity() > 0.0) {
					force.z -= world.getGravity();
				}

				// - Next, apply interactions of other objects that overlap the physical object
				float objectExtent = object.extent;
				for (int j = 1; j <= lastObjectIndex; j++) {
					WWObject object2 = objects[j];
					if (object2 != null && !object2.phantom && object2 != object && !object2.deleted && !object.isDescendant(object2)) {

						// First, see if the objects are close enough to possibly overlap.
						// If they are, it is worth determining if they actually do overlap
						float maxExtent = objectExtent + object2.extent; //FastMath.max(objectExtent, object2.extent);
						object2.getAbsolutePosition(position2, worldTime);
						if (/* object2.parentId != 0 || */(Math.abs(position2.x - position.x) < maxExtent && Math.abs(position2.y - position.y) < maxExtent && Math.abs(position2.z - position.z) < maxExtent)) {

							// Determine if the objects overlap, and the vector of overlap.  This
							// vector points in the direction of the deepest overlap, and the length of the
							// vector indicates the amount of overlap
							object2.getAbsoluteRotation(rotation2, worldTime);
							object.getOverlap(object2, position, rotation, rotationPoint, position2, rotation2, worldTime, tempPoint, tempPoint2, overlapPoint, overlapVector);

							if (!overlapVector.isZero()) {

								ObjectCollision collision = new ObjectCollision(object, object2, overlapVector);
								int index = previousCollidedObjects.indexOf(collision);
								if (index < 0) {
									world.collideObject(collision);
								} else {
									ObjectCollision oldCollision = previousCollidedObjects.get(index);
									world.slideObject(oldCollision);
									previousCollidedObjects.remove(index);
									collision = oldCollision;
								}
								newCollidedObjects.add(collision);

								// If the object2 is solid, apply solid-to-solid physics
								if (object2.solid) {

									// Adjust the position of the objects so that they are not overlapping
									position.x -= overlapVector.x;
									position.y -= overlapVector.y;
									position.z -= overlapVector.z;

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

									// Adjust angular momentum as well
									// TODO implement angular momentum adjustment

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
									aMomentum.average(object2.getAngularVelocity(), friction);
									// degrade forces
									force.average(WWVector.ZERO_VECTOR, friction);
								} // friction

							} // if overlapping
						} // if near each other

					} // if object != object2
				} // for object2

				// Cap force to avoid really bad behaviors
				if (force.length() > 10) {
					force.scale(10 / force.length());
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
					if (torqueVelocity.x < 0.0 && torqueVelocity.x < aMomentum.x) {
						aMomentum.x = FastMath.max(torqueVelocity.x, aMomentum.x + torque.x * 10.0f * deltaTime);
					} else if (torqueVelocity.x > 0.0 && torqueVelocity.x > aMomentum.x) {
						aMomentum.x = FastMath.min(torqueVelocity.x, aMomentum.x + torque.x * 10.0f * deltaTime);
					}
				} else {
					aMomentum.x = torqueVelocity.x;
				}
				if (object.isFreedomRotateY()) {
					if (torqueVelocity.y < 0.0 && torqueVelocity.y < aMomentum.y) {
						aMomentum.y = FastMath.max(torqueVelocity.y, aMomentum.y + torque.y * 10.0f * deltaTime);
					} else if (torqueVelocity.y > 0.0 && torqueVelocity.y > aMomentum.y) {
						aMomentum.y = FastMath.min(torqueVelocity.y, aMomentum.y + torque.y * 10.0f * deltaTime);
					}
				} else {
					aMomentum.y = torqueVelocity.y;
				}
				if (object.isFreedomRotateZ()) {
					if (torqueVelocity.z < 0.0 && torqueVelocity.z < aMomentum.z) {
						aMomentum.z = FastMath.max(torqueVelocity.z, aMomentum.z + torque.z * 10.0f * deltaTime);
					} else if (torqueVelocity.z > 0.0 && torqueVelocity.z > aMomentum.z) {
						aMomentum.z = FastMath.min(torqueVelocity.z, aMomentum.z + torque.z * 10.0f * deltaTime);
					}
				} else {
					aMomentum.z = torqueVelocity.z;
				}
//				}

				// Update the position, rotation, velocity and angular momentum values on the object if any have changed due to
				// physical interaction with another object, but only if the object has not been moved by some other thread
				if (object.lastMoveTime == originalLastMoveTime && (!position.equals(originalPosition) || !rotation.equals(originalRotation) || !velocity.equals(originalVelocity) || !aMomentum.equals(originalAMomentum))) {

					// Cap the movements, to cure possible physics ills
					if (position.x - originalPosition.x > 1f) {
						position.x = originalPosition.x + 1f;
					} else if (position.x - originalPosition.x < -0.5f) {
						position.x = originalPosition.x - 0.5f;
					}
					if (position.y - originalPosition.y > 0.5f) {
						position.y = originalPosition.y + 0.5f;
					} else if (position.y - originalPosition.y < -0.5f) {
						position.y = originalPosition.y - 0.5f;
					}
					if (position.z - originalPosition.z > 0.5f) {
						position.z = originalPosition.z + 0.5f;
					} else if (position.z - originalPosition.z < -0.5f) {
						position.z = originalPosition.z - 0.5f;
					}

					object.setOrientation(position, rotation, velocity, aMomentum, worldTime);
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
							}
						}
					}
				}
			}

		} // for object

		int size = previousCollidedObjects.size();
		for (int k = 0; k < size; k++) {
			ObjectCollision previousCollision = previousCollidedObjects.get(k);
			world.stopSlidingObject(previousCollision);
		}
		previousCollidedObjects.clear();

//		} // synchronize world

	}

}
