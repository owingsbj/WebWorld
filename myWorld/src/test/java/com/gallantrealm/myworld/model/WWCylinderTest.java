package com.gallantrealm.myworld.model;

import org.junit.Assert;
import org.junit.Test;

public class WWCylinderTest {

    @Test
    public void testCylinderCreation() {
        WWCylinder cyl = new WWCylinder(2.0f, 2.0f, 2.0f);
        Assert.assertEquals(2.0f, cyl.size.x, 1e-3f);
        Assert.assertNotNull(cyl.getEdgePoints());
    }
}
