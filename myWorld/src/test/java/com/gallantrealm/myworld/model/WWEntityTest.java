package com.gallantrealm.myworld.model;

import org.junit.Assert;
import org.junit.Test;

public class WWEntityTest {

    @Test
    public void testEntityCreation() {
        WWEntity entity = new WWEntity() {
        };
        entity.setName("TestEntity");
        Assert.assertEquals("TestEntity", entity.getName());
    }
}
