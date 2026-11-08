package com.gallantrealm.myworld.model;

import org.junit.Assert;
import org.junit.Test;

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
}
