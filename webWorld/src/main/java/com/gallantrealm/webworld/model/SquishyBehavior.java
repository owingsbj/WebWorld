package com.gallantrealm.webworld.model;

import com.gallantrealm.myworld.model.WWMatrix;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWVector;

/**
 * An animation that causes an object to temporarily deform when its velocity changes quickly.
 */
public class SquishyBehavior extends Behavior {

	private float amplitude = 0.25f;
	private float stiffness = 0.25f;
	private float springiness = 0.25f;

	WWVector lastVelocity = new WWVector();
	WWVector squishAmount = new WWVector();
	WWVector springMomentum = new WWVector();

	public SquishyBehavior() {
		setTimer(100);
	}

	public SquishyBehavior(float amplitude, float stiffness, float springiness) {
		setTimer(100);
		this.amplitude = amplitude;
		this.stiffness = stiffness;
		this.springiness = springiness;
	}

	@Override
	public boolean timerEvent(WWObject object) {
		WWVector velocityDelta = object.getVelocity().clone().subtract(lastVelocity);
		squishAmount.add(velocityDelta);
		WWVector rebound = squishAmount.clone().scale(10 * stiffness);
		springMomentum.subtract(rebound);
		springMomentum.scale(springiness);
		squishAmount.add(springMomentum);
		object.getVelocity().copyInto(lastVelocity);
		setTimer(50);
		return true;
	}

	@Override
	public void preAnimateModelMatrix(WWObject object, WWMatrix modelMatrix, long time) {
		modelMatrix.antirotate(object.getRotation());
		modelMatrix.scale(
				1.0f - 0.25f * amplitude * squishAmount.x,
				1.0f - 0.25f * amplitude * squishAmount.y,
				1.0f - 0.25f * amplitude * squishAmount.z
		);
		modelMatrix.move(0, 0, - 0.125f * amplitude * squishAmount.z * object.size.z);
		modelMatrix.rotate(object.getRotation());
	}

	public float getAmplitude() {
		return amplitude;
	}

	/**
	 * Change the amplitude of the squish.  By default it is 0.25, which changes the object size
	 * 1/4 for every unit change in velocity.
	 */
	public void setAmplitude(float amplitude) {
		this.amplitude = amplitude;
	}

	public float getStiffness() {
		return stiffness;
	}

	/**
	 * Changes the stiffness of the squish.  This is related to how fast the object deforms.  The
	 * default is 0.25, which takes about 1/2 second to deform/undeform.  Larger numbers
	 * will make the deformation effect occur more quickly.  Values should be greater than 0.
	 * Note that the springiness value should be increased if the stiffness is made very small
	 * or otherwise the effect will not be seen.
	 */
	public void setStiffness(float stiffness) {
		this.stiffness = stiffness;
	}

	public float getSpringiness() {
		return springiness;
	}

	/**
	 * Changes the springines/dampening of the squish.  The default is 0.25.  Values smaller than this
	 * will likely cause the squish effect to be dampened to where it can't be seen.  Values
	 * greater than 1 will cause an uncontrolled oscillation.  So, the recommended range
	 * is 0.25 to 0.9.
	 */
	public void setSpringiness(float springiness) {
		this.springiness = springiness;
	}

}
