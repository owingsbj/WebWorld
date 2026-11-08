package com.gallantrealm.myworld.model;

import org.junit.Assert;
import org.junit.Test;

public class SideAttributesTest {

    @Test
    public void testDefaultSideAttributes() {
        SideAttributes sa = new SideAttributes();
        Assert.assertNotNull(sa.getColor());
        Assert.assertNotNull(sa.getTexture());
        Assert.assertNotNull(SideAttributes.defaultSurface);
        Assert.assertTrue(SideAttributes.defaultSurface.isDefault);
    }

    @Test
    public void testClone() {
        SideAttributes sa = new SideAttributes();
        sa.shininess = 0.8f;
        SideAttributes clone = (SideAttributes) sa.clone();
        Assert.assertEquals(0.8f, clone.shininess, 1e-3f);
    }
}
