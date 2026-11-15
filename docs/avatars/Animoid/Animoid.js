// A animal-like avatar, not perfect, more robot-like.

skin = new Texture("fur.astc");
skin.scaleX = 0.1;
skin.scaleY = 0.1;
skin.color = new Color(0x808040);
skin.shininess = 1.0

avatar = new Cylinder(); // the avatar will be in this
avatar.name = "Animoid";
avatar.size = new Vector(0.5, 0.5, 1);
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
torso.rotation = [-90, 0, 0];
torso.texture = skin;

neck = new Cylinder();
neck.name = "neck";
neck.size = new Vector(0.12, 0.12, 0.2);
torso.addChild(neck);
neck.position = new Vector(0, 0.01, 0.38);
neck.rotation = [45, 0, 0];
neck.texture = skin;

head = new Cylinder();
head.name = "head";
head.size = new Vector(0.15, 0.20, 0.15);
head.rotationPoint = new Vector(0, 0, -0.075);
neck.addChild(head);
head.position = new Vector(0, 0, 0.115);
head.rotation = [45, 0, 0];
head.texture = skin;

headDome = new Sphere();
headDome.size = new Vector(0.15, 0.20, 0.1);
headDome.position = new Vector(0, 0, 0.07);
headDome.texture = skin;
head.addChild(headDome);

nose = new Cylinder();
nose.name = "nose";
nose.taper = [1, 1];
nose.size = new Vector(0.05, 0.1, 0.1);
nose.position = new Vector(0, -0.08, 0.02);
nose.texture = skin;
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
chest.textureSide1 = skin;

leftShoulder = new Sphere();
leftShoulder.name = "left shoulder";
leftShoulder.size = new Vector(0.2, 0.2, 0.3);
chest.addChild(leftShoulder);
leftShoulder.position = new Vector(-0.22, 0, 0);
leftShoulder.rotation = new Quaternion(75, 0, 0);
leftShoulder.textureSide1 = skin;

leftArm = new Sphere();
leftArm.name = "left arm";
leftArm.size = new Vector(0.1, 0.125, 0.4);
leftArm.taper = [-0.5, -0.5];
leftShoulder.addChild(leftArm);
leftArm.position = new Vector(0, 0, -0.2);
leftArm.rotation = new Quaternion(0, -5, 0);
leftArm.rotationPoint = new Vector(0, 0, 0.18);
leftArm.textureSide1 = skin;

leftForearm = new Sphere();
leftForearm.name = "left forearm";
leftForearm.size = new Vector(0.1, 0.1, 0.5);
leftArm.addChild(leftForearm);
leftForearm.position = new Vector(0, 0, -0.3);
leftForearm.rotationPoint = new Vector(0, 0, 0.18);
leftForearm.rotation = [30, 0, 0];
leftForearm.textureSide1 = skin;

leftHand = new Sphere();
leftHand.name = "left hand";
leftHand.size = new Vector(0.1, 0.1, 0.25);
leftHand.rotationPoint = new Vector(0, 0, 0.1);
leftForearm.addChild(leftHand);
leftHand.position = new Vector(0, 0, -0.25);
leftHand.rotation = [30, 0, 0]
leftHand.textureSide1 = skin;

rightShoulder = new Sphere();
rightShoulder.name = "right shoulder";
rightShoulder.size = new Vector(0.2, 0.2, 0.3);
chest.addChild(rightShoulder);
rightShoulder.position = new Vector(0.22, 0, 0);
rightShoulder.rotation = new Quaternion(75, 0, 0);
rightShoulder.textureSide1 = skin;

rightArm = new Sphere();
rightArm.name = "right arm";
rightArm.size = new Vector(0.1, 0.125, 0.4);
rightArm.taper = [-0.5, -0.5];
rightShoulder.addChild(rightArm);
rightArm.position = new Vector(0, 0, -0.2);
rightArm.rotation = new Quaternion(0, 5, 0);
rightArm.rotationPoint = new Vector(0, 0, 0.18);
rightArm.textureSide1 = skin;

