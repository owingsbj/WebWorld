package com.gallantrealm.myworld.model;

import org.junit.Assert;
import org.junit.Test;

public class WWSculptyTest {

    @Test
    public void testSculptyCreation() {
        WWSculpty sculpty = new WWSculpty(2.0f, 2.0f, 2.0f);
        Assert.assertEquals(2.0f, sculpty.size.x, 1e-3f);
        Assert.assertEquals("sculptedsphere", sculpty.getSculptyTexture());
        Assert.assertTrue(sculpty.isClosed());
    }
}
