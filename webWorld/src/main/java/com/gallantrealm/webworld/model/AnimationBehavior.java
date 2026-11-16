package com.gallantrealm.webworld.model;

import java.util.HashMap;
import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.model.WWAnimation;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWQuaternion;
import com.gallantrealm.myworld.model.WWVector;

public class AnimationBehavior extends Behavior {
	private static final long serialVersionUID = 1L;

	private String type;
	private float speed;
	private float range;
	private boolean started;
	private transient long lastTime;
	private transient float animationTime;
	transient float lastStepSine;
	transient boolean stepped;
	transient float lastStrokeSine;
	transient boolean stroked;

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
				if ("torso".equals(object.getName())) {
					float stepSine = FastMath.sin(4.0f * FastMath.PI * animationTime);
					position.z += 0.025f * (stepSine + 1) / 2.0f * FastMath.abs(range);
					if (lastStepSine > 0 && stepSine < 0) {
						stepped = true;
					}
					lastStepSine = stepSine;
				}
			} else if (type.equals("running")) {
				if ("torso".equals(object.getName())) {
					float stepSine = FastMath.sin(4.0f * FastMath.PI * animationTime);
					position.z += 0.1f * (stepSine + 1) / 2.0f;
					if (lastStepSine > 0 && stepSine < 0) {
						stepped = true;
					}
					lastStepSine = stepSine;
				}
			} else if (type.equals("swimming")) {
				if ("torso".equals(object.getName())) {
					float strokeSine = FastMath.sin(2.0f * FastMath.PI * animationTime + FastMath.PI);
					if (lastStrokeSine > 0 && strokeSine < 0) {
						stroked = true;
					}
					lastStrokeSine = strokeSine;
					position.z += object.size.y / 2.0f * FastMath.abs(range);
				}
			} else if (type.equals("treading")) {
				if ("torso".equals(object.getName())) {
					position.y -= 0.1f * object.size.y * FastMath.sin(2.0f * FastMath.PI * animationTime) * range;
				}
			} else if (type.equals("falling")) {
			}
		}
	}

	// Note: this will be called for both the parent and child objects
	@Override
	public void getAnimatedRotation(WWObject object, WWQuaternion rotation, long time) {
		if (started) {
			if (type.equals("walking")) {
				if ("left arm".equals(object.getName())) {
					rotation.pitch(-0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range);
				}
				if ("right arm".equals(object.getName())) {
					rotation.pitch(0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range);
				}
				if ("left forearm".equals(object.getName())) {
					rotation.pitch(Math.max(0.0f, -0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range));
				}
				if ("right forearm".equals(object.getName())) {
					rotation.pitch(Math.max(0.0f, 0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range));
				}
				if ("left leg".equals(object.getName())) {
					rotation.pitch(-0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range);
				}
				if ("right leg".equals(object.getName())) {
					rotation.pitch(0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range);
				}
				if ("left calf".equals(object.getName())) {
					rotation.pitch(Math.min(0.0f, -FastMath.TODEGREES * FastMath.cos(2.0f * FastMath.PI * animationTime) * range));
				}
				if ("right calf".equals(object.getName())) {
					rotation.pitch(Math.min(0.0f, FastMath.TODEGREES * FastMath.cos(2.0f * FastMath.PI * animationTime) * range));
				}
			} else if (type.equals("running")) {
				if ("left arm".equals(object.getName())) {
					rotation.pitch(-0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range);
					if (object.getChild("left forearm") != null) {
						rotation.pitch(-45);
					}
				}
				if ("right arm".equals(object.getName())) {
					rotation.pitch(0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range);
					if (object.getChild("right forearm") != null) {
						rotation.pitch(-45);
					}
				}
				if ("left forearm".equals(object.getName())) {
					rotation.pitch(90);
				}
				if ("right forearm".equals(object.getName())) {
					rotation.pitch(90);
				}
				if ("left leg".equals(object.getName())) {
					rotation.pitch(-0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range);
				}
				if ("right leg".equals(object.getName())) {
					rotation.pitch(0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range);
				}
				if ("left calf".equals(object.getName())) {
					rotation.pitch(Math.max(-90, Math.min(0.0f, -3 * FastMath.TODEGREES * FastMath.cos(2.0f * FastMath.PI * animationTime) * range)));
				}
				if ("right calf".equals(object.getName())) {
					rotation.pitch(Math.max(-90, Math.min(0.0f, 3 * FastMath.TODEGREES * FastMath.cos(2.0f * FastMath.PI * animationTime) * range)));
				}
			} else if (type.equals("swimming")) {
				if ("torso".equals(object.getName())) {
					rotation.pitch(75 * range);
				}
				if ("head".equals(object.getName())) {
					if (object.getParent() != null && "neck".equals(object.getParent().getName())) {
						rotation.pitch(-45 * range);
					} else {
						// bend head more when there's no neck to see horizon when swimming
						rotation.pitch(-60 * range);
					}
				}
				if ("neck".equals(object.getName())) {
					rotation.pitch(-15 * range);
				}
				if ("left arm".equals(object.getName())) {
					rotation.pitch(FastMath.TODEGREES * (FastMath.PI * animationTime));
				}
				if ("right arm".equals(object.getName())) {
					rotation.pitch(FastMath.TODEGREES * (FastMath.PI * animationTime) + 180);
				}
				if ("left leg".equals(object.getName())) {
					rotation.pitch(-0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range);
				}
				if ("right leg".equals(object.getName())) {
					rotation.pitch(0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range);
				}
				if ("left calf".equals(object.getName())) {
					rotation.pitch(Math.min(0.0f, -object.size.z * FastMath.TODEGREES * FastMath.cos(2.0f * FastMath.PI * animationTime) * range));
				}
				if ("right calf".equals(object.getName())) {
					rotation.pitch(Math.min(0.0f, object.size.z * FastMath.TODEGREES * FastMath.cos(2.0f * FastMath.PI * animationTime) * range));
				}
				if ("left foot".equals(object.getName())) {
					rotation.pitch(-90);
				}
				if ("right foot".equals(object.getName())) {
					rotation.pitch(-90);
				}
				if ("left hand".equals(object.getName())) {
					rotation.yaw(90);
				}
				if ("right hand".equals(object.getName())) {
					rotation.yaw(90);
				}
			} else if (type.equals("treading")) {
				if ("left arm".equals(object.getName())) {
					rotation.roll(-75);
					rotation.pitch(-0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range);
				}
				if ("right arm".equals(object.getName())) {
					rotation.roll(75f);
					rotation.pitch(0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime + FastMath.PI) * range);
				}
				if ("left forearm".equals(object.getName())) {
					rotation.pitch(Math.max(0.0f, object.size.z * FastMath.TODEGREES * FastMath.cos(2.0f * FastMath.PI * animationTime) * range));
				}
				if ("right forearm".equals(object.getName())) {
					rotation.pitch(Math.max(0.0f, object.size.z * FastMath.TODEGREES * FastMath.cos(2.0f * FastMath.PI * animationTime) * range));
				}
				if ("left leg".equals(object.getName())) {
					rotation.pitch(-0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range);
				}
				if ("right leg".equals(object.getName())) {
					rotation.pitch(0.5f * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range);
				}
				if ("left foot".equals(object.getName())) {
					rotation.pitch(-90);
				}
				if ("right foot".equals(object.getName())) {
					rotation.pitch(-90);
				}
			} else if (type.equals("falling")) {
				if ("torso".equals(object.getName())) {
					rotation.pitch(-30 * FastMath.abs(range));
				}
				if ("left arm".equals(object.getName())) {
					rotation.roll(-75 * FastMath.abs(range));
				}
				if ("right arm".equals(object.getName())) {
					rotation.roll(75 * FastMath.abs(range));
				}
				if ("left leg".equals(object.getName())) {
					rotation.roll(15 * FastMath.abs(range));
				}
				if ("right leg".equals(object.getName())) {
					rotation.roll(-15 * FastMath.abs(range));
				}
			}
		}

		// Disabling smooth animation. It worked nicely at high refresh rates but once
		// the rate was too slow to cover a swim stroke it begins to mess up
		/*
		if (object.getName() != null) {
			if (smoothAnimation == null) {
				smoothAnimation = new HashMap<String, SmoothValues>();
			}
			SmoothValues smoothValues = smoothAnimation.get(object.getName());
			if (smoothValues == null) {
				smoothValues = new SmoothValues();
				smoothAnimation.put(object.getName(), smoothValues);
				smoothValues.rotationX = rotation.getRoll();
				smoothValues.rotationY = rotation.getPitch();
				smoothValues.rotationZ = rotation.getYaw();
			} else {
				rotation.getRoll() = (rotation.getRoll() + 360) % 360;
				if (FastMath.abs(smoothValues.rotationX - rotation.getRoll()) > 180) {
					if (rotation.getRoll() > smoothValues.rotationX) {
						smoothValues.rotationX += 360;
					} else {
						smoothValues.rotationX -= 360;
					}
				}
				rotation.getPitch() = (rotation.getPitch() + 360) % 360;
				if (FastMath.abs(smoothValues.rotationY - rotation.getPitch()) > 180) {
					if (rotation.getPitch() > smoothValues.rotationY) {
						smoothValues.rotationY += 360;
					} else {
						smoothValues.rotationY -= 360;
					}
				}
				rotation.getYaw() = (rotation.getYaw() + 360) % 360;
				if (FastMath.abs(smoothValues.rotationZ - rotation.getYaw()) > 180) {
					if (rotation.getYaw() > smoothValues.rotationZ) {
						smoothValues.rotationZ += 360;
					} else {
						smoothValues.rotationZ -= 360;
					}
				}
				smoothValues.rotationX = (0.1f * rotation.getRoll() + 0.9f * smoothValues.rotationX);
				smoothValues.rotationY = (0.1f * rotation.getPitch() + 0.9f * smoothValues.rotationY);
				smoothValues.rotationZ = (0.1f * rotation.getYaw() + 0.9f * smoothValues.rotationZ);
				rotation.getRoll() = smoothValues.rotationX;
				rotation.getPitch() = smoothValues.rotationY;
				rotation.getYaw() = smoothValues.rotationZ;
			}
		}
		 */
	}

}
