package com.gallantrealm.webworld.model;

import com.gallantrealm.myworld.model.WWBox;
import com.gallantrealm.myworld.model.WWQuaternion;
import com.gallantrealm.myworld.model.WWVector;
import com.gallantrealm.myworld.model.WWWorld;
import org.junit.Assert;
import org.junit.Test;

public class AnimationBehaviorTest {

    private static final float EPSILON = 0.5f;

    @Test
    public void testTreadingArmRotations() {
        AnimationBehavior anim = new AnimationBehavior();
        anim.start("treading", 1.0f, 1.0f);

        WWBox leftArm = new WWBox();
        leftArm.setName("left arm");
        WWQuaternion leftRotation = new WWQuaternion();
        anim.getAnimatedRotation(leftArm, leftRotation, 1000L);
        // Left arm should tilt outward (positive roll)
        Assert.assertTrue("Left arm roll should be positive for outward tilt", leftRotation.getRoll() > 0);
        Assert.assertEquals(75.0f, leftRotation.getRoll(), EPSILON);

        WWBox rightArm = new WWBox();
        rightArm.setName("right arm");
        WWQuaternion rightRotation = new WWQuaternion();
        anim.getAnimatedRotation(rightArm, rightRotation, 1000L);
        // Right arm should tilt outward (negative roll)
        Assert.assertTrue("Right arm roll should be negative for outward tilt", rightRotation.getRoll() < 0);
        Assert.assertEquals(-75.0f, rightRotation.getRoll(), EPSILON);
    }

    @Test
    public void testFallingArmAndLegRotations() {
        AnimationBehavior anim = new AnimationBehavior();
        anim.start("falling", 1.0f, 1.0f);

        WWBox torso = new WWBox();
        torso.setName("torso");
        WWQuaternion torsoRotation = new WWQuaternion();
        anim.getAnimatedRotation(torso, torsoRotation, 1000L);
        Assert.assertTrue("Torso pitch should be positive for forward lean when falling", torsoRotation.getPitch() > 0);
        Assert.assertEquals(30.0f, torsoRotation.getPitch(), EPSILON);

        WWBox leftArm = new WWBox();
        leftArm.setName("left arm");
        WWQuaternion leftArmRotation = new WWQuaternion();
        anim.getAnimatedRotation(leftArm, leftArmRotation, 1000L);
        Assert.assertTrue("Left arm roll should be positive for outward tilt when falling", leftArmRotation.getRoll() > 0);
        Assert.assertEquals(75.0f, leftArmRotation.getRoll(), EPSILON);

        WWBox rightArm = new WWBox();
        rightArm.setName("right arm");
        WWQuaternion rightArmRotation = new WWQuaternion();
        anim.getAnimatedRotation(rightArm, rightArmRotation, 1000L);
        Assert.assertTrue("Right arm roll should be negative for outward tilt when falling", rightArmRotation.getRoll() < 0);
        Assert.assertEquals(-75.0f, rightArmRotation.getRoll(), EPSILON);

        WWBox leftLeg = new WWBox();
        leftLeg.setName("left leg");
        WWQuaternion leftLegRotation = new WWQuaternion();
        anim.getAnimatedRotation(leftLeg, leftLegRotation, 1000L);
        Assert.assertTrue("Left leg roll should be negative for outward tilt when falling", leftLegRotation.getRoll() < 0);
        Assert.assertEquals(-15.0f, leftLegRotation.getRoll(), EPSILON);

        WWBox rightLeg = new WWBox();
        rightLeg.setName("right leg");
        WWQuaternion rightLegRotation = new WWQuaternion();
        anim.getAnimatedRotation(rightLeg, rightLegRotation, 1000L);
        Assert.assertTrue("Right leg roll should be positive for outward tilt when falling", rightLegRotation.getRoll() > 0);
        Assert.assertEquals(15.0f, rightLegRotation.getRoll(), EPSILON);
    }

    @Test
    public void testSwimmingForwardRotations() {
        AnimationBehavior anim = new AnimationBehavior();
        // When swimming forward, speed and range are negative
        anim.start("swimming", -1.0f, -1.0f);

        WWBox torso = new WWBox();
        torso.setName("torso");
        WWQuaternion torsoRotation = new WWQuaternion();
        anim.getAnimatedRotation(torso, torsoRotation, 1000L);
        // Torso should pitch forward (positive pitch) when swimming forward
        Assert.assertTrue("Torso pitch should be positive for forward lean", torsoRotation.getPitch() > 0);
        Assert.assertEquals(75.0f, torsoRotation.getPitch(), EPSILON);

        WWWorld world = new WWWorld();
        WWBox neck = new WWBox();
        neck.setName("neck");
        world.addObject(neck);
        WWBox head = new WWBox();
        head.setName("head");
        neck.addChild(head);

        WWQuaternion headRotation = new WWQuaternion();
        anim.getAnimatedRotation(head, headRotation, 1000L);
        // Head should pitch up/back (negative pitch) to look toward the horizon
        Assert.assertTrue("Head pitch should be negative to look up toward horizon", headRotation.getPitch() < 0);
        Assert.assertEquals(-45.0f, headRotation.getPitch(), EPSILON);

        // Test arm rotation progression over time
        WWBox leftArm = new WWBox();
        leftArm.setName("left arm");
        WWVector dummyPos = new WWVector();
        WWQuaternion leftArmRotT1 = new WWQuaternion();
        WWQuaternion leftArmRotT2 = new WWQuaternion();

        anim.getAnimatedPosition(leftArm, dummyPos, 1000L);
        anim.getAnimatedRotation(leftArm, leftArmRotT1, 1000L); // at t1

        anim.getAnimatedPosition(leftArm, dummyPos, 1100L);
        anim.getAnimatedRotation(leftArm, leftArmRotT2, 1100L); // at t2 (later time)

        // Pitch angle should increase over time as time advances, propelling forward
        Assert.assertTrue("Arm pitch angle should increase over time when swimming forward", leftArmRotT2.getPitch() > leftArmRotT1.getPitch());
    }
}
