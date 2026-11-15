// A human-like avatar, not perfect, more robot-like. 

// Note: Avatar is positioned with its back to the camera.  Left is negative x, right positive x, front positive y, back negative y.
skin = "metal-skin.png";

avatar = new Cylinder(); // the avatar will be in this
avatar.name = "Humanoid";
avatar.size = new Vector(0.5, 0.5, 2.1);
avatar.transparency = 0.99; // the avatar object is (almost) clear. The pieces of the avatar are inside.
avatar.elasticity = -0.5;
//Make the avatar lighter than water but heavier than air, so it floats well to swim.
avatar.density = 0.5;


torso = new Sphere();
torso.name = "torso";
torso.size = new Vector(0.4, 0.3, 0.75);
torso.taper = [-0.2, 0];
avatar.addChild(torso);
torso.position = new Vector(0, 0, 0.35);
torso.textureSide1 = new Texture(skin);
torso.shininessSide1 = 0.5;

neck = new Cylinder();
neck.name = "neck";
neck.size = new Vector(0.12, 0.12, 0.2);
torso.addChild(neck);
neck.position = new Vector(0, 0.01, 0.38);
neck.textureSide1 = new Texture(skin);
neck.shininessSide1 = 0.5;

head = new Cylinder();
head.name = "head";
head.size = new Vector(0.15, 0.20, 0.15);
head.rotationPoint = new Vector(0, 0, -0.075);
neck.addChild(head);
head.position = new Vector(0, 0, 0.115);
head.texture = new Texture(skin);
head.shininess = 0.5;

headDome = new Sphere();
headDome.size = new Vector(0.15, 0.20, 0.1);
headDome.position = new Vector(0, 0, 0.07);
headDome.texture = new Texture(skin);
headDome.shininess = 0.5;
head.addChild(headDome);

nose = new Cylinder();
nose.name = "nose";
nose.taper = [1, 1];
nose.size = new Vector(0.05, 0.1, 0.1);
nose.position = new Vector(0, -0.08, 0.02);
nose.texture = new Texture(skin);
nose.shininess = 0.5;
head.addChild(nose);

leftEye = new Sphere();
leftEye.name = "left eye";
leftEye.size = new Vector(0.03, 0.02, 0.02);
leftEye.position = new Vector(-0.03, -0.08, 0.02);
leftEye.rotation = new Quaternion(-90, 0, 0);
leftEye.texture = new Texture("eye.png");
head.addChild(leftEye);

rightEye = new Sphere();
rightEye.name = "right eye";
rightEye.size = new Vector(0.03, 0.02, 0.02);
rightEye.position = new Vector(0.03, -0.08, 0.02);
rightEye.rotation = new Quaternion(-90, 0, 0);
rightEye.texture = new Texture("eye.png");
head.addChild(rightEye);

// The focus object indicates where to position the camera on first-person view
focus = new Sphere();
focus.name = "focus";
focus.transparency = 1.0;
focus.position = new Vector(0, -0.2, 0.1);
head.addChild(focus);

chest = new Sphere();
chest.name = "chest";
chest.size = new Vector(0.45, 0.3, 0.35);
chest.shear = [0, 0.1];
chest.taper = [-0.5, 0];
torso.addChild(chest);
chest.position = new Vector(0, -0.025, 0.2);
chest.textureSide1 = new Texture(skin);
chest.shininessSide1 = 0.5;

leftShoulder = new Sphere();
leftShoulder.name = "left shoulder";
leftShoulder.size = new Vector(0.2, 0.2, 0.3);
torso.addChild(leftShoulder);
leftShoulder.position = new Vector(-0.22, 0, 0.28);
leftShoulder.rotation = new Quaternion(0, 90, 0);
leftShoulder.textureSide1 = new Texture(skin);
leftShoulder.shininessSide1 = 0.5;

rightShoulder = new Sphere();
rightShoulder.name = "right shoulder";
rightShoulder.size = new Vector(0.2, 0.2, 0.3);
torso.addChild(rightShoulder);
rightShoulder.position = new Vector(0.22, 0, 0.28);
rightShoulder.rotation = new Quaternion(0, 90, 0);
rightShoulder.textureSide1 = new Texture(skin);
rightShoulder.shininessSide1 = 0.5;

leftArm = new Sphere();
leftArm.name = "left arm";
leftArm.size = new Vector(0.1, 0.125, 0.5);
leftArm.taper = [-0.5, -0.5];
torso.addChild(leftArm);
leftArm.position = new Vector(-0.28, 0, 0.1);
leftArm.rotation = new Quaternion(0, 5, 0);
leftArm.rotationPoint = new Vector(0, 0, 0.18);
leftArm.textureSide1 = new Texture(skin);
leftArm.shininessSide1 = 0.5;

leftForearm = new Sphere();
leftForearm.name = "left forearm";
leftForearm.size = new Vector(0.1, 0.1, 0.5);
leftArm.addChild(leftForearm);
leftForearm.position = new Vector(0, 0, -0.3);
leftForearm.rotationPoint = new Vector(0, 0, 0.18);
leftForearm.textureSide1 = new Texture(skin);
leftForearm.shininessSide1 = 0.5;

