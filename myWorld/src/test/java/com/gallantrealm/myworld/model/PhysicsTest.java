package com.gallantrealm.myworld.model;

import org.junit.Assert;
import org.junit.Test;

public class PhysicsTest {

    @Test
    public void testDefaultPhysics() {
        Physics p = new Physics();
        Assert.assertFalse(p.physical);
        Assert.assertTrue(p.solid);
        Assert.assertEquals(1.0f, p.friction, 1e-3f);
        Assert.assertNotNull(Physics.DEFAULT_PHYSICS);
    }

    @Test
    public void testClone() {
        Physics p = new Physics();
        p.physical = true;
        p.friction = 0.5f;
        Physics clone = (Physics) p.clone();
        Assert.assertTrue(clone.physical);
        Assert.assertEquals(0.5f, clone.friction, 1e-3f);
    }
}
