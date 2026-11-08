package com.gallantrealm.myworld.model;

import org.junit.Assert;
import org.junit.Test;

public class WWSphereTest {

    private static final float EPSILON = 1e-3f;

    @Test
    public void testSphereCreation() {
        WWSphere sphere = new WWSphere(2.0f, 2.0f, 2.0f);
        Assert.assertEquals(2.0f, sphere.size.x, EPSILON);
        Assert.assertNotNull(sphere.getEdgePoints());
    }
}
