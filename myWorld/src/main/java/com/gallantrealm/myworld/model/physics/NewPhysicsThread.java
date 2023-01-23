package com.gallantrealm.myworld.model.physics;

import java.util.ArrayList;
import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.model.WWBehavior;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWQuaternion;
import com.gallantrealm.myworld.model.WWVector;
import com.gallantrealm.myworld.model.WWWorld;

/**
 * This thread performs updates to the world according to physical properties. This involves detecting collision and forces between objects and adjusting the position, orientation, velocity and angular momentum to match.
 * <p>
 * Note that this thread does not actually handle moving or rotating objects due to their own velocity and angular momentum. That is handled within WWObject itself.
 * <p>
 * This is the "New" version of physics. It mimics real world more precisely. In particular, it applies thrust and torque first, before other forces are determined. This causes the thrust and torque to be limited by other forces, such as
 * what occurs in the real world.
 */
public class NewPhysicsThread extends PhysicsThread {

	private static final boolean queueUpdates = false;

	ArrayList<ObjectCollision> previousPreviousCollidedObjects = new ArrayList<ObjectCollision>();
	ArrayList<ObjectCollision> previousCollidedObjects = new ArrayList<ObjectCollision>();
	ArrayList<ObjectCollision> newCollidedObjects = new ArrayList<ObjectCollision>();

	int slideStyle;

	public NewPhysicsThread(WWWorld world, int iterationTime, int slideStyle) {
		super(world, iterationTime);
		this.slideStyle = slideStyle;
	}

	class QueuedUpdate {
		public WWObject object;
		public WWVector position;
		public WWQuaternion rotation;
		public WWVector velocity;
		public WWVector aMomentum;
		public long worldTime;

		public QueuedUpdate(WWObject object, WWVector position, WWQuaternion rotation, WWVector velocity, WWVector aMomentum, long worldTime) {
			this.object = object;
			this.position = position;
			this.rotation = rotation;
			this.velocity = velocity;
			this.aMomentum = aMomentum;
			this.worldTime = worldTime;
		}
	}

