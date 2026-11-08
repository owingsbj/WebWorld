package com.gallantrealm.myworld.model;

import org.junit.Assert;
import org.junit.Test;

public class WWTorusTest {

    @Test
    public void testTorusCreationAndPenetration() {
        WWTorus torus = new WWTorus(2.0f, 2.0f, 2.0f);
        Assert.assertEquals(2.0f, torus.size.x, 1e-3f);
        Assert.assertNotNull(torus.getEdgePoints());

        WWVector point = new WWVector(0.375f, 0.0f, 0.0f);
        WWVector position = new WWVector(0.0f, 0.0f, 0.0f);
        WWQuaternion rotation = new WWQuaternion();
        WWVector tempPoint = new WWVector();
        WWVector penetration = new WWVector();

        torus.getPenetration(point, position, rotation, 0L, tempPoint, penetration);
        Assert.assertNotNull(penetration);
    }
}
