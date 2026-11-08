package com.gallantrealm.myworld.model;

import org.junit.Assert;
import org.junit.Test;

public class WWParticleAnimationTest {

    @Test
    public void testParticleAnimation() {
        WWParticleAnimation pa = new WWParticleAnimation() {
            @Override
            public float getParticleRate() {
                return 1.0f;
            }

            @Override
            public void startParticle(WWParticleEmitter emitter, WWParticleEmitter.Particle particle, long worldTime) {
            }

            @Override
            public void updateParticle(WWParticleEmitter emitter, WWParticleEmitter.Particle particle, long worldTime) {
            }
        };
        Assert.assertNotNull(pa);
    }
}