leftHand = new Sphere();
leftHand.name = "left hand";
leftHand.size = new Vector(0.05, 0.1, 0.15);
leftHand.rotationPoint = new Vector(0, 0, 0.07);
leftForearm.addChild(leftHand);
leftHand.position = new Vector(0, 0, -0.25);
leftHand.textureSide1 = new Texture(skin);
leftHand.shininessSide1 = 0.5;

rightArm = new Sphere();
rightArm.name = "right arm";
rightArm.size = new Vector(0.1, 0.125, 0.5);
rightArm.taper = [-0.5, -0.5];
torso.addChild(rightArm);
rightArm.position = new Vector(0.28, 0, 0.1);
rightArm.rotation = new Quaternion(0, -5, 0);
rightArm.rotationPoint = new Vector(0, 0, 0.18);
rightArm.textureSide1 = new Texture(skin);
rightArm.shininessSide1 = 0.5;

rightForearm = new Sphere();
rightForearm.name = "right forearm";
rightForearm.size = new Vector(0.1, 0.1, 0.5);
rightArm.addChild(rightForearm);
rightForearm.position = new Vector(0, 0, -0.3);
rightForearm.rotationPoint = new Vector(0, 0, 0.18);
rightForearm.textureSide1 = new Texture(skin);
rightForearm.shininessSide1 = 0.5;

rightHand = new Sphere();
rightHand.name = "right hand";
rightHand.size = new Vector(0.05, 0.1, 0.15);
rightHand.rotationPoint = new Vector(0, 0, 0.07);
rightForearm.addChild(rightHand);
rightHand.position = new Vector(0, 0, -0.25);
rightHand.textureSide1 = new Texture(skin);
rightHand.shininessSide1 = 0.5;

leftButtock = new Sphere();
leftButtock.name = "left buttock";
leftButtock.size = new Vector(0.25, 0.25, 0.35);
torso.addChild(leftButtock);
leftButtock.position = new Vector(-0.11, 0.04, -0.35);
leftButtock.textureSide1 = new Texture(skin);
leftButtock.shininessSide1 = 0.5;

rightButtock = new Sphere();
rightButtock.name = "right buttock";
rightButtock.size = new Vector(0.25, 0.25, 0.35);
torso.addChild(rightButtock);
rightButtock.position = new Vector(0.11, 0.04, -0.35);
rightButtock.textureSide1 = new Texture(skin);
rightButtock.shininessSide1 = 0.5;

leftLeg = new Sphere();
leftLeg.name = "left leg";
leftLeg.size = new Vector(0.25, 0.25, 0.7);
leftLeg.rotationPoint = new Vector(0, 0, 0.1875);
torso.addChild(leftLeg);
leftLeg.position = new Vector(-0.125, 0, -0.375-0.25);
leftLeg.textureSide1 = new Texture(skin);
leftLeg.shininessSide1 = 0.5;

leftCalf = new Sphere();
leftCalf.name = "left calf";
leftCalf.size = new Vector(0.2, 0.2, 0.6);
leftCalf.rotationPoint = new Vector(0, 0, 0.1875);
leftLeg.addChild(leftCalf);
leftCalf.position = new Vector(0, 0, -0.5);
leftCalf.textureSide1 = new Texture(skin);
leftCalf.shininessSide1 = 0.5;

leftFoot = new Sphere();
leftFoot.name = "left foot";
leftFoot.size = new Vector(0.15, 0.1, 0.3);
leftCalf.addChild(leftFoot);
leftFoot.rotationPoint = new Vector(0, 0, 0.1);
leftFoot.rotation = new Quaternion(-90, 0, 0);
leftFoot.position = new Vector(0, 0.05, -0.35);
leftFoot.textureSide1 = new Texture(skin);
leftFoot.shininessSide1 = 0.5;

rightLeg = new Sphere();
rightLeg.name = "right leg";
rightLeg.size = new Vector(0.25, 0.25, 0.7);
rightLeg.rotationPoint = new Vector(0, 0, 0.1875);
torso.addChild(rightLeg);
rightLeg.position = new Vector(0.125, 0, -0.375-0.25);
rightLeg.textureSide1 = new Texture(skin);
rightLeg.shininessSide1 = 0.5;

rightCalf = new Sphere();
rightCalf.name = "right calf";
rightCalf.size = new Vector(0.2, 0.2, 0.6);
rightCalf.rotationPoint = new Vector(0, 0, 0.1875);
rightLeg.addChild(rightCalf);
rightCalf.position = new Vector(0, 0, -0.5);
rightCalf.textureSide1 = new Texture(skin);
rightCalf.shininessSide1 = 0.5;

rightFoot = new Sphere();
rightFoot.name = "right foot";
rightFoot.size = new Vector(0.15, 0.1, 0.3);
rightFoot.rotationPoint = new Vector(0, 0, 0.1);
rightFoot.position = new Vector(0, 0.05, -0.35);
rightFoot.rotation = new Quaternion(-90, 0, 0);
rightCalf.addChild(rightFoot);
rightFoot.textureSide1 = new Texture(skin);
rightFoot.shininessSide1 = 0.5;

// Give the avatar human behavior when moving
avatar.addBehavior(new HumanMotionBehavior());
