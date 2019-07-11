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

	// SmoothValues provides a place to remember the last animation size so it can be smoothed when switching
	// animations.
	private final class SmoothValues {
		float rotationX;
		float rotationY;
		float rotationZ;
		float positionX;
		float positionY;
		float positionZ;
	}
	private HashMap<String, SmoothValues> smoothAnimation = new HashMap<String, SmoothValues>();

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
				if ("left arm".equals(object.name)) {
//					position.y += 0.2f * object.sizeZ * FastMath.sin(2.0f * FastMath.PI * animationTime)  * range;
				}
				if ("right arm".equals(object.name)) {
//					position.y -= 0.2f * object.sizeZ * FastMath.sin(2.0f * FastMath.PI * animationTime) * range;
				}
				if ("left leg".equals(object.name)) {
//					position.y += 0.2f * object.sizeZ * FastMath.sin(2.0f * FastMath.PI * animationTime)  * range;
				}
				if ("right leg".equals(object.name)) {
//					position.y -= 0.2f * object.sizeZ * FastMath.sin(2.0f * FastMath.PI * animationTime) * range;
				}
			} else if (type.equals("swimming")) {
				if ("torso".equals(object.name)) {
					position.z += object.sizeY / 2.0f * FastMath.abs(range);
				}
				if ("left arm".equals(object.name)) {
//					float rotationRadius = (object.sizeZ - object.sizeY/ 2.0f) / 2.0f;
//					position.y -= rotationRadius * FastMath.sin(FastMath.PI * animationTime) ;
//					position.z += -rotationRadius * FastMath.cos(FastMath.PI * animationTime) + object.sizeY;
				}
				if ("right arm".equals(object.name)) {
//					float rotationRadius = (object.sizeZ - object.sizeY/ 2.0f) / 2.0f;
//					position.y -= rotationRadius * FastMath.sin(FastMath.PI * animationTime + FastMath.PI) ;
//					position.z += -rotationRadius * FastMath.cos(FastMath.PI * animationTime + FastMath.PI) + object.sizeY;
				}
				if ("left leg".equals(object.name)) {
//					position.y += 0.2f * object.sizeZ * FastMath.sin(2.0f * FastMath.PI * animationTime)  * range;
				}
				if ("right leg".equals(object.name)) {
//					position.y -= 0.2f * object.sizeZ * FastMath.sin(2.0f * FastMath.PI * animationTime) * range;
				}
			} else if (type.equals("treading")) {
				if ("torso".equals(object.name)) {
					position.y -= 0.1f * object.sizeY * FastMath.sin(2.0f * FastMath.PI * animationTime) * range;
				}
				if ("left arm".equals(object.name)) {
//					position.x -= object.sizeX;
//					position.y += 0.2f * object.sizeZ * FastMath.sin(2.0f * FastMath.PI * animationTime)  * range;
//					position.z += 0.965 * (object.sizeZ / 2.0f - object.sizeX);
				}
				if ("right arm".equals(object.name)) {
//					position.x += object.sizeX;
//					position.y += 0.2f * object.sizeZ * FastMath.sin(2.0f * FastMath.PI * animationTime) * range;
//					position.z += 0.965 * (object.sizeZ / 2.0f - object.sizeX);
				}
				if ("left leg".equals(object.name)) {
//					position.y += 0.2f * object.sizeZ * FastMath.sin(2.0f * FastMath.PI * animationTime)  * range;
				}
				if ("right leg".equals(object.name)) {
//					position.y -= 0.2f * object.sizeZ * FastMath.sin(2.0f * FastMath.PI * animationTime) * range;
				}
			} else if (type.equals("falling")) {
				if ("left arm".equals(object.name)) {
//					position.x -= (object.sizeZ - object.sizeX) / 2.0 * FastMath.sin(75f * FastMath.abs(range) * FastMath.TORADIAN);
//					position.z += (object.sizeZ / 2.0 - object.sizeX) * FastMath.sin(75f * FastMath.abs(range) * FastMath.TORADIAN);
				}
				if ("right arm".equals(object.name)) {
//					position.x += (object.sizeZ - object.sizeX) / 2.0 * FastMath.sin(75f * FastMath.abs(range) * FastMath.TORADIAN);
//					position.z += (object.sizeZ / 2.0 - object.sizeX) * FastMath.sin(75f * FastMath.abs(range) * FastMath.TORADIAN);
				}
				if ("left leg".equals(object.name)) {
//					position.x += object.sizeZ / 2.0f * FastMath.sin(15  * FastMath.abs(range) * FastMath.TORADIAN);
				}
				if ("right leg".equals(object.name)) {
//					position.x  -= object.sizeZ / 2.0f * FastMath.sin(15  * FastMath.abs(range) * FastMath.TORADIAN);
				}
			}
		}
		if (object.name != null) {
			SmoothValues smoothValues = smoothAnimation.get(object.name);
			if (smoothValues == null) {
				smoothValues = new SmoothValues();
				smoothAnimation.put(object.name, smoothValues);
			}
			smoothValues.positionX = 0.1f * position.x + 0.9f * smoothValues.positionX;
			smoothValues.positionY = 0.1f * position.y + 0.9f * smoothValues.positionY;
			smoothValues.positionZ = 0.1f * position.z + 0.9f * smoothValues.positionZ;
			position.x = smoothValues.positionX;
			position.y = smoothValues.positionY;
			position.z = smoothValues.positionZ;
		}
	}

	// Note: this will be called for both the parent and child objects
	@Override
	public void getAnimatedRotation(WWObject object, WWVector rotation, long time) {
		if (started) {
			if (type.equals("walking")) {
				if ("left arm".equals(object.name)) {
					rotation.x -= 0.5f * object.sizeZ * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range;
				}
				if ("right arm".equals(object.name)) {
					rotation.x += 0.5f * object.sizeZ * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range;
				}
				if ("left leg".equals(object.name)) {
					rotation.x -= 0.5f * object.sizeZ * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range;
				}
				if ("right leg".equals(object.name)) {
					rotation.x += 0.5f * object.sizeZ * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range;
				}
				if ("left calf".equals(object.name)) {
					rotation.x += Math.min(0.0f, -object.sizeZ * FastMath.TODEGREES * FastMath.cos(2.0f * FastMath.PI * animationTime) * range);
				}
				if ("right calf".equals(object.name)) {
					rotation.x += Math.min(0.0f, object.sizeZ * FastMath.TODEGREES * FastMath.cos(2.0f * FastMath.PI * animationTime) * range);
				}
			} else if (type.equals("swimming")) {
				if ("torso".equals(object.name)) {
					rotation.x += 75 * range;
				}
				if ("head".equals(object.name)) {
					rotation.x -= 45 * range;
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
					rotation.x -= 0.5f * object.sizeZ * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range;
				}
				if ("right leg".equals(object.name)) {
					rotation.x += 0.5f * object.sizeZ * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range;
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
					rotation.x -= 0.5f * object.sizeZ * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range;
				}
				if ("right arm".equals(object.name)) {
					rotation.y += 75f;
					rotation.x += 0.5f * object.sizeZ * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime + FastMath.PI) * range;
				}
				if ("left leg".equals(object.name)) {
					rotation.x -= 0.5f * object.sizeZ * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range;
				}
				if ("right leg".equals(object.name)) {
					rotation.x += 0.5f * object.sizeZ * FastMath.TODEGREES * FastMath.sin(2.0f * FastMath.PI * animationTime) * range;
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
		if (object.name != null) {
			SmoothValues smoothValues = smoothAnimation.get(object.name);
			if (smoothValues == null) {
				smoothValues = new SmoothValues();
				smoothAnimation.put(object.name, smoothValues);
			}
			smoothValues.rotationX = (0.1f * rotation.x + 0.9f * smoothValues.rotationX) % 360;
			smoothValues.rotationY = (0.1f * rotation.y + 0.9f * smoothValues.rotationY) % 360;
			smoothValues.rotationZ = (0.1f * rotation.z + 0.9f * smoothValues.rotationZ) % 360;
			rotation.x = smoothValues.rotationX;
			rotation.y = smoothValues.rotationY;
			rotation.z = smoothValues.rotationZ;
		}
	}

}
