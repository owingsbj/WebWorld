package com.gallantrealm.myworld.model;

import org.junit.Assert;
import org.junit.Test;

public class WWBoxTest {

    private static final float EPSILON = 1e-3f;

    @Test
    public void testBoxCreationAndPenetration() {
        WWBox box = new WWBox(2.0f, 2.0f, 2.0f);
        Assert.assertEquals(2.0f, box.size.x, EPSILON);
        Assert.assertEquals(2.0f, box.size.y, EPSILON);
        Assert.assertEquals(2.0f, box.size.z, EPSILON);

        WWVector point = new WWVector(0.0f, 0.0f, 0.0f);
        WWVector position = new WWVector(0.0f, 0.0f, 0.0f);
        WWQuaternion rotation = new WWQuaternion();
        WWVector tempPoint = new WWVector();
        WWVector penetration = new WWVector();

        box.getPenetration(point, position, rotation, 0L, tempPoint, penetration);
        Assert.assertNotNull(penetration);
    }
}
