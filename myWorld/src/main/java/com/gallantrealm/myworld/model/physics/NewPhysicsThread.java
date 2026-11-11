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

	static class QueuedUpdate {
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
		ArrayList<ObjectCollision> temp = previousPreviousCollidedObjects;
		temp.clear();
		previousPreviousCollidedObjects = previousCollidedObjects;
		previousCollidedObjects = newCollidedObjects;
		newCollidedObjects = temp;

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
		WWVector forceVector = new WWVector();
		WWVector force2Vector = new WWVector();
		WWVector mirrorVector = new WWVector();

		WWObject[] objects = world.getObjects();
		int lastObjectIndex = world.lastObjectIndex;
		for (int i = 0; i <= lastObjectIndex; i++) {
			WWObject object = objects[i];

			// For physical objects, determine forces upon the object. This done by summing
			// up all forces on the object, ignoring those where freedom is restricted.
			// Only examine physical objects that are also solid. Non-solid (liquid, gas)
			// objects are not influenced by other objects (but do influence other objects
			// by being tested in the inner loop below).
			if (object != null && object.physical && !object.deleted) {

//				synchronized (object) { // to keep it from being modified

				long originalLastMoveTime = object.lastMoveTime;

				// Get current orientation and momentum values.
				object.getAbsolutePosition(position, worldTime);
				object.getAbsoluteRotation(rotation, worldTime);
				object.getRotationPoint(rotationPoint);
				object.getVelocity(velocity);
				object.getAngularVelocity(aMomentum);
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

							// Determine if the objects overlap, and the vector of overlap. This
							// vector points in the direction of the deepest overlap, and the length of the
							// vector indicates the amount of overlap
							object2.getAbsoluteRotation(rotation2, worldTime);
							
							// Test object1's points penetrating object2
							object.getOverlap(object2, position, rotation, rotationPoint, position2, rotation2, worldTime, tempPoint, tempPoint2, overlapPoint, overlapVector);
							
							// Test object2's points penetrating object1 (important for small objects vs large meshes)
							WWVector overlapPoint2 = new WWVector();
							WWVector overlapVector2 = new WWVector();
							object2.getOverlap(object, position2, rotation2, object2.getRotationPoint(), position, rotation, worldTime, tempPoint, tempPoint2, overlapPoint2, overlapVector2);
							
							// Keep the deepest overlap
							if (overlapVector2.isLongerThan(overlapVector)) {
								overlapVector2.copyInto(overlapVector);
								overlapPoint2.copyInto(overlapPoint);
								overlapVector.scale(-1.0f); // Reverse the vector since it's from object2's perspective
							}

							if (!overlapVector.isZero()) {

								ObjectCollision collision = new ObjectCollision(object, object2, overlapVector);
								newCollidedObjects.add(collision);

								// Determine forces based on elasticity
								WWVector unitOverlapVector;
								if (overlapVector.length() > 0) {
									unitOverlapVector = new WWVector(overlapVector);
								} else {
									unitOverlapVector = overlapVector.clone();
								}
								unitOverlapVector.normalize();

								// Adjust positions if solid vs solid
								if (object.solid && object2.solid) {
									// move objects away from each other evenly
									WWVector adjustVector = overlapVector.clone().scale(0.5f);
									
									// Adjust object1
									if (!object.freedomMoveX || !object.freedomMoveY || !object.freedomMoveZ) {
										WWObject.antiRotate(position, rotation);
										WWObject.antiRotate(adjustVector, rotation);
										if (object.freedomMoveX) position.x -= adjustVector.x;
										if (object.freedomMoveY) position.y -= adjustVector.y;
										if (object.freedomMoveZ) position.z -= adjustVector.z;
										WWObject.rotate(position, rotation);
										WWObject.rotate(adjustVector, rotation);
									} else {
										position.subtract(adjustVector);
									}

									// If object2 isn't physical, we should just adjust object1 completely (don't adjust object2).
									// Since we only do half the adjustVector above, if object2 is static, we must adjust object1 by the full amount.
									if (!object2.physical) {
										if (!object.freedomMoveX || !object.freedomMoveY || !object.freedomMoveZ) {
											WWObject.antiRotate(position, rotation);
											WWObject.antiRotate(adjustVector, rotation);
											if (object.freedomMoveX) position.x -= adjustVector.x;
											if (object.freedomMoveY) position.y -= adjustVector.y;
											if (object.freedomMoveZ) position.z -= adjustVector.z;
											WWObject.rotate(position, rotation);
											WWObject.rotate(adjustVector, rotation);
										} else {
											position.subtract(adjustVector);
										}
									}
								}

								// --- APPLY PHYSICS BASED ON SOLID STATE ---
								// 1. Solid vs Solid
								if (object.solid && object2.solid) {
									
									if (FastMath.avg(object.elasticity, object2.elasticity) > 0.0) { 
										// bounce both objects off of each other
										if (object2.physical) {
											float objectMass = object.density * object.size.x * object.size.y * object.size.z;
											float object2Mass = object2.density * object2.size.x * object2.size.y * object2.size.z;
											
											forceVector.set(velocity).scale(objectMass);
											force2Vector.set(object2.getVelocity()).scale(object2Mass);
											WWVector totalForceVector = forceVector.add(force2Vector);
											
											mirrorVector.set(unitOverlapVector);
											mirrorVector.scale(-1.0f);
											WWVector mirrorForceVector = totalForceVector.reflect(mirrorVector);
											float elasticity = FastMath.avg(object.elasticity, object2.elasticity);
											
											// Apply to object1
											velocity.x = mirrorForceVector.x * elasticity;
											velocity.y = mirrorForceVector.y * elasticity;
											velocity.z = mirrorForceVector.z * elasticity;
											aMomentum.scale(elasticity);
											
										} else { // simple bounce-back from a non-physical object (e.g. a wall or a moving bat)
											float elasticity = Math.max(object.elasticity, object2.elasticity);
											
											// Start with the physical object's velocity relative to the non-physical object
											WWVector relativeVelocity = velocity.clone().subtract(object2.getVelocity());
											
											// Reflect that relative velocity off the surface
											mirrorVector.set(unitOverlapVector).scale(-1.0f);
											relativeVelocity.reflect(mirrorVector).scale(elasticity);
											
											// Add the non-physical object's velocity back to impart energy (the "bat" hitting the "ball")
											velocity.set(relativeVelocity).add(object2.getVelocity());
											aMomentum.scale(elasticity);
										}
									} else { 
										// slide on the object
										if (slideStyle == 0) { // used in rtr
											WWVector antiForceVector = unitOverlapVector.clone();
											antiForceVector.scale(0.99f); 
											antiForceVector.scale(-totalForce.length());
											totalForce.add(antiForceVector);
											velocity.scale(1.0f, 1.0f, 1.0f - Math.abs(unitOverlapVector.z));
											
										} else if (slideStyle == 1) { // used in bonkers
											WWVector unitVelocity = velocity.clone().normalize();
											WWVector combined = unitVelocity.clone().add(unitOverlapVector); // FIXED MUTATION BUG
											float repelMag = 1 - combined.length();
											WWVector repelForce = unitOverlapVector.clone().scale(velocity.length() / deltaTime * repelMag);
											totalForce.add(repelForce);
											
										} else if (slideStyle == 2) { // most recent (best?)
											// 1. Cancel velocity into the surface
											float velIntoSurface = velocity.dot(unitOverlapVector);
											if (velIntoSurface > 0) {
												WWVector repelForce = unitOverlapVector.clone().scale(-velIntoSurface / deltaTime);
												totalForce.add(repelForce);
											}
											
											// 2. Cancel force (gravity, thrust) into the surface (Normal Force)
											float forceIntoSurface = totalForce.dot(unitOverlapVector);
											if (forceIntoSurface > 0) {
												WWVector normalForce = unitOverlapVector.clone().scale(-forceIntoSurface);
												totalForce.add(normalForce);
											}
											
										}
									}

									// Adjust angular momentum due to the collision.  This logic assumes the center of mass of the object
									// is in the middle of the object.  It then tries to determine how much angular force is applied to the
									// object due to the amount of impact (which is relative to the size of the overlap vector) torquing the overlap
									// point around that center of mass.

									// 1. The lever arm (r): vector FROM the center of mass TO the overlap point.
									// (Note: We do NOT normalize this, because a longer lever arm should mathematically create more torque!)
									WWVector leverArm = overlapPoint.clone().subtract(originalPosition);

									// 2. The impact force (F): spring force proportional to object mass and overlap penetration.
									float objectMass = object.density * object.size.x * object.size.y * object.size.z;
									WWVector impactForce = overlapVector.clone().scale(-objectMass);

									// 3. Torque = r x F
									WWVector impactTorque = leverArm.cross(impactForce).scale(FastMath.TODEGREES);

									// 4. Apply the torque to the object
									impactTorque.scale(4.0f);  // a fudge factor.. need to determine why it is useful
									totalTorque.add(impactTorque);

									// TODO implement physics of tops for object2
								} 
								// 2. Object1 Solid vs Object2 Liquid/Gas
								else if (object.solid && !object2.solid) {
									// buoyancy on object1
									if (object.isFreedomMoveZ() && object2.getDensity() > 0.0) {
										float pressure = FastMath.max(position2.z + object2.size.z / 2.0f - position.z, 0.0f);
										float buoyancy = object2.getDensity() * pressure - object.getDensity();
										if (buoyancy > 0) {
											velocity.z += (buoyancy * deltaTime) * 30.0f;
										}
									}
								}
								// 3. Object1 Liquid/Gas vs Object2 Solid
								else if (!object.solid && object2.solid) {
									// In full N^2 loop, this is handled when object2 is the outer loop (if it's physical)
								}

								// Friction
								if (object.solid && object2.solid && object.getElasticity() + object2.getElasticity() > 0) {
									// friction for solids touching with elasticity only impacts angular velocity
								} else {
									float friction = FastMath.min(object.friction, object2.friction);
									if (friction > 0) {
										if (object.solid && object2.solid) {  
											// solid-against-solid friction
											
											// 1. Calculate the lever arm (radius) from the center of mass DOWN to the contact point
											WWVector r1 = overlapPoint.clone().subtract(originalPosition);

											// 2. Calculate the relative linear velocity of the surfaces in contact
											// The surface velocity is the linear velocity PLUS the velocity contributed by the spin: v + (w x r)
											// We must convert the engine's degrees/sec angular momentum back to radians/sec for the math
											WWVector spinVel = aMomentum.clone().scale(1.0f / FastMath.TODEGREES).cross(r1);
											WWVector surfaceVelocity = velocity.clone().add(spinVel);
											
											// We compare this against the other object's surface velocity
											WWVector r2 = overlapPoint.clone().subtract(position2);
											WWVector obj2SpinVel = object2.getAngularVelocity().clone().scale(1.0f / FastMath.TODEGREES).cross(r2);
											WWVector surfaceVelocity2 = object2.getVelocity().clone().add(obj2SpinVel);
											
											WWVector relativeSurfaceVelocity = surfaceVelocity.subtract(surfaceVelocity2);

											// 3. The friction force pushes back against the slipping surface!
											// If the block is spinning, but not moving (like a car tire burning out), 
											// relativeSurfaceVelocity is high. The friction force will push forward, 
											// which ACCELERATES the block linearly!
											WWVector frictionForce = relativeSurfaceVelocity.clone().scale(-2.5f * friction);
											totalForce.add(frictionForce);

											// 4. Calculate the resulting Rolling Torque (from the friction force pushing on the bottom of the block)
											// Torque is the cross product of the lever arm and the force (T = r x F)
											WWVector rollingTorque = r1.clone().cross(frictionForce).scale(FastMath.TODEGREES);
											float extentSq = object.extent * object.extent;
											if (extentSq > 0.0001f) {
												rollingTorque.scale(1.0f / (0.5f * extentSq));
											}
											totalTorque.add(rollingTorque);

											// 5. Apply a dampening effect to the existing angular momentum
											// This simulates twisting friction (like a top spinning in place slowing down)
											WWVector spinDampening = aMomentum.clone().scale(-1.0f * friction);
											totalTorque.add(spinDampening);

											// Simulate static friction: removing contribution to totalForce and forcing velocity to equal object2's velocity
											if (relativeSurfaceVelocity.length() < friction && totalForce.length() < friction) {
												totalForce.subtract(frictionForce);
												velocity.subtract(relativeSurfaceVelocity);
											}
											if (aMomentum.length() / FastMath.TODEGREES < 2.5f * friction && totalTorque.length() / FastMath.TODEGREES < 2.5f * friction) {
												totalTorque.subtract(rollingTorque);
												aMomentum.set(0, 0, 0);
											}
											// TODO static friction effect on aMomentum can be improved above to account for a moving object2

										} else if (object.solid && !object2.solid) {
											// solid-in-liquid/gas friction
											WWVector frictionVForce = object2.getVelocity().clone().subtract(velocity);
											frictionVForce.scale(10 * friction);
											totalForce.add(frictionVForce);
											WWVector frictionAForce = new WWVector();
											frictionAForce.subtract(aMomentum);
											frictionAForce.scale(10 * friction);
											totalTorque.add(frictionAForce);
										} else if (!object.solid && object2.solid && object2.physical) {
											// Handled in outer loop when object2 is processed
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
					WWObject.antiRotate(velocity, rotation);
					if (!object.freedomMoveX) {
						velocity.x = 0.0f;
					}
					if (!object.freedomMoveY) {
						velocity.y = 0.0f;
					}
					if (!object.freedomMoveZ) {
						velocity.z = 0.0f;
					}
					WWObject.rotate(velocity, rotation);
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
