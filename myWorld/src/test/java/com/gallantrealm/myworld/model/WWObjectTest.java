package com.gallantrealm.myworld.model;

import org.junit.Assert;
import org.junit.Test;

public class WWObjectTest {

    @Test
    public void testObjectCreation() {
        WWObject obj = new WWBox();
        obj.setName("Object1");
        Assert.assertEquals("Object1", obj.getName());
        Assert.assertNotNull(obj.getPosition());
        Assert.assertNotNull(obj.getRotation());
        Assert.assertNotNull(obj.getSize());
    }
}
