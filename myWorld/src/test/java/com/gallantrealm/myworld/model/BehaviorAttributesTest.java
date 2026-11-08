package com.gallantrealm.myworld.model;

import org.junit.Assert;
import org.junit.Test;

public class BehaviorAttributesTest {

    @Test
    public void testBehaviorAttributesCreation() {
        BehaviorAttributes ba = new BehaviorAttributes("com.gallantrealm.myworld.model.WWBehavior");
        Assert.assertNotNull(ba);
    }
}
