package com.gallantrealm.webworld.model;

import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.model.WWMatrix;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWQuaternion;
import com.gallantrealm.myworld.model.WWVector;

/**
 * Provides basic human-like bipedal behavior.  Options can be set to make it more/less
 * human:
 * - breathing -- turns on an adidtional behavior that has the avatar torso expand to look as if the avatar was breathing.
 * - atEase -- turns on an additional behavior that has the avatar lean from side to side when standing after a while.
 */
public final class HumanMotionBehavior extends AnimationBehavior {
	private static final long serialVersionUID = 1L;

	private boolean breathing = true;
	private boolean atEase = true;

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
				object.getVelocity().z += 0.15f * verticle;
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
		float velocityForward = object.getVelocity().clone().antirotate(object.getRotation()).y;
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

	public boolean isBreathing() {
		return breathing;
	}

	public void setBreathing(boolean breathing) {
		this.breathing = breathing;
	}

	public boolean isAtEase() {
		return atEase;
	}

	public void setAtEase(boolean atEase) {
		this.atEase = atEase;
	}

	@Override
	public void postAnimateModelMatrix(WWObject object, WWMatrix modelMatrix, long time) {
		super.postAnimateModelMatrix(object, modelMatrix, time);
		if (breathing && "torso".equals(object.name)) {
			// expand torso in y dimension every 4 seconds to simulate breathing
			float expansion = 0.04f  * (0.5f + FastMath.sin(2.0f * FastMath.PI * time / 4000));
			modelMatrix.scale(
					1.0f,
					1.0f + expansion,
					1.0f
			);
			modelMatrix.move(
					0.0f,
					-object.size.y * expansion / 2.5f,
					0.0f);
		}

	}

}
