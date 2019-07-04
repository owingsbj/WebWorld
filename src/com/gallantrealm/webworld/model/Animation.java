package com.gallantrealm.webworld.model;

import java.io.Serializable;
import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWVector;

public class Animation implements Serializable {
	private static final long serialVersionUID = 1L;

	private String type;
	private float speed;
	private float range;
	private transient boolean started;
	private transient long lastTime;
	private transient float animationTime;

	public Animation(String type) {
		this.type = type;
		this.speed = 1.0f;
		this.range = 1.0f;
	}

	public Animation(String type, float speed, float range) {
		this.type = type;
		this.speed = speed;
		this.range = range;
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public float getSpeed() {
		return speed;
	}
	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public float getRange() {
		return range;
	}
	public void setRange(float range) {
		this.range = range;
	}

	public void start() {
		started = true;
		animationTime = 0;
		lastTime = System.currentTimeMillis();
	}

	public void stop() {
		started = false;
	}

	// Note: this will be called for both the parent and child objects
	public void animatePosition(WWObject object, WWVector position, long time) {
		if (started) {
			animationTime += (time - lastTime) / 1000.0f * speed;
			lastTime = time;
			if (type.equals("walk")) {
				if ("leg1".equals(object.name)) {
					position.y += 0.2f * object.sizeZ * FastMath.sin(animationTime / 2.0f / FastMath.PI)  * range;
					//System.out.println("walk animation leg1.  animation time " +animationTime+" range "+range+" position.y "+position.y);
				}
				if ("leg2".equals(object.name)) {
					position.y -= 0.2f * object.sizeZ * FastMath.sin(animationTime / 2.0f / FastMath.PI) * range;
				}
			} else {

			}
		}
	}

	// Note: this will be called for both the parent and child objects
	public void animateRotation(WWObject object, WWVector rotation, long time) {
		if (started) {
			if (type.equals("walk")) {
				if ("leg1".equals(object.name)) {
					rotation.x -= 0.5f * object.sizeZ * FastMath.TODEGREES * FastMath.sin(animationTime  / 2.0f / FastMath.PI) * range;
				}
				if ("leg2".equals(object.name)) {
					rotation.x += 0.5f * object.sizeZ * FastMath.TODEGREES * FastMath.sin(animationTime  / 2.0f / FastMath.PI) * range;
				}
			} else {

			}
		}
	}

}
