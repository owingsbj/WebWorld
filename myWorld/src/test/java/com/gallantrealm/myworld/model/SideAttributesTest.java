package com.gallantrealm.myworld.model;

import com.gallantrealm.myworld.communication.DataInputStreamX;
import com.gallantrealm.myworld.communication.DataOutputStreamX;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SideAttributesTest {

    @Test
    public void testDefaultSideAttributes() {
        SideAttributes sa = new SideAttributes();
        Assert.assertNotNull(sa.getColor());
        Assert.assertNotNull(sa.getTexture());
        Assert.assertNotNull(SideAttributes.defaultSurface);
        Assert.assertTrue(SideAttributes.defaultSurface.isDefault);
    }

    @Test
    public void testClone() {
        SideAttributes sa = new SideAttributes();
        sa.shininess = 0.8f;
        SideAttributes clone = (SideAttributes) sa.clone();
        Assert.assertNotNull(clone);
        Assert.assertEquals(0.8f, clone.shininess, 1e-3f);
    }

    @Test
    public void testSendAndReceive() throws IOException {
        SideAttributes original = new SideAttributes();
        original.shininess = 0.5f;
        original.fullBright = true;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStreamX dos = new DataOutputStreamX(baos);
        original.send(dos);
        dos.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        DataInputStreamX dis = new DataInputStreamX(bais);
        SideAttributes received = new SideAttributes();
        received.receive(dis);

        Assert.assertEquals(0.5f, received.shininess, 1e-3f);
        Assert.assertTrue(received.fullBright);
    }
}
