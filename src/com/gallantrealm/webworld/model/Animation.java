package com.gallantrealm.webworld.model;

import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWVector;

public class Animation {
	private static final long serialVersionUID = 1L;

	private String type;
	private float speed;
	private float range;
	private transient boolean started;
	private transient long startTime;

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
		startTime = System.currentTimeMillis();
	}

	public void stop() {
		started = false;
	}

	public void animatePosition(WWObject object, WWVector position, long time) {
		if (started) {
			float animationTime = (int)(time - startTime) * speed;
			if (type.equals("walk")) {

			} else {

			}
		}
	}

	public void animateRotation(WWObject object, WWVector rotation, long time) {
		if (started) {
			float animationTime = (int)(time - startTime) * speed;
			if (type.equals("walk")) {

			} else {

			}
		}
	}

}
