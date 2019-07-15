package com.gallantrealm.webworld.model;

import java.util.HashMap;
import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.model.WWAnimation;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWVector;

public class AnimationBehavior extends WWAnimation {
	private static final long serialVersionUID = 1L;

	private String type;
	private float speed;
	private float range;
	private boolean started;
	private transient long lastTime;
	private transient float animationTime;
	transient float lastStepSine;
	transient boolean stepped;

	// SmoothValues provides a place to remember the last animation size so it can be smoothed when switching
	// animations.
	private final class SmoothValues {
		float rotationX;
		float rotationY;
		float rotationZ;
	}

	private transient HashMap<String, SmoothValues> smoothAnimation;

	public String getType() {
		return type;
	}

	public void start(String type, float speed, float range) {
		this.type = type;
		this.speed = speed;
		if (range < -1.0f) {
			this.range = -1.0f;
		} else if (range > 1.0f) {
			this.range = 1.0f;
		} else {
			this.range = range;
		}
		started = true;
	}

	public void stop() {
		started = false;
		animationTime = 0;
		lastTime = 0;
	}

	// Note: this will be called for both the parent and child objects
	@Override
	public void getAnimatedPosition(WWObject object, WWVector position, long time) {
		if (started) {
			if (lastTime == 0) {
				lastTime = time;
			}
			animationTime += (time - lastTime) / 1000.0f * speed;
			lastTime = time;
			if (type.equals("walking")) {
				if ("torso".equals(object.name)) {
					float stepSine = FastMath.sin(4.0f * FastMath.PI * animationTime);
					position.z += 0.025f * (stepSine + 1) / 2.0f * FastMath.abs(range);
					if (lastStepSine > 0 && stepSine < 0) {
						stepped = true;
					}
					lastStepSine = stepSine;
				}
			} else if (type.equals("running")) {
				if ("torso".equals(object.name)) {
					float stepSine = FastMath.sin(4.0f * FastMath.PI * animationTime);
					position.z += 0.1f * (stepSine + 1) / 2.0f;
					if (lastStepSine > 0 && stepSine < 0) {
						stepped = true;
					}
					lastStepSine = stepSine;
				}
			} else if (type.equals("swimming")) {
				if ("torso".equals(object.name)) {
					position.z += object.sizeY / 2.0f * FastMath.abs(range);
				}
			} else if (type.equals("treading")) {
				if ("torso".equals(object.name)) {
					position.y -= 0.1f * object.sizeY * FastMath.sin(2.0f * FastMath.PI * animationTime) * range;
				}
			} else if (type.equals("falling")) {
			}
		}
	}

