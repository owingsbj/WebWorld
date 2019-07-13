package com.gallantrealm.webworld.model;

import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.model.WWAnimation;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWVector;

public final class HumanMotionBehavior extends WWAnimation {
	private static final long serialVersionUID = 1L;

	private long lastSlidOnSolid = -1000;
	private long lastSlidThruLiquid = -1000;

	public HumanMotionBehavior() {
		setTimer(100);
	}

	@Override
	public void getAnimatedPosition(WWObject object, WWVector position, long time) {
	}

	@Override
	public void getAnimatedRotation(WWObject object, WWVector rotation, long time) {
	}

	@Override
	public boolean slideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
		if (nearObject.solid) {
			lastSlidOnSolid = world.getWorldTime();
		} else if (nearObject.density > object.density) {
			lastSlidThruLiquid = world.getWorldTime();
		}
		return false;
	}

	@Override
	public boolean collideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
		slideEvent(object, nearObject, proximity);
		return false;
	}

	@Override
	public boolean timerEvent(WWObject object) {
		if (object instanceof WebWorldObject) {
			WebWorldObject webobject = (WebWorldObject) object;
			float velocityForward = object.getVelocity().antirotate(object.getRotation()).y;
			if (lastSlidOnSolid > world.getWorldTime() - 500) {
				if (FastMath.abs(velocityForward) < 0.25) {
					webobject.stopAnimation();
				} else if (FastMath.abs(velocityForward) <= 2.5) {
					webobject.animate("walking", velocityForward, velocityForward / 2);
				} else {
					webobject.animate("running", velocityForward, 1);
				}
			} else if (lastSlidThruLiquid > world.getWorldTime() - 500) {
				if (Math.abs(velocityForward) < 0.25) {
					webobject.animate("treading");
				} else {
					webobject.animate("swimming", velocityForward, velocityForward / 2);
				}
			} else {
				float velocityDown = FastMath.abs(object.getVelocity().clone().antirotate(object.getRotation()).z);
				webobject.animate("falling", 1, velocityDown / 10);
			}
			WWObject head = object.getChild("head");
			if (head != null) {
				float turningForce = object.getTorque().clone().antirotate(object.getRotation()).z;
				WWVector headRotation = head.getRotation();
				if (velocityForward <= 0) { // negative means ahead
					head.setRotation(headRotation.x, headRotation.y, turningForce / 2);
				} else {
					head.setRotation(headRotation.x, headRotation.y, -turningForce / 2);
				}
			}
			setTimer(100); // to repeat
		}
		return false;
	}
}
