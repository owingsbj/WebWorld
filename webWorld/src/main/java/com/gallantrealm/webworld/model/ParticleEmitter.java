package com.gallantrealm.webworld.model;

import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.model.WWParticleAnimation;
import com.gallantrealm.myworld.model.WWParticleEmitter;
import com.gallantrealm.myworld.model.WWVector;

public class ParticleEmitter extends WWParticleEmitter {
	private static final long serialVersionUID = 1L;

	private float particleRate = 100.0f;
	private float particleRateRandom = 0.0f;
	private float particleDrag = 0.0f;
	private float particleGravity = 0.0f;
	private float particleGrowthRate = 0.0f;
	private String particleImage = "";
	private float particleTransparency = 0.0f;
	private float particleFadeRate = 0.0f;
	private WWVector particlePositionRandom = new WWVector(0, 0, 0);
	private WWVector particleVelocity = new WWVector(0, 0, 0);
	private WWVector particleVelocityRandom = new WWVector(0, 0, 0);

	class ParticleAnimation extends WWParticleAnimation {
		private static final long serialVersionUID = 1L;

		@Override
		public float getParticleRate() {
			return (particleRate + particleRate * FastMath.random(-particleRateRandom, particleRateRandom))*15.0f/1000.0f;
		}

		@Override
		public void startParticle(WWParticleEmitter emitter, Particle particle, long worldTime) {
			emitter.getAbsolutePosition(particle.position, worldTime);
			particle.position.add( //
					particlePositionRandom.x * FastMath.random(-0.5f, 0.5f), //
					particlePositionRandom.y * FastMath.random(-0.5f, 0.5f), //
					particlePositionRandom.z * FastMath.random(-0.5f, 0.5f));
			particle.size = particleSize;
			particle.alpha = 1.0f - particleTransparency;
			particleVelocity.copyInto(particle.velocity);
			particle.velocity.add( //
					particleVelocityRandom.x * FastMath.random(-0.5f, 0.5f), //
					particleVelocityRandom.y * FastMath.random(-0.5f, 0.5f), //
					particleVelocityRandom.z * FastMath.random(-0.5f, 0.5f));
		}

		@Override
		public void updateParticle(WWParticleEmitter emitter, Particle particle, long worldTime) {
			particle.position.add( //
					particle.velocity.x * 0.01f,
					particle.velocity.y * 0.01f,
					particle.velocity.z * 0.01f);
			particle.size *= (1.0f + particleGrowthRate);
			particle.alpha /= (1.0f + particleFadeRate);
			particle.velocity.scale(1.0f - particleDrag* 15.0f / 1000.0f);
			particle.velocity.z -= particleGravity * world.getGravity() * 15.0f / 1000.0f;
		}
	}

	public ParticleEmitter() {
		animation = new ParticleAnimation();
	}

	public void startAnimation() {
		super.startAnimation(world.getWorldTime());
	}

	public float getParticleRate() {
		return particleRate;
	}

	public void setParticleRate(float particleRate) {
		this.particleRate = particleRate;
	}

	public WWVector getParticlePositionRandom() {
		return particlePositionRandom;
	}

	public void setParticlePositionRandom(WWVector particlePositionRandom) {
		this.particlePositionRandom = particlePositionRandom;
	}

	public WWVector getParticleVelocity() {
		return particleVelocity;
	}

	public void setParticleVelocity(WWVector particleVelocity) {
		this.particleVelocity = particleVelocity;
	}

	public WWVector getParticleVelocityRandom() {
		return particleVelocityRandom;
	}

	public void setParticleVelocityRandom(WWVector particleVelocityRandom) {
		this.particleVelocityRandom = particleVelocityRandom;
	}

	public float getParticleDrag() {
		return particleDrag;
	}

	public void setParticleDrag(float particleDrag) {
		this.particleDrag = particleDrag;
	}

	public float getParticleGravity() {
		return particleGravity;
	}

	public void setParticleGravity(float particleGravity) {
		this.particleGravity = particleGravity;
	}

	public float getParticleRateRandom() {
		return particleRateRandom;
	}

	public void setParticleRateRandom(float particleRateRandom) {
		this.particleRateRandom = particleRateRandom;
	}

	public float getParticleGrowthRate() {
		return particleGrowthRate;
	}

	public void setParticleGrowthRate(float particleGrowthRate) {
		this.particleGrowthRate = particleGrowthRate;
	}

	public String getParticleImage() {
		return particleImage;
	}

	public void setParticleImage(String particleImage) {
		System.out.println(particleImage);
		this.particleImage = particleImage;
		//temporary
		setTexture(Texture.worldPrefixUrl(particleImage));
	}

	public float getParticleTransparency() {
		return particleTransparency;
	}

	public void setParticleTransparency(float particleTransparency) {
		this.particleTransparency = particleTransparency;
		// temporary
		setTransparency(particleTransparency);
	}

	public float getParticleFadeRate() {
		return particleFadeRate;
	}

	public void setParticleFadeRate(float particleFadeRate) {
		this.particleFadeRate = particleFadeRate;
	}

}