	@Override
	public void performIteration(long timeIncrement) {
		if (timeIncrement <= 0) {
			return;
		}
		long worldTime = world.getWorldTime() + timeIncrement;
		float deltaTime = timeIncrement / 1000.0f;

		ArrayList<QueuedUpdate> queuedUpdates = null;
		if (queueUpdates) {
			queuedUpdates = new ArrayList<QueuedUpdate>();
		}
		previousPreviousCollidedObjects = previousCollidedObjects;
		previousCollidedObjects = newCollidedObjects;
		newCollidedObjects = new ArrayList<ObjectCollision>();

		WWVector position = new WWVector();
		WWQuaternion rotation = new WWQuaternion();
		WWVector rotationPoint = new WWVector();
		WWVector velocity = new WWVector();
		WWVector aMomentum = new WWVector();
		WWVector position2 = new WWVector();
		WWQuaternion rotation2 = new WWQuaternion();
		WWVector tempPoint = new WWVector();
		WWVector tempPoint2 = new WWVector();
		WWVector overlapPoint = new WWVector();
		WWVector overlapVector = new WWVector();

		WWObject[] objects = world.getObjects();
		int lastObjectIndex = world.lastObjectIndex;
		for (int i = 0; i <= lastObjectIndex; i++) {
			WWObject object = objects[i];

			// For physical objects, determine forces upon the object. This done by summing
			// up all forces on the object, ignoring those where freedom is restricted.
			// Only examine physical objects that are also solid. Non-solid (liquid, gas)
			// objects are not influenced by other objects (but do influence other objects
			// by being tested in the inner loop below).
			if (object != null && object.physical && object.solid && !object.deleted) {

//				synchronized (object) { // to keep it from being modified

				long originalLastMoveTime = object.lastMoveTime;

				// Get current orientation and momentum values.
				object.getAbsolutePosition(position, worldTime);
				object.getAbsoluteRotation(rotation, worldTime);
				object.getRotationPoint(rotationPoint);
				object.getVelocity(velocity);
				object.getAMomentum(aMomentum);
				WWVector thrust = object.getThrust();
				WWVector thrustVelocity = object.getThrustVelocity();
				WWVector torque = object.getTorque();
				WWVector torqueVelocity = object.getTorqueVelocity();

				WWVector originalPosition = position.clone();
				WWQuaternion originalRotation = rotation.clone();
				WWVector originalVelocity = velocity.clone();
				WWVector originalAMomentum = aMomentum.clone();

				// start with thrust and torque by the object itself
				// note that these are attenuated (by thrust and torque velocity) if the velocity
				// and torque are already high.
				WWVector totalForce = thrust.clone();
				if (totalForce.length() > 0) {
					rotation.rotateVector(totalForce);
					rotation.rotateVector(thrustVelocity);
					if (thrustVelocity.x > 0 && velocity.x > thrustVelocity.x) {
						totalForce.x = 0;
					} else if (thrustVelocity.x < 0 && velocity.x < thrustVelocity.x) {
						totalForce.x = 0;
					}
					if (thrustVelocity.y > 0 && velocity.y > thrustVelocity.y) {
						totalForce.y = 0;
					} else if (thrustVelocity.y < 0 && velocity.y < thrustVelocity.y) {
						totalForce.y = 0;
					}
					if (thrustVelocity.z > 0 && velocity.z > thrustVelocity.z) {
						totalForce.z = 0;
					} else if (thrustVelocity.z < 0 && velocity.z < thrustVelocity.z) {
						totalForce.z = 0;
					}
				}
				WWVector totalTorque = torque.clone();
				if (totalTorque.length() > 0) {
					rotation.rotateVector(totalTorque);
					rotation.rotateVector(torqueVelocity);
					if (torqueVelocity.x > 0 && aMomentum.x > torqueVelocity.x) {
						totalTorque.x = 0;
					} else if (torqueVelocity.x < 0 && aMomentum.x < torqueVelocity.x) {
						totalTorque.x = 0;
					}
					if (torqueVelocity.y > 0 && aMomentum.y > torqueVelocity.y) {
						totalTorque.y = 0;
					} else if (torqueVelocity.y < 0 && aMomentum.y < torqueVelocity.y) {
						totalTorque.y = 0;
					}
					if (torqueVelocity.z > 0 && aMomentum.z > torqueVelocity.z) {
						totalTorque.z = 0;
					} else if (torqueVelocity.z < 0 && aMomentum.z < torqueVelocity.z) {
						totalTorque.z = 0;
					}
				}

				WWVector gravityForce = world.getGravityForce(position.x, position.y, position.z);

				// sum in gravitational force only if object has mass
				if (object.getDensity() > 0.0) {
					totalForce.add(gravityForce);
				}

				// - Next, apply interactions of other objects that overlap the physical object
				float objectExtent = object.extent;
				for (int j = 1; j <= lastObjectIndex; j++) {
					WWObject object2 = objects[j];
					if (object2 != null && !object2.phantom && object2 != object && !object2.deleted && !object.isDescendant(object2)) {

						// First, see if the objects are close enough to possibly overlap.
						// If they are, it is worth determining if they actually do overlap
						float maxExtentx = objectExtent + object2.extentx;
						float maxExtenty = objectExtent + object2.extenty;
						float maxExtentz = objectExtent + object2.extentz;
						object2.getAbsolutePosition(position2, worldTime);
						float px = position2.x - position.x;
						float py = position2.y - position.y;
						float pz = position2.z - position.z;
						if (px < 0)
							px = -px;
						if (py < 0)
							py = -py;
						if (pz < 0)
							pz = -pz;
						if (/* object2.parentId != 0 || */(px <= maxExtentx && py <= maxExtenty && pz <= maxExtentz)) {

							// Determine if the objects overlap, and the vector of overlap. This
							// vector points in the direction of the deepest overlap, and the length of the
							// vector indicates the amount of overlap
							object2.getAbsoluteRotation(rotation2, worldTime);
							object.getOverlap(object2, position, rotation, rotationPoint, position2, rotation2, worldTime, tempPoint, tempPoint2, overlapPoint, overlapVector);

							if (!overlapVector.isZero()) {

								ObjectCollision collision = new ObjectCollision(object, object2, overlapVector);
								newCollidedObjects.add(collision);

								// If the object2 is solid, apply solid-to-solid physics
								if (object2.solid) {

									// Adjust the position of the objects so that they are not overlapping
									if (!object.freedomMoveX || !object.freedomMoveY || !object.freedomMoveZ) {
										WWObject.antiRotate(position, rotation, worldTime);
										WWObject.antiRotate(overlapVector, rotation, worldTime);
										if (object.freedomMoveX) {
											position.x -= overlapVector.x;
										}
										if (object.freedomMoveY) {
											position.y -= overlapVector.y;
										}
										if (object.freedomMoveZ) {
											position.z -= overlapVector.z;
										}
										WWObject.rotate(position, rotation, worldTime);
										WWObject.rotate(overlapVector, rotation, worldTime);
									} else {
										position.x -= overlapVector.x;
										position.y -= overlapVector.y;
										position.z -= overlapVector.z;
									}

									// If the object is moving toward object2, stop or repell it (according to elasticity)
									WWVector unitOverlapVector;
									if (overlapVector.length() > 0) {
										unitOverlapVector = new WWVector(overlapVector);
									} else { // use force
										unitOverlapVector = overlapVector.clone(); // new WWVector(-force.x, -force.y, -force.z);
									}
									unitOverlapVector.normalize();
									if (FastMath.avg(object.elasticity, object2.elasticity) > 0.0) { // bounce both objects off of each other
										if (object2.physical) {
											float objectMass = object.density * object.sizeX * object.sizeY * object.sizeZ;
											float object2Mass = object2.density * object2.sizeX * object2.sizeY * object2.sizeZ;
											WWVector forceVector = velocity.clone().scale(objectMass);
											WWVector force2Vector = object2.getVelocity().scale(object2Mass);
											WWVector totalForceVector = forceVector.add(force2Vector);
											WWVector mirrorVector = unitOverlapVector.clone();
											mirrorVector.scale(-1.0f);
											WWVector mirrorForceVector = totalForceVector.getReflection(mirrorVector);
											float elasticity = FastMath.avg(object.elasticity, object2.elasticity);
											velocity.x = mirrorForceVector.x * elasticity;
											velocity.y = mirrorForceVector.y * elasticity;
											velocity.z = mirrorForceVector.z * elasticity;
										} else { // simple bounce-back
											WWVector mirrorVector = unitOverlapVector.clone();
											mirrorVector.scale(-1.0f);
											WWVector mirrorVelocityVector = velocity.getReflection(mirrorVector);
											float elasticity = Math.max(object.elasticity, object2.elasticity);
											velocity.x = mirrorVelocityVector.x * elasticity;
											velocity.y = mirrorVelocityVector.y * elasticity;
											velocity.z = mirrorVelocityVector.z * elasticity;
										}
									} else { // slide on the object

										if (slideStyle == 0) { // used in rtr

											WWVector antiForceVector = unitOverlapVector.clone();
											antiForceVector.scale(0.99f); // a small fudge factor to keep object sliding
											antiForceVector.scale(-totalForce.length());
											totalForce.add(antiForceVector);
											velocity.scale(1.0f, 1.0f, 1.0f - Math.abs(unitOverlapVector.z));

										} else if (slideStyle == 1) { // used in bonkers

											WWVector unitVelocity = velocity.clone().normalize();
											float repelMag = 1 - unitVelocity.add(unitOverlapVector).length();
											WWVector repelForce = unitOverlapVector.clone().scale(velocity.length() / deltaTime * repelMag);
											totalForce.add(repelForce);

										} else if (slideStyle == 2) { // most recent (best?)

											WWVector unitVelocity = velocity.clone().normalize();
											float repelMag = -Math.abs(unitVelocity.dot(unitOverlapVector));
											WWVector repelForce = unitOverlapVector.clone().scale(velocity.length() / deltaTime * repelMag);
											totalForce.add(repelForce);

										}

									}

									// Adjust angular momentum due to the collision.
									// This is the cross product of the unitoverlapvector and the unit overlappoint->centerpoint,
									// scaled by the velocity into the collided object
									WWVector collisionRelativePosition = originalPosition.clone().subtract(overlapPoint).normalize();
									WWVector collisionVelocity = unitOverlapVector.clone().cross(collisionRelativePosition);
									totalTorque.add(collisionVelocity.scale(1/deltaTime));

									// Place torque on the object as well due to the gravitaional force.
									totalTorque.add(collisionRelativePosition.clone().cross(gravityForce).scale(-1/deltaTime));

									// TODO implement physics of tops

								} // solid

								// If the object2 is non-solid, apply solid-to-liquid/gas physics
								else {

									// boyancy
									if (object.isFreedomMoveZ() && object2.getDensity() > 0.0) {
										// Note: pressure is determined by how deep into the object. This is an
										// estimate here, based on the extent. This will be correct only if the object is level (flat)
										float pressure = FastMath.max(position2.z + object2.sizeZ / 2.0f - position.z, 0.0f);
										float boyancy = object2.getDensity() * pressure - object.getDensity();
										if (boyancy > 0) {
											velocity.z += (boyancy * deltaTime) * 30.0;
										}
									}

								} // nonsolid

								// Depending on friction forces, slow the object movement
								if (object2.isSolid() && object.getElasticity() + object2.getElasticity() > 0) {
									// friction for solids touching with elasticity only impacts angular velocity
									// TODO determine appropriate angular velocity change for solids colliding with elasticity
								} else {
									float friction = FastMath.min(object.friction, object2.friction);
									if (friction > 0) {

										// Note: the scale 10 factors below are fudged so objects will have greater friction.
										// This is just a fudge factor that needs to be better modelled
										// TODO better model friction to avoid the scale 10 fudge factors

										if (object2.isSolid()) {  // solid-against-solid friction

											WWVector collisionDirection = originalPosition.clone().subtract(overlapPoint).normalize();

											// Determine the relative speed of the two object surfaces.  This is a factor
											// of both the linear velocity and the angular velocity of the two objects.
											WWVector relativeVelocity = velocity.clone().subtract(object2.getVelocity()).scale(10);    // need to add scale 10 to avoid sliding too much
											relativeVelocity.add(aMomentum.clone().cross(collisionDirection).subtract(object2.getAMomentum().cross(collisionDirection)).scale(object.getExtent() / FastMath.PI / 2));

											// Determine the relative angular momentum of the two objects at the impact point as well.
											// The angular momentum of the object is tempered, averaging over these two momenta over time
											WWVector relativeAMomentum = aMomentum.clone().subtract(object2.getAMomentum()).scale(collisionDirection).scale(10);

											relativeVelocity.scale(friction);
											relativeAMomentum.scale(friction);

											totalForce.subtract(relativeVelocity);
											totalTorque.subtract(relativeVelocity);
											totalTorque.subtract(relativeAMomentum);

										} else { // solid-in-liquid/gas friction

											WWVector frictionVForce = object2.getVelocity().subtract(velocity);
											frictionVForce.scale(10 * friction);
											totalForce.add(frictionVForce);
											// TODO: Add angular velocity effect on force in liquids, to emulate paddlewheels

											WWVector frictionAForce = new WWVector();
											frictionAForce.subtract(aMomentum);
											frictionAForce.scale(10 * friction);
											totalTorque.add(frictionAForce);

										}
									}
								} // friction

							} // if overlapping
						} // if near each other

					} // if object != object2
				} // for object2

				// Apply forces to object's velocity
				velocity.x += totalForce.x * deltaTime;
				velocity.y += totalForce.y * deltaTime;
				velocity.z += totalForce.z * deltaTime;

				// Limit velocity according to freedom
				if (!object.freedomMoveX || !object.freedomMoveY || !object.freedomMoveZ) {
					WWObject.antiRotate(velocity, rotation, worldTime);
					if (!object.freedomMoveX) {
						velocity.x = 0.0f;
					}
					if (!object.freedomMoveY) {
						velocity.y = 0.0f;
					}
					if (!object.freedomMoveZ) {
						velocity.z = 0.0f;
					}
					WWObject.rotate(velocity, rotation, worldTime);
				}

				aMomentum.x += totalTorque.x * deltaTime;
				aMomentum.y += totalTorque.y * deltaTime;
				aMomentum.z += totalTorque.z * deltaTime;

				if (!object.freedomRotateX) {
					aMomentum.x = 0;
				}
				if (!object.freedomRotateY) {
					aMomentum.y = 0;
				}
				if (!object.freedomRotateZ) {
					aMomentum.z = 0;
				}

				// Update the position, rotation, velocity and angular momentum values on the object if any have changed due to
				// physical interaction with another object, but only if the object has not been moved by some other thread
				if (object.lastMoveTime == originalLastMoveTime && (!position.equals(originalPosition) || !rotation.equals(originalRotation) || !velocity.equals(originalVelocity) || !aMomentum.equals(originalAMomentum))) {
					if (position.length() > 10000) {
						System.err.println("NewPhysicsThread blew object out of range.  Object: "+object.getName());
					} else {
						if (queueUpdates) {
							queuedUpdates.add(new QueuedUpdate(object, position.clone(), rotation.clone(), velocity.clone(), aMomentum.clone(), worldTime));
						} else {
							object.setOrientation(position, rotation, velocity, aMomentum, worldTime);
						}
					}
				}

//				} // synchronize object

			} // if physical

		} // for object

		world.updateWorldTime(timeIncrement);

		// performed queued updates. This done with world locked so that the updates are "instantaneous"
		if (queueUpdates) {
			synchronized (world) {
				int qsize = queuedUpdates.size();
				for (int i = 0; i < qsize; i++) {
					QueuedUpdate queuedUpdate = queuedUpdates.get(i);
					if (queuedUpdate.worldTime > queuedUpdate.object.lastMoveTime) { // don't update if changed externally
						queuedUpdate.object.setOrientation(queuedUpdate.position, queuedUpdate.rotation, queuedUpdate.velocity, queuedUpdate.aMomentum, queuedUpdate.worldTime);
					}
				}
			}
		}

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

		// update timers and invoke timer events on object behaviors
		for (int i = 0; i <= lastObjectIndex; i++) {
			WWObject object = objects[i];
			if (object != null && !object.deleted && object.behaviors != null) {
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
		}

	}
}
