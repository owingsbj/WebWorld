package com.gallantrealm.myworld.model;

import org.junit.Assert;
import org.junit.Test;

public class WWVectorTest {

    private static final float EPSILON = 1e-3f;

    @Test
    public void testLengthAndNormalize() {
        WWVector v = new WWVector(3.0f, 4.0f, 0.0f);
        Assert.assertEquals(5.0f, v.length(), EPSILON);

        v.normalize();
        Assert.assertEquals(1.0f, v.length(), EPSILON);
        Assert.assertEquals(0.6f, v.x, EPSILON);
        Assert.assertEquals(0.8f, v.y, EPSILON);
        Assert.assertEquals(0.0f, v.z, EPSILON);
    }

    @Test
    public void testDotAndCrossProduct() {
        WWVector v1 = new WWVector(1.0f, 0.0f, 0.0f); // World X
        WWVector v2 = new WWVector(0.0f, 1.0f, 0.0f); // World Y

        Assert.assertEquals(0.0f, v1.dot(v2), EPSILON);

        // X cross Y should yield Z (0, 0, 1) in right-hand system
        WWVector cross = v1.clone().cross(v2);
        Assert.assertEquals(0.0f, cross.x, EPSILON);
        Assert.assertEquals(0.0f, cross.y, EPSILON);
        Assert.assertEquals(1.0f, cross.z, EPSILON);
    }

    @Test
    public void testPanAndTiltInWorldSpace() {
        // Straight forward along World Y
        WWVector fwd = new WWVector(0.0f, 10.0f, 0.0f);
        Assert.assertEquals(0.0f, fwd.getPan(), EPSILON);
        Assert.assertEquals(0.0f, fwd.getTilt(), EPSILON);

        // Straight right along World X
        WWVector right = new WWVector(10.0f, 0.0f, 0.0f);
        Assert.assertEquals(90.0f, right.getPan(), EPSILON);
        Assert.assertEquals(0.0f, right.getTilt(), EPSILON);

        // Straight up along World Z
        WWVector up = new WWVector(0.0f, 0.0f, 10.0f);
        Assert.assertEquals(90.0f, up.getTilt(), EPSILON);

        // Straight down along World Z
        WWVector down = new WWVector(0.0f, 0.0f, -10.0f);
        Assert.assertEquals(-90.0f, down.getTilt(), EPSILON);
    }

    @Test
    public void testGetReflectionPerpendicular() {
        // Vertical impact onto floor (mirror normal = Up (0,0,1))
        WWVector velocity = new WWVector(0.0f, 0.0f, -10.0f);
        WWVector floorNormal = new WWVector(0.0f, 0.0f, 1.0f);

        WWVector reflected = velocity.getReflection(floorNormal);

        // Should bounce straight up (+10 in Z)
        Assert.assertEquals(0.0f, reflected.x, EPSILON);
        Assert.assertEquals(0.0f, reflected.y, EPSILON);
        Assert.assertEquals(10.0f, reflected.z, EPSILON);
    }

    @Test
    public void testGetReflectionAngled() {
        // 45 degree impact onto floor (10 forward, 10 down)
        WWVector velocity = new WWVector(10.0f, 0.0f, -10.0f);
        WWVector floorNormal = new WWVector(0.0f, 0.0f, 1.0f);

        WWVector reflected = velocity.getReflection(floorNormal);

        // Should bounce with same forward velocity and reversed vertical velocity (+10 in X, +10 in Z)
        Assert.assertEquals(10.0f, reflected.x, EPSILON);
        Assert.assertEquals(0.0f, reflected.y, EPSILON);
        Assert.assertEquals(10.0f, reflected.z, EPSILON);
    }

    @Test
    public void testReduceLength() {
        WWVector v = new WWVector(10.0f, 0.0f, 0.0f);
        v.reduce(3.0f); // Reduce by 3 units -> length becomes 7

        Assert.assertEquals(7.0f, v.length(), EPSILON);
        Assert.assertEquals(7.0f, v.x, EPSILON);
    }

    @Test
    public void testRotateVectorWithQuaternion() {
        WWVector v = new WWVector(0.0f, 1.0f, 0.0f); // World Y
        WWQuaternion qPitch = new WWQuaternion().pitch(90.0f);

        v.rotate(qPitch); // 90 deg pitch turns World Y into World Z (0, 0, 1)
        Assert.assertEquals(0.0f, v.x, EPSILON);
        Assert.assertEquals(0.0f, v.y, EPSILON);
        Assert.assertEquals(1.0f, v.z, EPSILON);
    }
}
