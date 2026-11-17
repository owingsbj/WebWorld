package com.gallantrealm.myworld.model.physics;

import com.gallantrealm.myworld.model.WWBehavior;
import com.gallantrealm.myworld.model.WWBox;
import com.gallantrealm.myworld.model.WWCylinder;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWQuaternion;
import com.gallantrealm.myworld.model.WWSphere;
import com.gallantrealm.myworld.model.WWVector;
import com.gallantrealm.myworld.model.WWWorld;

import org.junit.Assert;
import org.junit.Test;

public class NewPhysicsThreadTest {

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

    @Test
    public void testSphereRollsDownSlope() {
        WWWorld world = new WWWorld();

        WWBox slope = new WWBox(20.0f, 20.0f, 1.0f);
        slope.setPosition(0, 0, -0.5f);
        slope.setRotation(new WWQuaternion(0, 15.0f, 0));
        slope.solid = true;
        slope.physical = false;
        slope.friction = 0.9f;
        world.addObject(slope);

        WWSphere sphere = new WWSphere(2.0f, 2.0f, 2.0f);
        sphere.setPosition(0, 0, 1.0f);
        sphere.solid = true;
        sphere.physical = true;
        sphere.density = 0.25f;
        sphere.friction = 0.9f;
        world.addObject(sphere);

        NewPhysicsThread thread = new NewPhysicsThread(world, 50, 2);
        thread.performIteration(50);

        WWVector angularVel = new WWVector();
        sphere.getAngularVelocity(angularVel);
        Assert.assertTrue("Sphere on slope should gain rolling angular velocity", angularVel.length() > 0.0f);
    }

    @Test
    public void testBlockRestingOnSurfaceDoesNotBounceOrFlip() {
        WWWorld world = new WWWorld();

        WWBox ground = new WWBox(10.0f, 10.0f, 1.0f);
        ground.setPosition(0, 0, -0.5f);
        ground.solid = true;
        ground.physical = false;
        ground.friction = 0.9f;
        world.addObject(ground);

        WWBox block = new WWBox(1.0f, 1.0f, 1.0f);
        block.setPosition(0, 0, 0.48f); // Resting with slight initial overlap on floor
        block.setRotation(new WWQuaternion(1.0f, 2.0f, 0.0f));
        block.solid = true;
        block.physical = true;
        block.density = 0.25f;
        block.friction = 0.9f;
        world.addObject(block);

        NewPhysicsThread thread = new NewPhysicsThread(world, 50, 2);
        for (int i = 0; i < 20; i++) {
            thread.performIteration(50);
        }

        WWVector angularVel = new WWVector();
        block.getAngularVelocity(angularVel);
        System.out.println("Resting block angular velocity: " + angularVel);
        System.out.println("Resting block velocity: " + block.getVelocity());
        System.out.println("Resting block position: " + block.getPosition());

        Assert.assertTrue("Resting block should not gain high angular velocity", angularVel.length() < 5.0f);
    }

    @Test
    public void testThrustSpherePinchedBetweenCylinderAndGround() {
        System.out.println("testThrustSpherePinchedBetweenCylinderAndGround");

        WWWorld world = new WWWorld();

        WWBox ground = new WWBox(26.0f, 26.0f, 10.0f);
        ground.setPosition(0, 0, -5.0f);
        ground.solid = true;
        ground.physical = false;
        ground.elasticity = 0.0f;
        ground.friction = 0.0f;
        world.addObject(ground);

        WWCylinder wall = new WWCylinder(2.25f, 1.0f, 25.0f);
        wall.setPosition(0, 0, 0);
        wall.setRotation(new WWQuaternion(90.0f, 0, 0));
        wall.solid = true;
        wall.physical = false;
        wall.elasticity = 0.0f;
        world.addObject(wall);
        wall.addBehavior(new WWBehavior() {
            @Override
            public boolean collideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
                System.out.println("Collision with "+nearObject);
                return true;
            }

            @Override
            public boolean slideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
                System.out.println("Sliding with "+nearObject);
                return true;
            }
        });

        WWSphere avatar = new WWSphere(2.0f, 2.0f, 0.75f);
        avatar.setPosition(10.0f, 0.0f, 0.375f); // Resting on ground, outside wall in X
        avatar.solid = true;
        avatar.physical = true;
        avatar.density = 0.25f;
        avatar.elasticity = 0.75f;

        avatar.setThrust(new WWVector(-25.0f, 0.0f, 0));
        world.addObject(avatar);

        NewPhysicsThread thread = new NewPhysicsThread(world, 100, 2);

        for (int i = 0; i < 100; i++) {
            thread.updateDynamicProperties(100);
            thread.performIteration(100);
            System.out.println("Iter " + i + " pos: " + avatar.getPosition() + " vel: " + avatar.getVelocity());
        }

        WWVector pos = avatar.getPosition();
        Assert.assertTrue("Sphere should not penetrate or drill through wall", pos.x >= 0.5f);
    }

    @Test
    public void testTorqueDirection() {
        WWWorld world = new WWWorld();

        WWBox box = new WWBox(1.0f, 1.0f, 1.0f);
        box.setPosition(0, 0, 0);
        box.solid = true;
        box.physical = true;
        box.density = 1.0f;
        box.setTorque(new WWVector(0, 0, 10.0f));
        box.setTorqueVelocity(new WWVector(0, 0, 10.0f));
        world.addObject(box);

        NewPhysicsThread thread = new NewPhysicsThread(world, 100, 2);
        thread.performIteration(100);

        System.out.println("Angular velocity after physics iteration: " + box.getAngularVelocity());
        System.out.println("Initial rotation: pitch=" + box.getRotation().getPitch() + " roll=" + box.getRotation().getRoll() + " yaw=" + box.getRotation().getYaw());

        thread.updateDynamicProperties(100);

        System.out.println("Rotation after updateDynamicProperties: pitch=" + box.getRotation().getPitch() + " roll=" + box.getRotation().getRoll() + " yaw=" + box.getRotation().getYaw());

        WWVector forward = new WWVector(0, 1, 0); // North (+Y)
        box.getRotation().rotateVector(forward);
        Assert.assertTrue("Torque <0,0,10> should result in positive Z angular velocity", box.getAngularVelocity().z > 0);
        Assert.assertTrue("Positive Z angular velocity should turn object counter-clockwise (forward vector x < 0)", forward.x < 0);
    }
}
