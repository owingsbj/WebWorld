package com.gallantrealm.myworld.model;

import org.junit.Assert;
import org.junit.Test;

public class WWParticleEmitterTest {

    @Test
    public void testParticleEmitter() {
        WWParticleEmitter pe = new WWParticleEmitter();
        Assert.assertNotNull(pe);
    }
}
