package com.gallantrealm.myworld.model;

import org.junit.Assert;
import org.junit.Test;

public class WWSphereTest {

    private static final float EPSILON = 1e-3f;

    @Test
    public void testSphereCreationAndPenetrationInside() {
        WWSphere sphere = new WWSphere(2.0f, 2.0f, 2.0f);
        Assert.assertEquals(2.0f, sphere.size.x, EPSILON);
        Assert.assertNotNull(sphere.getEdgePoints());

        WWVector point = new WWVector(0.1f, 0.1f, 0.1f);
        WWVector position = new WWVector(0.0f, 0.0f, 0.0f);
        WWQuaternion rotation = new WWQuaternion();
        WWVector tempPoint = new WWVector();
        WWVector penetration = new WWVector();

        sphere.getPenetration(point, position, rotation, 0L, tempPoint, penetration);
        Assert.assertFalse(penetration.isZero());
    }

    @Test
    public void testSpherePenetrationOutside() {
        WWSphere sphere = new WWSphere(2.0f, 2.0f, 2.0f);

        WWVector point = new WWVector(3.0f, 3.0f, 3.0f);
        WWVector position = new WWVector(0.0f, 0.0f, 0.0f);
        WWQuaternion rotation = new WWQuaternion();
        WWVector tempPoint = new WWVector();
        WWVector penetration = new WWVector();

        sphere.getPenetration(point, position, rotation, 0L, tempPoint, penetration);
        Assert.assertTrue(penetration.isZero());
    }
}
