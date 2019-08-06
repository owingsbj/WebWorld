package com.gallantrealm.webworld.model;

import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.model.WWParticleAnimation;
import com.gallantrealm.myworld.model.WWParticleEmitter;

public class ParticleEmitter extends WWParticleEmitter {
	private static final long serialVersionUID = 1L;
	
	public String animationName;
	public int particleRate = 3;
	
	class ParticleAnimation extends WWParticleAnimation {
		private static final long serialVersionUID = 1L;

		@Override
		public int getParticleRate() {
			return particleRate;
		}
		
		@Override
		public void startParticle(WWParticleEmitter emitter, Particle particle, long worldTime) {
			if ("exhaust".equals(animationName)) {
				particle.position =  emitter.getAbsolutePosition(worldTime -(long)FastMath.random(0, 5));
				particle.position.add( //
						emitter.sizeX * FastMath.random(-0.05f, 0.05f), //
						emitter.sizeY * FastMath.random(-0.05f, 0.05f), //
						emitter.sizeZ * FastMath.random(-0.05f, 0.05f));
				particle.size = 0.5f;
				particle.alpha = 0.5f;
			}
		}
		
		@Override
		public void updateParticle(WWParticleEmitter emitter, Particle particle, long worldTime) {
			if ("exhaust".equals(animationName)) {
				particle.position.add( //
						emitter.sizeX * FastMath.random(-0.01f, 0.01f), //
						emitter.sizeY * FastMath.random(-0.01f, 0.01f), //
						emitter.sizeZ * FastMath.random(-0.01f, 0.01f));
				particle.size += 0.25f;
				particle.alpha -= 0.1f;
			}
		}
	}

	public ParticleEmitter() {
		animation = new ParticleAnimation();
	}
	
	public void setType(String type) {
		animationName = type;
	}
	
	public String getType() {
		return animationName;
	}
	
	public void startAnimation() {
		super.startAnimation(world.getWorldTime());
	}
	
	public int getParticleRate() {
		return particleRate;
	}
	
	public void setParticleRate(int particleRate) {
		this.particleRate = particleRate;
	}
	
}
