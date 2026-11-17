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
        // Col 3 is translation (m[12]=World X, m[13]=World Z, m[14]=World Y)
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
        // 90 deg around Z in OpenGL (X,Z,Y) space
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
        Assert.assertEquals(0.5f, m[4], EPSILON);
        Assert.assertEquals(0.25f, m[6], EPSILON);
    }

    @Test
    public void testTaperInWorldSpace() {
        WWMatrix matrix = new WWMatrix();
        matrix.taper(0.2f, 0.0f, 0.0f); // taper rate along height (World Z)

        float[] m = matrix.m;
        Assert.assertEquals(0.2f, m[7], EPSILON);
        Assert.assertEquals(1.0f, m[15], EPSILON);
    }

    @Test
    public void testMultiplyMMOrthogonalityAndAccuracy() {
        WWQuaternion q1 = new WWQuaternion(30.0f, 45.0f, 60.0f);
        WWQuaternion q2 = new WWQuaternion(15.0f, -20.0f, 35.0f);

        float[] m1 = new float[16];
        float[] m2 = new float[16];
        float[] result = new float[16];

        q1.toMatrix(m1);
        q2.toMatrix(m2);

        WWMatrix.multiplyMM(result, m1, m2);

        // Check orthogonality of 3x3 rotation columns (col 0, col 1, col 2 length must be 1.0)
        float lenCol0 = (float) Math.sqrt(result[0] * result[0] + result[1] * result[1] + result[2] * result[2]);
        float lenCol1 = (float) Math.sqrt(result[4] * result[4] + result[5] * result[5] + result[6] * result[6]);
        float lenCol2 = (float) Math.sqrt(result[8] * result[8] + result[9] * result[9] + result[10] * result[10]);

        Assert.assertEquals(1.0f, lenCol0, EPSILON);
        Assert.assertEquals(1.0f, lenCol1, EPSILON);
        Assert.assertEquals(1.0f, lenCol2, EPSILON);

        // Check column dot products (must be 0 for orthogonal matrix without squashing)
        float dot01 = result[0] * result[4] + result[1] * result[5] + result[2] * result[6];
        float dot02 = result[0] * result[8] + result[1] * result[9] + result[2] * result[10];
        float dot12 = result[4] * result[8] + result[5] * result[9] + result[6] * result[10];

        Assert.assertEquals(0.0f, dot01, EPSILON);
        Assert.assertEquals(0.0f, dot02, EPSILON);
        Assert.assertEquals(0.0f, dot12, EPSILON);

        // Multiplying by combined quaternion should equal result from multiplyMM
        WWQuaternion qCombined = q1.clone().rotate(q2);
        float[] mCombined = new float[16];
        qCombined.toMatrix(mCombined);

        for (int i = 0; i < 16; i++) {
            Assert.assertEquals("Mismatch at matrix index " + i, mCombined[i], result[i], EPSILON);
        }
    }
}
