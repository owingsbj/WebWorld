package com.gallantrealm.myworld.model;

import org.junit.Assert;
import org.junit.Test;

public class WWTextureTest {

    @Test
    public void testTextureCreation() {
        WWTexture tex = new WWTexture("test.png", 2.0f, 2.0f);
        Assert.assertEquals("test.png", tex.getName());
        Assert.assertEquals(2.0f, tex.getScaleX(), 1e-3f);
        Assert.assertNotNull(tex.clone());
    }
}
