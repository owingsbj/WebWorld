package com.gallantrealm.myworld.model;

import com.gallantrealm.myworld.communication.DataInputStreamX;
import com.gallantrealm.myworld.communication.DataOutputStreamX;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class WWColorTest {

    private static final float EPSILON = 1e-2f;

    @Test
    public void testConstructorsAndGetters() {
        WWColor c1 = new WWColor();
        Assert.assertEquals(0xFFFFFF, c1.getRGB());

        WWColor c2 = new WWColor(0x123456);
        Assert.assertEquals(0x123456, c2.getRGB());

        WWColor c3 = new WWColor(1.0f, 0.5f, 0.0f);
        Assert.assertEquals(1.0f, c3.getRed(), EPSILON);
        Assert.assertEquals(0.498f, c3.getGreen(), EPSILON);
        Assert.assertEquals(0.0f, c3.getBlue(), EPSILON);
    }

    @Test
    public void testDarkerAndMix() {
        WWColor c = new WWColor(1.0f, 1.0f, 1.0f);
        WWColor darker = c.darker();
        Assert.assertEquals(0.498f, darker.getRed(), EPSILON);

        c.mix(new WWColor(0.5f, 0.5f, 0.5f));
        Assert.assertEquals(0.498f, c.getRed(), EPSILON);
    }

    @Test
    public void testSendAndReceive() throws IOException {
        WWColor original = new WWColor(0.2f, 0.4f, 0.6f);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStreamX dos = new DataOutputStreamX(baos);
        original.send(dos);
        dos.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        DataInputStreamX dis = new DataInputStreamX(bais);
        WWColor received = new WWColor();
        received.receive(dis);

        Assert.assertEquals(original.getRGB(), received.getRGB());
    }
}
