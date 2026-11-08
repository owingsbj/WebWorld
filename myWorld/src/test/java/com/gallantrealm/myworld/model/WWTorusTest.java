package com.gallantrealm.myworld.model;

import org.junit.Assert;
import org.junit.Test;

public class WWTorusTest {

    @Test
    public void testTorusCreation() {
        WWTorus torus = new WWTorus(2.0f, 2.0f, 2.0f);
        Assert.assertEquals(2.0f, torus.size.x, 1e-3f);
        Assert.assertNotNull(torus.getEdgePoints());
    }
}
