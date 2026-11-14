package com.gallantrealm.myworld.model;

import com.gallantrealm.myworld.model.physics.NewPhysicsThread;
import org.junit.Assert;
import org.junit.Test;

public class PhysicsTest {

    @Test
    public void testDefaultPhysics() {
        Physics p = new Physics();
        Assert.assertFalse(p.physical);
        Assert.assertTrue(p.solid);
        Assert.assertEquals(1.0f, p.friction, 1e-3f);
        Assert.assertNotNull(Physics.DEFAULT_PHYSICS);
    }

    @Test
    public void testClone() {
        Physics p = new Physics();
        p.physical = true;
        p.friction = 0.5f;
        Physics clone = (Physics) p.clone();
        Assert.assertTrue(clone.physical);
        Assert.assertEquals(0.5f, clone.friction, 1e-3f);
    }

    @Test
    public void testRotationalFrictionInNewPhysicsThread() {
        WWWorld world = new WWWorld();

        WWBox ground = new WWBox(10.0f, 10.0f, 1.0f);
        ground.setPosition(0, 0, -0.5f);
        ground.solid = true;
        ground.physical = false;
        ground.friction = 0.9f;
        world.addObject(ground);

        WWCylinder cyl = new WWCylinder(1.0f, 0.5f, 2.0f);
        cyl.setPosition(0, 0, 0.99f); // resting on ground
        cyl.solid = true;
        cyl.physical = true;
        cyl.density = 0.25f;
        cyl.friction = 0.9f;
        cyl.setAngularVelocity(new WWVector(0, 0, 100.0f));
        world.addObject(cyl);

        NewPhysicsThread thread = new NewPhysicsThread(world, 50, 2);
        thread.performIteration(50);

        WWVector angularVel = new WWVector();
        cyl.getAngularVelocity(angularVel);
        Assert.assertTrue("Rotational friction should reduce angular velocity around normal axis", angularVel.z < 100.0f);
    }

    @Test
    public void testRollingAngularVelocityNotDampenedByNormalFriction() {
        WWWorld world = new WWWorld();

        WWBox ground = new WWBox(10.0f, 10.0f, 1.0f);
        ground.setPosition(0, 0, -0.5f);
        ground.solid = true;
        ground.physical = false;
        ground.friction = 0.9f;
        world.addObject(ground);

        WWCylinder cyl = new WWCylinder(1.0f, 0.5f, 2.0f);
        cyl.setPosition(0, 0, 0.99f);
        cyl.solid = true;
        cyl.physical = true;
        cyl.density = 0.25f;
        cyl.friction = 0.9f;
        // Set pure rolling angular velocity around X (perpendicular to Z normal)
        cyl.setAngularVelocity(new WWVector(100.0f, 0, 0));
        world.addObject(cyl);

        NewPhysicsThread thread = new NewPhysicsThread(world, 50, 2);
        thread.performIteration(50);

        WWVector angularVel = new WWVector();
        cyl.getAngularVelocity(angularVel);
        // Angular velocity around normal (Z) should remain 0, and rolling angular velocity (X) should experience rolling friction but not normal spin damping
        Assert.assertEquals(0.0f, angularVel.z, 1e-3f);
        Assert.assertTrue(angularVel.x > 0.0f && angularVel.x <= 100.0f);
    }
}
