package com.gallantrealm.myworld.model;

import org.junit.Assert;
import org.junit.Test;

public class WWAnimationTest {

    @Test
    public void testAnimationMethods() {
        WWAnimation anim = new WWAnimation() {
            @Override
            public void getAnimatedPosition(WWObject object, WWVector position, long time) {
            }

            @Override
            public void getAnimatedRotation(WWObject object, WWQuaternion rotation, long time) {
            }
        };
        Assert.assertNotNull(anim);
    }
}
