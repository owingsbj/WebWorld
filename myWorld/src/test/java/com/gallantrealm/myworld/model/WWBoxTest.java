package com.gallantrealm.myworld.model;

import com.gallantrealm.myworld.communication.DataInputStreamX;
import com.gallantrealm.myworld.communication.DataOutputStreamX;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class WWBoxTest {

    private static final float EPSILON = 1e-3f;

    @Test
    public void testBoxCreationAndPenetrationCenter() {
        WWBox box = new WWBox(2.0f, 2.0f, 2.0f);
        Assert.assertEquals(2.0f, box.size.x, EPSILON);

        WWVector point = new WWVector(0.0f, 0.0f, 0.0f);
        WWVector position = new WWVector(0.0f, 0.0f, 0.0f);
        WWQuaternion rotation = new WWQuaternion();
        WWVector tempPoint = new WWVector();
        WWVector penetration = new WWVector();

        box.getPenetration(point, position, rotation, 0L, tempPoint, penetration);
        Assert.assertFalse(penetration.isZero());
    }

    @Test
    public void testBoxPenetrationOutside() {
        WWBox box = new WWBox(2.0f, 2.0f, 2.0f);

        WWVector point = new WWVector(5.0f, 5.0f, 5.0f);
        WWVector position = new WWVector(0.0f, 0.0f, 0.0f);
        WWQuaternion rotation = new WWQuaternion();
        WWVector tempPoint = new WWVector();
        WWVector penetration = new WWVector();

        box.getPenetration(point, position, rotation, 0L, tempPoint, penetration);
        Assert.assertTrue(penetration.isZero());
    }

    @Test
    public void testBoxPenetrationFaces() {
        WWBox box = new WWBox(2.0f, 2.0f, 2.0f);
        WWVector position = new WWVector(0.0f, 0.0f, 0.0f);
        WWQuaternion rotation = new WWQuaternion();
        WWVector tempPoint = new WWVector();
        WWVector penetration = new WWVector();

        box.getPenetration(new WWVector(0.8f, 0.0f, 0.0f), position, rotation, 0L, tempPoint, penetration);
        Assert.assertEquals(-0.2f, penetration.x, EPSILON);
        Assert.assertEquals(0.0f, penetration.y, EPSILON);
        Assert.assertEquals(0.0f, penetration.z, EPSILON);
    }

    @Test
    public void testBoxHollowAndCutout() {
        WWBox box = new WWBox(2.0f, 2.0f, 2.0f);
        box.setHollow(0.5f);
        box.setCutout(0.25f, 0.75f);

        WWVector point = new WWVector(0.0f, 0.0f, 0.0f);
        WWVector position = new WWVector(0.0f, 0.0f, 0.0f);
        WWQuaternion rotation = new WWQuaternion();
        WWVector tempPoint = new WWVector();
        WWVector penetration = new WWVector();

        box.getPenetration(point, position, rotation, 0L, tempPoint, penetration);
        Assert.assertTrue(penetration.isZero());
    }

    @Test
    public void testBoxTranslationAndRotation() {
        WWBox box = new WWBox(2.0f, 2.0f, 2.0f);
        WWVector position = new WWVector(10.0f, 0.0f, 0.0f);
        WWQuaternion rotation = new WWQuaternion();
        WWVector tempPoint = new WWVector();
        WWVector penetration = new WWVector();

        box.getPenetration(new WWVector(10.0f, 0.0f, 0.0f), position, rotation, 0L, tempPoint, penetration);
        Assert.assertFalse(penetration.isZero());

        box.getPenetration(new WWVector(0.0f, 0.0f, 0.0f), position, rotation, 0L, tempPoint, penetration);
        Assert.assertTrue(penetration.isZero());
    }

    @Test
    public void testSendAndReceive() throws IOException {
        WWBox original = new WWBox(2.0f, 3.0f, 4.0f);
        original.noXPenetration = true;
        original.setHollow(0.2f);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStreamX dos = new DataOutputStreamX(baos);
        original.send(dos);
        dos.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        DataInputStreamX dis = new DataInputStreamX(bais);
        WWBox received = new WWBox();
        received.receive(dis);

        Assert.assertTrue(received.noXPenetration);
        Assert.assertEquals(0.2f, received.getHollow(), EPSILON);
        Assert.assertEquals(2.0f, received.size.x, EPSILON);
    }
}
