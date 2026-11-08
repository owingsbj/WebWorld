package com.gallantrealm.myworld.model;

import com.gallantrealm.myworld.client.renderer.IRenderer;
import org.junit.Assert;
import org.junit.Test;

public class WWSimpleShapeTest {

    @Test
    public void testSimpleShapeCreation() {
        WWSimpleShape shape = new WWSimpleShape(1.0f, 2.0f, 3.0f) {
            @Override
            public void getPenetration(WWVector point, WWVector position, WWQuaternion rotation, long worldTime, WWVector tempPoint, WWVector penetration) {
            }

            @Override
            public void createRendering(IRenderer renderer, long worldTime) {
            }
        };
        Assert.assertEquals(1.0f, shape.size.x, 1e-3f);
        Assert.assertEquals(2.0f, shape.size.y, 1e-3f);
        Assert.assertEquals(3.0f, shape.size.z, 1e-3f);
    }
}
