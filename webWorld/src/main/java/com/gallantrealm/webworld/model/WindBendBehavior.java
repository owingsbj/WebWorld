package com.gallantrealm.webworld.model;

import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.model.WWMatrix;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWVector;

/**
 * An animation that causes tilting of an object to mimic bending in the wind.
 */
public class WindBendBehavior extends Behavior {

	private float amplitude = 0.25f;
	private float randomness = 0.25f;

	static WWVector windVelocity = new WWVector();
	static WWVector smoothedWindVelocity = new WWVector();
	static boolean firstWind = true;

	public WindBendBehavior() {
		//if (firstWind) {
			setTimer(100);   // only need one running timer event
			firstWind = false;
		//}
	}

	public WindBendBehavior(float amplitude, float randomness) {
		setTimer(100);
		this.amplitude = amplitude;
		this.randomness = randomness;
	}

	@Override
	public boolean timerEvent(WWObject object) {
		windVelocity.x = (float)Math.max(-1.0, Math.min(1.0, windVelocity.x + (2.0 * Math.random() - 1.0) * randomness));
		windVelocity.y = (float)Math.max(-1.0, Math.min(1.0, windVelocity.y + (2.0 * Math.random() - 1.0) * randomness));
		smoothedWindVelocity.x = (9 * smoothedWindVelocity.x + windVelocity.x) / 10.0f;
		smoothedWindVelocity.y = (9 * smoothedWindVelocity.y + windVelocity.y) / 10.0f;
		setTimer(100);
		return true;
	}

	@Override
	public void preAnimateModelMatrix(WWObject object, WWMatrix modelMatrix, long time) {
		modelMatrix.move(0, 0, -object.size.z/2);
		float x = amplitude * FastMath.sin(smoothedWindVelocity.x + object.getPosition().x / 10.0f);
		float y = amplitude * FastMath.sin(smoothedWindVelocity.y + object.getPosition().y / 10.0f);
		modelMatrix.shear(x, y, 0);
		modelMatrix.scale(1, 1, 1.0f / (float) (Math.sqrt(1.0 + x * x + y * y)));
		modelMatrix.move(0, 0, object.size.z/2);
	}

	public float getAmplitude() {
		return amplitude;
	}

	/**
	 * Change the amplitude of the bending.  A value of 1 would bend by 45 degrees.  By default it is 0.25,
	 */
	public void setAmplitude(float amplitude) {
		this.amplitude = amplitude;
	}

	public float getRandomness() {
		return randomness;
	}

	/**
	 * Changes how drastically the wind changes speed.  Default is 0.25.
	 */
	public void setRandomness(float randomness) {
		this.randomness = randomness;
	}

}
