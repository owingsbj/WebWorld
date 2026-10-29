package com.gallantrealm.myworld.model;

import org.junit.Assert;
import org.junit.Test;

public class WWQuaternionTest {

    private static final float EPSILON = 1e-3f;

    @Test
    public void testNormalizeUnitQuaternion() {
        WWQuaternion q = new WWQuaternion();
        q.set(1.0f, 0.0f, 0.0f, 0.0f);
        Assert.assertEquals(1.0f, q.getW(), EPSILON);
        Assert.assertEquals(0.0f, q.getX(), EPSILON);
        Assert.assertEquals(0.0f, q.getY(), EPSILON);
        Assert.assertEquals(0.0f, q.getZ(), EPSILON);
    }

    @Test
    public void testNormalizeNonUnitQuaternion() {
        WWQuaternion q = new WWQuaternion();
        q.set(2.0f, 0.0f, 0.0f, 0.0f); // w=2, x=0, y=0, z=0 before norm
        float length = (float) Math.sqrt(q.getW() * q.getW() + q.getX() * q.getX() + q.getY() * q.getY() + q.getZ() * q.getZ());
        Assert.assertEquals(1.0f, length, EPSILON);
    }

    @Test
    public void testNormalizeZeroQuaternion() {
        WWQuaternion q = new WWQuaternion();
        q.set(0.0f, 0.0f, 0.0f, 0.0f);
        Assert.assertFalse(Float.isNaN(q.getW()));
        Assert.assertFalse(Float.isInfinite(q.getW()));
        Assert.assertEquals(1.0f, q.getW(), EPSILON);
    }

    @Test
    public void testAngleAxisConstructorNegativeAngle() {
        WWQuaternion qNegative = new WWQuaternion(-90.0f, 0.0f, 1.0f, 0.0f);

        // A -90 degree rotation around Y should have y = sin(-45deg)
        float expectedW = (float) Math.cos(Math.toRadians(-45));
        float expectedY = (float) Math.sin(Math.toRadians(-45));
        Assert.assertEquals(expectedW, qNegative.getW(), EPSILON);
        Assert.assertEquals(expectedY, qNegative.getY(), EPSILON);
    }

    @Test
    public void testEulerAngleConversionRoundTrip() {
        float pitch = 30.0f;
        float roll = 45.0f;
        float yaw = 60.0f;

        WWQuaternion q = new WWQuaternion(pitch, roll, yaw);

        Assert.assertEquals(pitch, q.getPitch(), 1.0f);
        Assert.assertEquals(roll, q.getRoll(), 1.0f);
        Assert.assertEquals(yaw, q.getYaw(), 1.0f);
    }

    @Test
    public void testPitchRotationAroundWorldX() {
        // Pitch = 90 deg around World X axis
        WWQuaternion q = new WWQuaternion().pitch(90.0f);

        Assert.assertEquals(90.0f, q.getPitch(), 1.0f);
        Assert.assertEquals(0.0f, q.getRoll(), 1.0f);
        Assert.assertEquals(0.0f, q.getYaw(), 1.0f);

        // Rotating World Y (0,1,0) by 90deg pitch yields World Z (0,0,1)
        WWVector v = new WWVector(0.0f, 1.0f, 0.0f);
        v.rotate(q);
        Assert.assertEquals(0.0f, v.x, EPSILON);
        Assert.assertEquals(0.0f, v.y, EPSILON);
        Assert.assertEquals(1.0f, v.z, EPSILON);
    }

    @Test
    public void testRollRotationAroundWorldY() {
        // Roll = 45 deg around World Y axis
        WWQuaternion q = new WWQuaternion().roll(45.0f);

        Assert.assertEquals(0.0f, q.getPitch(), 1.0f);
        Assert.assertEquals(45.0f, q.getRoll(), 1.0f);
        Assert.assertEquals(0.0f, q.getYaw(), 1.0f);

        // Rotating World Z (0,0,1) by 90deg roll yields World X (1,0,0)
        WWQuaternion q90 = new WWQuaternion().roll(90.0f);
        WWVector v = new WWVector(0.0f, 0.0f, 1.0f);
        v.rotate(q90);
        Assert.assertEquals(1.0f, v.x, EPSILON);
        Assert.assertEquals(0.0f, v.y, EPSILON);
        Assert.assertEquals(0.0f, v.z, EPSILON);
    }

    @Test
    public void testYawRotationAroundWorldZ() {
        // Yaw = 90 deg around World Z axis
        WWQuaternion q = new WWQuaternion().yaw(90.0f);

        Assert.assertEquals(0.0f, q.getPitch(), 1.0f);
        Assert.assertEquals(0.0f, q.getRoll(), 1.0f);
        Assert.assertEquals(90.0f, q.getYaw(), 1.0f);

        // Rotating World X (1,0,0) by 90deg yaw yields World Y (0,1,0)
        WWVector v = new WWVector(1.0f, 0.0f, 0.0f);
        v.rotate(q);
        Assert.assertEquals(0.0f, v.x, EPSILON);
        Assert.assertEquals(1.0f, v.y, EPSILON);
        Assert.assertEquals(0.0f, v.z, EPSILON);
    }

    @Test
    public void testToMatrixIdentity() {
        WWQuaternion q = new WWQuaternion();
        float[] mat = new float[16];
        q.toMatrix(mat);

        Assert.assertEquals(1.0f, mat[0], EPSILON); // col 0, row 0
        Assert.assertEquals(1.0f, mat[5], EPSILON); // col 1, row 1
        Assert.assertEquals(1.0f, mat[10], EPSILON); // col 2, row 2
        Assert.assertEquals(1.0f, mat[15], EPSILON); // col 3, row 3
    }

    @Test
    public void testToMatrixRotation() {
        // 90 deg around World Z (Yaw)
        WWQuaternion q = new WWQuaternion(90.0f, 0.0f, 0.0f, 1.0f);
        float[] mat = new float[16];
        q.toMatrix(mat);

        // In World space (Z-up), rotating 90 deg around Z turns X (1,0,0) into Y (0,1,0).
        // In OpenGL matrix (where OpenGL Y=World Z, OpenGL Z=World Y):
        // Col 0 (world X -> OpenGL space): X=0, Y(world Z)=0, Z(world Y)=1 -> mat[0]=0, mat[1]=0, mat[2]=1
        Assert.assertEquals(0.0f, mat[0], EPSILON);
        Assert.assertEquals(0.0f, mat[1], EPSILON);
        Assert.assertEquals(1.0f, mat[2], EPSILON);
    }
}
