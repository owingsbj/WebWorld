package com.gallantrealm.myworld.model;

import org.junit.Assert;
import org.junit.Test;

public class WWPlantTest {

    @Test
    public void testPlantCreation() {
        WWPlant plant = new WWPlant();
        Assert.assertNotNull(plant);
        Assert.assertEquals(WWPlant.TYPE_TREE, plant.getType());
    }
}