	// Note: this will be called for both the parent and child objects
	@Override
	public void getAnimatedRotation(WWObject object, WWVector rotation, long time) {
		if (started) {
			if (type.equals("walking")) {
				if ("left arm".equals(object.name)) {
					rotation.x -= 0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range;
				}
				if ("right arm".equals(object.name)) {
					rotation.x += 0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range;
				}
				if ("left forearm".equals(object.name)) {
					rotation.x += Math.max(0.0, -0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range);
				}
				if ("right forearm".equals(object.name)) {
					rotation.x += Math.max(0.0, 0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range);
				}
				if ("left leg".equals(object.name)) {
					rotation.x -= 0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range;
				}
				if ("right leg".equals(object.name)) {
					rotation.x += 0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range;
				}
				if ("left calf".equals(object.name)) {
					rotation.x += Math.min(0.0f, -FastMath.TODEGREES * FastMath.cos(2.0f * FastMath.PI * animationTime) * range);
				}
				if ("right calf".equals(object.name)) {
					rotation.x += Math.min(0.0f, FastMath.TODEGREES * FastMath.cos(2.0f * FastMath.PI * animationTime) * range);
				}
			} else if (type.equals("running")) {
				if ("left arm".equals(object.name)) {
					rotation.x += -0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range;
					if (object.getChild("left forearm") != null) {
						rotation.x += -45;
					}
				}
				if ("right arm".equals(object.name)) {
					rotation.x += 0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range;
					if (object.getChild("right forearm") != null) {
						rotation.x += -45;
					}
				}
				if ("left forearm".equals(object.name)) {
					rotation.x += 90;
				}
				if ("right forearm".equals(object.name)) {
					rotation.x += 90;
				}
				if ("left leg".equals(object.name)) {
					rotation.x -= 0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range;
				}
				if ("right leg".equals(object.name)) {
					rotation.x += 0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range;
				}
				if ("left calf".equals(object.name)) {
					rotation.x += Math.max(-90, Math.min(0.0f, -3 * FastMath.TODEGREES * FastMath.cos(2.0f * FastMath.PI * animationTime) * range));
				}
				if ("right calf".equals(object.name)) {
					rotation.x += Math.max(-90, Math.min(0.0f, 3 * FastMath.TODEGREES * FastMath.cos(2.0f * FastMath.PI * animationTime) * range));
				}
			} else if (type.equals("swimming")) {
				if ("torso".equals(object.name)) {
					rotation.x += 75 * range;
				}
				if ("head".equals(object.name)) {
					if ("neck".equals(world.getObject(object.getParent()).name)) {
						rotation.x -= 45 * range;
					} else {
						// bend head more when there's no neck to see horizon when swimming
						rotation.x -= 60 * range;
					}
				}
				if ("neck".equals(object.name)) {
					rotation.x -= 15 * range;
				}
				if ("left arm".equals(object.name)) {
					rotation.x += FastMath.TODEGREES * (FastMath.PI * animationTime);
				}
				if ("right arm".equals(object.name)) {
					rotation.x += FastMath.TODEGREES * (FastMath.PI * animationTime) + 180;
				}
				if ("left leg".equals(object.name)) {
					rotation.x -= 0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range;
				}
				if ("right leg".equals(object.name)) {
					rotation.x += 0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range;
				}
				if ("left calf".equals(object.name)) {
					rotation.x += Math.min(0.0f, -object.sizeZ * FastMath.TODEGREES * FastMath.cos(2.0f * FastMath.PI * animationTime) * range);
				}
				if ("right calf".equals(object.name)) {
					rotation.x += Math.min(0.0f, object.sizeZ * FastMath.TODEGREES * FastMath.cos(2.0f * FastMath.PI * animationTime) * range);
				}
				if ("left foot".equals(object.name)) {
					rotation.x -= 90f;
				}
				if ("right foot".equals(object.name)) {
					rotation.x -= 90f;
				}
				if ("left hand".equals(object.name)) {
					rotation.z += 90f;
				}
				if ("right hand".equals(object.name)) {
					rotation.z += 90f;
				}
			} else if (type.equals("treading")) {
				if ("left arm".equals(object.name)) {
					rotation.y -= 75f;
					rotation.x -= 0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range;
				}
				if ("right arm".equals(object.name)) {
					rotation.y += 75f;
					rotation.x += 0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime + FastMath.PI) * range;
				}
				if ("left forearm".equals(object.name)) {
					rotation.x += Math.max(0.0f, object.sizeZ * FastMath.TODEGREES * FastMath.cos(2.0f * FastMath.PI * animationTime) * range);
				}
				if ("right forearm".equals(object.name)) {
					rotation.x += Math.max(0.0f, object.sizeZ * FastMath.TODEGREES * FastMath.cos(2.0f * FastMath.PI * animationTime) * range);
				}
				if ("left leg".equals(object.name)) {
					rotation.x -= 0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range;
				}
				if ("right leg".equals(object.name)) {
					rotation.x += 0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range;
				}
				if ("left foot".equals(object.name)) {
					rotation.x -= 90f;
				}
				if ("right foot".equals(object.name)) {
					rotation.x -= 90f;
				}
			} else if (type.equals("falling")) {
				if ("torso".equals(object.name)) {
					rotation.x -= 30f * FastMath.abs(range);
				}
				if ("left arm".equals(object.name)) {
					rotation.y -= 75f * FastMath.abs(range);
				}
				if ("right arm".equals(object.name)) {
					rotation.y += 75f * FastMath.abs(range);
				}
				if ("left leg".equals(object.name)) {
					rotation.y += 15f * FastMath.abs(range);
				}
				if ("right leg".equals(object.name)) {
					rotation.y -= 15f * FastMath.abs(range);
				}
			}
		}

		// Disabling smooth animation. It worked nicely at high refresh rates but once
		// the rate was too slow to cover a swim stroke it begins to mess up
		/*
		if (object.name != null) {
			if (smoothAnimation == null) {
				smoothAnimation = new HashMap<String, SmoothValues>();
			}
			SmoothValues smoothValues = smoothAnimation.get(object.name);
			if (smoothValues == null) {
				smoothValues = new SmoothValues();
				smoothAnimation.put(object.name, smoothValues);
				smoothValues.rotationX = rotation.x;
				smoothValues.rotationY = rotation.y;
				smoothValues.rotationZ = rotation.z;
			} else {
				rotation.x = (rotation.x + 360) % 360;
				if (FastMath.abs(smoothValues.rotationX - rotation.x) > 180) {
					if (rotation.x > smoothValues.rotationX) {
						smoothValues.rotationX += 360;
					} else {
						smoothValues.rotationX -= 360;
					}
				}
				rotation.y = (rotation.y + 360) % 360;
				if (FastMath.abs(smoothValues.rotationY - rotation.y) > 180) {
					if (rotation.y > smoothValues.rotationY) {
						smoothValues.rotationY += 360;
					} else {
						smoothValues.rotationY -= 360;
					}
				}
				rotation.z = (rotation.z + 360) % 360;
				if (FastMath.abs(smoothValues.rotationZ - rotation.z) > 180) {
					if (rotation.z > smoothValues.rotationZ) {
						smoothValues.rotationZ += 360;
					} else {
						smoothValues.rotationZ -= 360;
					}
				}
				smoothValues.rotationX = (0.1f * rotation.x + 0.9f * smoothValues.rotationX);
				smoothValues.rotationY = (0.1f * rotation.y + 0.9f * smoothValues.rotationY);
				smoothValues.rotationZ = (0.1f * rotation.z + 0.9f * smoothValues.rotationZ);
				rotation.x = smoothValues.rotationX;
				rotation.y = smoothValues.rotationY;
				rotation.z = smoothValues.rotationZ;
			}
		}
		 */
	}

}
