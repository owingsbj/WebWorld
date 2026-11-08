package com.gallantrealm.myworld.model;

import org.junit.Assert;
import org.junit.Test;

public class WWConstantTest {

    @Test
    public void testConstants() {
        Assert.assertEquals(0, WWConstant.SIDE_ALL);
        Assert.assertEquals(1, WWConstant.SIDE_TOP);
        Assert.assertEquals(15, WWConstant.NSIDES);
    }
}
