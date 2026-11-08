package com.gallantrealm.myworld.model;

import org.junit.Assert;
import org.junit.Test;

public class WWBehaviorTest {

    @Test
    public void testBehaviorTimer() {
        WWBehavior behavior = new WWBehavior() {
        };
        behavior.setTimer(1000);
        Assert.assertEquals(1000, behavior.getTimer());
        Assert.assertFalse(behavior.touchEvent(null, null, 0, 0, 0));
        Assert.assertFalse(behavior.pressEvent(null, null, 0, 0, 0));
        Assert.assertFalse(behavior.dragEvent(null, null, 0, 0, 0));
        Assert.assertFalse(behavior.releaseEvent(null, null, 0, 0, 0));
        Assert.assertFalse(behavior.collideEvent(null, null, null));
    }
}
