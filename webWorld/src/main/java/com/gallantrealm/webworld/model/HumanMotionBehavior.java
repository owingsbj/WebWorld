package com.gallantrealm.webworld.model;

import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWQuaternion;
import com.gallantrealm.myworld.model.WWVector;

public final class HumanMotionBehavior extends AnimationBehavior {
	private static final long serialVersionUID = 1L;

	private long lastSlidOnSolid = -1000;
	private long lastSlidThruLiquid = -1000;
	private WWObject lastSlidOnObject;

	public HumanMotionBehavior() {
		setTimer(100);
	}

	@Override
	public void getAnimatedPosition(WWObject object, WWVector position, long time) {
		super.getAnimatedPosition(object, position, time);
	}

	@Override
	public void getAnimatedRotation(WWObject object, WWQuaternion rotation, long time) {
		super.getAnimatedRotation(object, rotation, time);
	}

	@Override
	public boolean slideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
		if (nearObject.solid) {
			lastSlidOnSolid = world.getWorldTime();
			lastSlidOnObject = nearObject;
			
			// when the avatar comes to a steep area, allow it to climb
			float verticle = proximity.clone().normalize().cross(new WWVector(0, 0, 1)).length();
			if (verticle > 0.5) {
				object.velocityZ += 0.15f * verticle;
			}
			
		} else if (nearObject.density > object.density) {
			lastSlidThruLiquid = world.getWorldTime();
			lastSlidOnObject = nearObject;
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
		float velocityForward = object.getVelocity().antirotate(object.getRotation()).y;
		if (lastSlidOnSolid > world.getWorldTime() - 500) {
			if (FastMath.abs(velocityForward) < 0.25) {
				stop();
			} else if (FastMath.abs(velocityForward) <= 2.75) {
				start("walking", velocityForward, velocityForward / 2);
			} else {
				start("running", velocityForward, 1);
			}
			if (stepped && lastSlidOnObject.getImpactSound() != null) {
				object.playSound(lastSlidOnObject.getImpactSound(), 0.05f * FastMath.abs(velocityForward));
				stepped = false;
			}
		} else if (lastSlidThruLiquid > world.getWorldTime() - 500) {
			if (Math.abs(velocityForward) < 0.25) {
				start("treading", 1, 1);
			} else {
				start("swimming", velocityForward, velocityForward / 2);
				if (stroked && lastSlidOnObject.getImpactSound() != null) {
					object.playSound(lastSlidOnObject.getImpactSound(), 0.05f * FastMath.abs(velocityForward));
					stroked = false;
				}
			}
		} else {
			float velocityDown = FastMath.abs(object.getVelocity().clone().antirotate(object.getRotation()).z);
			start("falling", 1, velocityDown / 10);
		}
		WWObject head = object.getDescendant("head");
		if (head != null) {
			float turningForce = object.getTorqueVelocity().clone().antirotate(object.getRotation()).z;
			WWQuaternion headRotation = head.getRotation();
			if (velocityForward > 0.1) { // positive means moving backwards
				head.setRotation(headRotation.getPitch(), headRotation.getRoll(), -FastMath.range(turningForce / 2, -45, 45));
			} else {
				head.setRotation(headRotation.getPitch(), headRotation.getRoll(), FastMath.range(turningForce / 2, -45, 45));
			}
		}
		setTimer(50); // to repeat
		return false;
	}

}
