package com.gallantrealm.myworld.model;

import org.junit.Assert;
import org.junit.Test;

public class WWObjectTest {

    private static final float EPSILON = 1e-2f;

    @Test
    public void testObjectCreation() {
        WWObject obj = new WWBox();
        obj.setName("Object1");
        Assert.assertEquals("Object1", obj.getName());
        Assert.assertNotNull(obj.getPosition());
        Assert.assertNotNull(obj.getRotation());
        Assert.assertNotNull(obj.getSize());
    }

    @Test
    public void testTransformAndAntiTransform() {
        WWVector position = new WWVector(10.0f, 20.0f, 30.0f);
        WWQuaternion rotation = new WWQuaternion().yaw(90.0f); // 90 deg around Z

        WWVector point = new WWVector(1.0f, 0.0f, 5.0f); // 1 right, 0 forward, 5 up
        WWVector pointCopy = point.clone();

        // Transform: 90 deg around Z turns (1,0,5) to (0,1,5), then + (10,20,30) = (10, 21, 35)
        WWObject.transform(point, position, rotation, null);
        Assert.assertEquals(10.0f, point.x, EPSILON);
        Assert.assertEquals(21.0f, point.y, EPSILON);
        Assert.assertEquals(35.0f, point.z, EPSILON);

        // Anti-transform restores original point
        WWObject.antiTransform(point, position, rotation, 0);
        Assert.assertEquals(pointCopy.x, point.x, EPSILON);
        Assert.assertEquals(pointCopy.y, point.y, EPSILON);
        Assert.assertEquals(pointCopy.z, point.z, EPSILON);
    }

    @Test
    public void testBoxPenetrationZUp() {
        WWBox box = new WWBox(2.0f, 4.0f, 6.0f); // Size: X=2 (width), Y=4 (depth), Z=6 (height)
        WWVector position = new WWVector(0.0f, 0.0f, 0.0f);
        WWQuaternion rotation = new WWQuaternion();

        // Point penetrating slightly from top (+Z = +2.8, inside top face at +3.0)
        WWVector pointTop = new WWVector(0.0f, 0.0f, 2.8f);
        WWVector tempPoint = new WWVector();
        WWVector penetration = new WWVector();
        box.getPenetration(pointTop, position, rotation, 0, tempPoint, penetration);

        // Minimal penetration pushes inward/outward along -Z direction (magnitude -0.2)
        Assert.assertEquals(0.0f, penetration.x, EPSILON);
        Assert.assertEquals(0.0f, penetration.y, EPSILON);
        Assert.assertEquals(-0.2f, penetration.z, EPSILON);
    }

    @Test
    public void testCylinderPenetrationZUp() {
        WWCylinder cylinder = new WWCylinder(2.0f, 2.0f, 4.0f); // Radius=1 in XY, Height=4 along Z
        WWVector position = new WWVector(0.0f, 0.0f, 0.0f);
        WWQuaternion rotation = new WWQuaternion();

        // Point penetrating from top (+Z = 1.8, inside top face at +2.0)
        WWVector pointTop = new WWVector(0.0f, 0.0f, 1.8f);
        WWVector tempPoint = new WWVector();
        WWVector penetration = new WWVector();
        cylinder.getPenetration(pointTop, position, rotation, 0, tempPoint, penetration);

        // Penetration pushes along -Z by -0.2
        Assert.assertEquals(0.0f, penetration.x, EPSILON);
        Assert.assertEquals(0.0f, penetration.y, EPSILON);
        Assert.assertEquals(-0.2f, penetration.z, EPSILON);
    }

    @Test
    public void testSpherePenetrationZUp() {
        WWSphere sphere = new WWSphere(2.0f, 2.0f, 2.0f); // Radius=1
        WWVector position = new WWVector(0.0f, 0.0f, 0.0f);
        WWQuaternion rotation = new WWQuaternion();

        // Point inside top of sphere (+Z = 0.8)
        WWVector pointTop = new WWVector(0.0f, 0.0f, 0.8f);
        WWVector tempPoint = new WWVector();
        WWVector penetration = new WWVector();
        sphere.getPenetration(pointTop, position, rotation, 0, tempPoint, penetration);

        // Radial penetration pointing along -Z (-0.2)
        Assert.assertEquals(0.0f, penetration.x, EPSILON);
        Assert.assertEquals(0.0f, penetration.y, EPSILON);
        Assert.assertEquals(-0.2f, penetration.z, EPSILON);
    }

    @Test
    public void testMeshHeightValueZUp() {
        WWMesh mesh = new WWMesh(10.0f, 10.0f, 2.0f); // 10x10 ground mesh, height 2 along Z
        mesh.setPosition(new WWVector(0.0f, 0.0f, 0.0f));

        // Point at center (0,0)
        WWVector centerPoint = new WWVector(0.0f, 0.0f, 0.0f);
        float meshVal = mesh.getMeshValue(centerPoint);

        // Default mesh point value is 5000 / 10000 = 0.5 -> (0.5 - 0.5) * 2 = 0.0 along Z
        Assert.assertEquals(0.0f, meshVal, EPSILON);
    }
}
