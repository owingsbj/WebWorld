package com.gallantrealm.myworld.model;

import com.gallantrealm.myworld.communication.DataInputStreamX;
import com.gallantrealm.myworld.communication.DataOutputStreamX;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class WWTextureTest {

    @Test
    public void testTextureCreation() {
        WWTexture tex = new WWTexture("test.png", 2.0f, 2.0f);
        Assert.assertEquals("test.png", tex.getName());
        Assert.assertEquals(2.0f, tex.getScaleX(), 1e-3f);
        Assert.assertNotNull(tex.clone());
    }

    @Test
    public void testSendAndReceive() throws IOException {
        WWTexture original = new WWTexture("wood.png", 1.5f, 2.0f, 90.0f, 0.1f, 0.2f, true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStreamX dos = new DataOutputStreamX(baos);
        original.send(dos);
        dos.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        DataInputStreamX dis = new DataInputStreamX(bais);
        WWTexture received = new WWTexture();
        received.receive(dis);

        Assert.assertEquals("wood.png", received.getName());
        Assert.assertEquals(1.5f, received.getScaleX(), 1e-3f);
        Assert.assertTrue(received.isPixelated());
    }
}
