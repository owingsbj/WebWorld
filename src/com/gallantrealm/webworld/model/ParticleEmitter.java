package com.gallantrealm.webworld.model;

import com.gallantrealm.myworld.model.WWParticleAnimation;
import com.gallantrealm.myworld.model.WWParticleEmitter;

public class ParticleEmitter extends WWParticleEmitter {
	private static final long serialVersionUID = 1L;
	
	public String animationName;
	
	class ParticleAnimation extends WWParticleAnimation {
		
		@Override
		public int getParticleRate() {
			// TODO Auto-generated method stub
			return 0;
		}
		@Override
		public void startParticle(WWParticleEmitter emitter, Particle particle, long worldTime) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void updateParticle(WWParticleEmitter emitter, Particle particle, long worldTime) {
			// TODO Auto-generated method stub
			
		}
	}

	public ParticleEmitter() {
	}
	
	public void setAnimation(String animation) {
		animationName = animation;
	}
	
	public String getAnimation() {
		return animationName;
	}

}
