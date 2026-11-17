package com.gallantrealm.myworld.model;

import org.junit.Assert;
import org.junit.Test;

public class WWMatrixTest {

    private static final float EPSILON = 1e-3f;

    @Test
    public void testIdentityMatrix() {
        WWMatrix matrix = new WWMatrix();
        float[] m = matrix.m;

        Assert.assertEquals(1.0f, m[0], EPSILON);  // Col 0, row 0 (World X)
        Assert.assertEquals(1.0f, m[5], EPSILON);  // Col 1, row 1 (World Z)
        Assert.assertEquals(1.0f, m[10], EPSILON); // Col 2, row 2 (World Y)
        Assert.assertEquals(1.0f, m[15], EPSILON); // Col 3, row 3 (W)
    }

    @Test
    public void testMoveInWorldSpace() {
        WWMatrix matrix = new WWMatrix();
        matrix.move(10.0f, 20.0f, 30.0f); // x=10 (World X), y=20 (World Y), z=30 (World Z)

        float[] m = matrix.m;
        // In OpenGL matrix: Col 3 is translation (m[12]=OpenGL X, m[13]=OpenGL Y, m[14]=OpenGL Z)
        // OpenGL X = World X = 10
        // OpenGL Y = World Z = 30
        // OpenGL Z = World Y = 20
        Assert.assertEquals(10.0f, m[12], EPSILON);
        Assert.assertEquals(30.0f, m[13], EPSILON);
        Assert.assertEquals(20.0f, m[14], EPSILON);
        Assert.assertEquals(1.0f, m[15], EPSILON);
    }

    @Test
    public void testScaleInWorldSpace() {
        WWMatrix matrix = new WWMatrix();
        matrix.scale(2.0f, 3.0f, 4.0f); // scale x=2 (World X), y=3 (World Y), z=4 (World Z)

        float[] m = matrix.m;
        // Col 0 (World X) scaled by 2
        Assert.assertEquals(2.0f, m[0], EPSILON);
        // Col 1 (World Z) scaled by 4
        Assert.assertEquals(4.0f, m[5], EPSILON);
        // Col 2 (World Y) scaled by 3
        Assert.assertEquals(3.0f, m[10], EPSILON);
    }

    @Test
    public void testRotateMatrix() {
        WWMatrix matrix = new WWMatrix();
        WWQuaternion q = new WWQuaternion(90.0f, 0.0f, 0.0f, 1.0f); // 90 deg around Z
        matrix.rotate(q);

        float[] m = matrix.m;
        Assert.assertEquals(0.0f, m[0], EPSILON);
        Assert.assertEquals(0.0f, m[1], EPSILON);
        Assert.assertEquals(1.0f, m[2], EPSILON);
    }

    @Test
    public void testShearDoesNotCorruptW() {
        WWMatrix matrix = new WWMatrix();
        matrix.shear(0.5f, 0.25f, 0.0f);

        float[] m = matrix.m;
        // m[7] is W component for Col 1 - MUST remain 0
        Assert.assertEquals(0.0f, m[7], EPSILON);
        Assert.assertEquals(1.0f, m[15], EPSILON);

        // Shearing along height (World Z):
        // m[4] (OpenGL X / World X) receives 0.5
        Assert.assertEquals(0.5f, m[4], EPSILON);
        // m[6] (OpenGL Z / World Y) receives 0.25
        Assert.assertEquals(0.25f, m[6], EPSILON);
    }

    @Test
    public void testTaperInWorldSpace() {
        WWMatrix matrix = new WWMatrix();
        matrix.taper(0.2f, 0.0f, 0.0f); // taper rate along height (World Z)

        float[] m = matrix.m;
        // Col 1, row 3 (m[7]) receives 0.2
        Assert.assertEquals(0.2f, m[7], EPSILON);
        Assert.assertEquals(1.0f, m[15], EPSILON);
    }
}
