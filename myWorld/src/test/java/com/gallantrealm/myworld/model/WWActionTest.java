package com.gallantrealm.myworld.model;

import org.junit.Assert;
import org.junit.Test;

public class WWActionTest {

    @Test
    public void testActionEnabled() {
        WWAction action = new WWAction() {
            @Override
            public String getName() {
                return "Test";
            }
        };
        Assert.assertTrue(action.isEnabled());
        Assert.assertEquals("Test", action.getName());
        action.setEnabled(false);
        Assert.assertFalse(action.isEnabled());
        Assert.assertNotNull(action.toString());
    }
}
