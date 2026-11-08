package com.gallantrealm.myworld.model;

import org.junit.Assert;
import org.junit.Test;

public class WWMeshTest {

    @Test
    public void testMeshCreation() {
        WWMesh mesh = new WWMesh();
        Assert.assertNotNull(mesh);
    }
}