rightForearm = new Sphere();
rightForearm.name = "right forearm";
rightForearm.size = new Vector(0.1, 0.1, 0.5);
rightArm.addChild(rightForearm);
rightForearm.position = new Vector(0, 0, -0.3);
rightForearm.rotationPoint = new Vector(0, 0, 0.18);
rightForearm.rotation = [30, 0, 0];
rightForearm.textureSide1 = skin;

rightHand = new Sphere();
rightHand.name = "right hand";
rightHand.size = new Vector(0.1, 0.1, 0.25);
rightHand.rotationPoint = new Vector(0, 0, 0.1);
rightForearm.addChild(rightHand);
rightHand.position = new Vector(0, 0, -0.25);
rightHand.rotation = [30, 0, 0]
rightHand.textureSide1 = skin;

leftButtock = new Sphere();
leftButtock.name = "left buttock";
leftButtock.size = new Vector(0.25, 0.25, 0.36);
torso.addChild(leftButtock);
leftButtock.position = new Vector(0.11, 0, -0.35);
leftButtock.rotation = [120, 0, 0];
leftButtock.textureSide1 = skin;

leftLeg = new Sphere();
leftLeg.name = "left leg";
leftLeg.size = new Vector(0.25, 0.25, 0.4);
leftLeg.rotationPoint = new Vector(0, 0, 0.15);
leftButtock.addChild(leftLeg);
leftLeg.position = new Vector(0, 0, -0.2);
leftLeg.textureSide1 = skin;

leftCalf = new Sphere();
leftCalf.name = "left calf";
leftCalf.size = new Vector(0.2, 0.2, 0.5);
leftCalf.rotationPoint = new Vector(0, 0, 0.1875);
leftLeg.addChild(leftCalf);
leftCalf.position = new Vector(0, 0, -0.25);
leftCalf.rotation = [-60, 0, 0];
leftCalf.textureSide1 = skin;

leftFoot = new Sphere();
leftFoot.name = "left foot";
leftFoot.size = new Vector(0.15, 0.15, 0.5);
leftCalf.addChild(leftFoot);
leftFoot.rotationPoint = new Vector(0, 0, 0.2);
leftFoot.rotation = new Quaternion(75, 0, 0);
leftFoot.position = new Vector(0, 0.05, -0.35);
leftFoot.textureSide1 = skin;

rightButtock = new Sphere();
rightButtock.name = "right buttock";
rightButtock.size = new Vector(0.25, 0.25, 0.36);
torso.addChild(rightButtock);
rightButtock.position = new Vector(-0.11, 0, -0.35);
rightButtock.rotation = [120, 0, 0];
rightButtock.textureSide1 = skin;

rightLeg = new Sphere();
rightLeg.name = "right leg";
rightLeg.size = new Vector(0.25, 0.25, 0.4);
rightLeg.rotationPoint = new Vector(0, 0, 0.15);
rightButtock.addChild(rightLeg);
rightLeg.position = new Vector(0, 0, -0.2);
rightLeg.textureSide1 = skin;

rightCalf = new Sphere();
rightCalf.name = "right calf";
rightCalf.size = new Vector(0.2, 0.2, 0.5);
rightCalf.rotationPoint = new Vector(0, 0, 0.1875);
rightLeg.addChild(rightCalf);
rightCalf.position = new Vector(0, 0, -0.25);
rightCalf.rotation = [-60, 0, 0];
rightCalf.textureSide1 = skin;

rightFoot = new Sphere();
rightFoot.name = "right foot";
rightFoot.size = new Vector(0.15, 0.15, 0.5);
rightFoot.rotationPoint = new Vector(0, 0, 0.2);
rightFoot.position = new Vector(0, 0.05, -0.35);
rightFoot.rotation = new Quaternion(75, 0, 0);
rightCalf.addChild(rightFoot);
rightFoot.textureSide1 = skin;

// Give the avatar human behavior when moving
avatar.addBehavior(new HumanMotionBehavior());
