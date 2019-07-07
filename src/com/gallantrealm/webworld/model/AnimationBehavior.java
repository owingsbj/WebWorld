package com.gallantrealm.webworld.model;

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
					position.y += 0.2f * object.sizeZ * FastMath.sin(animationTime / 2.0f / FastMath.PI)  * range;
				}
				if ("right arm".equals(object.name)) {
					position.y -= 0.2f * object.sizeZ * FastMath.sin(animationTime / 2.0f / FastMath.PI) * range;
				}
				if ("left leg".equals(object.name)) {
					position.y += 0.2f * object.sizeZ * FastMath.sin(animationTime / 2.0f / FastMath.PI)  * range;
				}
				if ("right leg".equals(object.name)) {
					position.y -= 0.2f * object.sizeZ * FastMath.sin(animationTime / 2.0f / FastMath.PI) * range;
				}
			} else if (type.equals("swimming")) {
				if ("left arm".equals(object.name)) {
					position.y += 0.5f * object.sizeZ * FastMath.sin(animationTime / 2.0f / FastMath.PI) ;
					position.z -= 0.5f * object.sizeZ * FastMath.cos(animationTime / 2.0f / FastMath.PI) ;
				}
				if ("right arm".equals(object.name)) {
					position.y -= 0.5f * object.sizeZ * FastMath.sin(animationTime / 2.0f / FastMath.PI);
					position.z += 0.5f * object.sizeZ * FastMath.cos(animationTime / 2.0f / FastMath.PI);
				}
				if ("left leg".equals(object.name)) {
					position.y += 0.2f * object.sizeZ * FastMath.sin(animationTime / 2.0f / FastMath.PI)  * range;
				}
				if ("right leg".equals(object.name)) {
					position.y -= 0.2f * object.sizeZ * FastMath.sin(animationTime / 2.0f / FastMath.PI) * range;
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
					rotation.x -= 0.5f * object.sizeZ * FastMath.TODEGREES * FastMath.sin(animationTime  / 2.0f / FastMath.PI) * range;
				}
				if ("right arm".equals(object.name)) {
					rotation.x += 0.5f * object.sizeZ * FastMath.TODEGREES * FastMath.sin(animationTime  / 2.0f / FastMath.PI) * range;
				}
				if ("left leg".equals(object.name)) {
					rotation.x -= 0.5f * object.sizeZ * FastMath.TODEGREES * FastMath.sin(animationTime  / 2.0f / FastMath.PI) * range;
				}
				if ("right leg".equals(object.name)) {
					rotation.x += 0.5f * object.sizeZ * FastMath.TODEGREES * FastMath.sin(animationTime  / 2.0f / FastMath.PI) * range;
				}
			} else if (type.equals("swimming")) {
				if ("torso".equals(object.name)) {
					rotation.x += 60 * range;
				}
				if ("head".equals(object.name)) {
					rotation.x -= 45 * range;
				}
				if ("left arm".equals(object.name)) {
					rotation.x += FastMath.TODEGREES * (animationTime  / 2.0f / FastMath.PI);
				}
				if ("right arm".equals(object.name)) {
					rotation.x += FastMath.TODEGREES * (animationTime  / 2.0f / FastMath.PI);
				}
				if ("left leg".equals(object.name)) {
					rotation.x -= 0.5f * object.sizeZ * FastMath.TODEGREES * FastMath.sin(animationTime  / 2.0f / FastMath.PI) * range;
				}
				if ("right leg".equals(object.name)) {
					rotation.x += 0.5f * object.sizeZ * FastMath.TODEGREES * FastMath.sin(animationTime  / 2.0f / FastMath.PI) * range;
				}

			} else if (type.equals("falling")) {
				
			}
		}
	}

}
