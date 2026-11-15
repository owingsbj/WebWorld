// A block avatar, somewhat mindcrafty. 

avatar = new Sphere(); // the avatar will be in this sphere
avatar.name = "Blockhead";
avatar.size = new Vector(1, 0.5, 2);
avatar.transparency = 0.99; // the avatar is clear. The pieces of the avatar are inside.
//  Note: the avatar is not completely transparent so that it can be picked
avatar.elasticity = -0.5;
//Make the avatar lighter than water but heavier than air, so it floats well to swim.
avatar.density = 0.25;
avatar.addBehavior(new HumanMotionBehavior());

buildAvatarBody = function() {
    rounded = avatarCustomization.getCustomProperty("rounded", false) == true;

    // build the avatar
    body = new Box();
    body.name = "torso";
    body.size = new Vector(0.5, 0.25, 0.75);
    body.gluedToParent = true;
    avatar.addChild(body);
    body.position = new Vector(0, 0, 0.125);

    head = new Box();
    head.name = "head";
    head.size = new Vector(0.5, 0.5, 0.5);
    body.addChild(head);
    head.position = new Vector(0, 0, 0.625);
    head.rotationPoint = new Vector(0, 0, -0.25);

    focus = new Box();
    focus.name = "focus";
    focus.transparency = 1.0;
    focus.position = new Vector(0, -0.25, 0.125);
    head.addChild(focus);

    leftArm = new Box();
    leftArm.name = "left arm";
    leftArm.size = new Vector(0.185, 0.25, 0.75);
    body.addChild(leftArm);
    leftArm.position = new Vector(-0.3425, 0, 0.0);
    leftArm.rotationPoint = new Vector(0, 0, 0.25);

    rightArm = new Box();
    rightArm.name = "right arm";
    rightArm.size = new Vector(0.185, 0.25, 0.75);
    body.addChild(rightArm);
    rightArm.position = new Vector(0.3425, 0, 0.0);
    rightArm.rotationPoint = new Vector(0, 0, 0.25);

    leftLeg = new Box();
    leftLeg.name = "left leg";
    leftLeg.size = new Vector(0.25, 0.25, 0.75);
    body.addChild(leftLeg);
    leftLeg.position = new Vector(0.125, 0, -0.75);
    leftLeg.rotationPoint = new Vector(0, 0, 0.375);

    rightLeg = new Box();
    rightLeg.name = "right leg";
    rightLeg.size = new Vector(0.25, 0.25, 0.75);
    body.addChild(rightLeg);
    rightLeg.position = new Vector(-0.125, 0, -0.75);
    rightLeg.rotationPoint = new Vector(0, 0, 0.375);

    if (rounded) {
        body.roundedSides = true;
        head.roundedSides = true;
        head.roundedBottom = true;
        head.roundedTop = true;
        leftArm.roundedSides = true;
        leftArm.roundedBottom = true;
        rightArm.roundedSides = true;
        rightArm.roundedBottom = true;
        leftLeg.roundedSides = true;
        rightLeg.roundedSides = true;
    }

}

reskin = function() {
    skin = avatarCustomization.getCustomProperty("skin", "steve_skin.png");
    slim = avatarCustomization.getCustomProperty("slim", true) == true;

    var skinTexture = new Texture(skin);
    skinTexture.pixelated = true;

    body.textureSide1 = skinTexture.subMap(64, 64, 8, 12, 20, 20);
    body.textureSide2 = skinTexture.subMap(64, 64, 4, 12, 16, 20);
    body.textureSide3 = skinTexture.subMap(64, 64, 8, 12, 32, 20);
    body.textureSide4 = skinTexture.subMap(64, 64, 4, 12, 28, 20);
    body.textureTop = skinTexture.subMap(64, 64, 8, 4, 20, 16);
    body.textureBottom = skinTexture.subMap(64, 64, 8, 4, 28, 16, 90);

    head.textureSide1 = skinTexture.subMap(64, 64, 8, 8, 8, 8);
    head.textureSide2 = skinTexture.subMap(64, 64, 8, 8, 0, 8);
    head.textureSide3 = skinTexture.subMap(64, 64, 8, 8, 24, 8);
    head.textureSide4 = skinTexture.subMap(64, 64, 8, 8, 16, 8);
    head.textureTop = skinTexture.subMap(64, 64, 8, 8, 8, 0);
    head.textureBottom = skinTexture.subMap(64, 64, 8, 8, 16, 0, 90);

    if (slim == true) {
        leftArm.size = new Vector(0.185, 0.25, 0.75);
        leftArm.position = new Vector(-0.3425, 0, 0.0);
        leftArm.textureSide1 = skinTexture.subMap(64, 64, 3, 12, 36, 52);
        leftArm.textureSide2 = skinTexture.subMap(64, 64, 4, 12, 32, 52);
        leftArm.textureSide3 = skinTexture.subMap(64, 64, 3, 12, 43, 52);
        leftArm.textureSide4 = skinTexture.subMap(64, 64, 4, 12, 39, 52);
        leftArm.textureTop = skinTexture.subMap(64, 64, 3, 4, 36, 48);
        leftArm.textureBottom = skinTexture.subMap(64, 64, 3, 4, 39, 48, 90);

        rightArm.size = new Vector(0.185, 0.25, 0.75);
        rightArm.position = new Vector(0.3425, 0, 0.0);
        rightArm.textureSide1 = skinTexture.subMap(64, 64, 3, 12, 44, 20);
        rightArm.textureSide2 = skinTexture.subMap(64, 64, 4, 12, 40, 20);
        rightArm.textureSide3 = skinTexture.subMap(64, 64, 3, 12, 51, 20);
        rightArm.textureSide4 = skinTexture.subMap(64, 64, 4, 12, 47, 20);
        rightArm.textureTop = skinTexture.subMap(64, 64, 3, 4, 44, 16);
        rightArm.textureBottom = skinTexture.subMap(64, 64, 3, 4, 47, 16, 90);
    } else {
        leftArm.size = new Vector(0.25, 0.25, 0.75);
        leftArm.position = new Vector(-0.375, 0, 0.0);
        leftArm.textureSide1 = skinTexture.subMap(64, 64, 4, 12, 36, 52);
        leftArm.textureSide2 = skinTexture.subMap(64, 64, 4, 12, 32, 52);
        leftArm.textureSide3 = skinTexture.subMap(64, 64, 4, 12, 44, 52);
        leftArm.textureSide4 = skinTexture.subMap(64, 64, 4, 12, 40, 52);
        leftArm.textureTop = skinTexture.subMap(64, 64, 4, 4, 36, 48);
        leftArm.textureBottom = skinTexture.subMap(64, 64, 4, 4, 40, 48, 90);

        rightArm.size = new Vector(0.25, 0.25, 0.75);
        rightArm.position = new Vector(0.375, 0, 0.0);
        rightArm.textureSide1 = skinTexture.subMap(64, 64, 4, 12, 44, 20);
        rightArm.textureSide2 = skinTexture.subMap(64, 64, 4, 12, 40, 20);
        rightArm.textureSide3 = skinTexture.subMap(64, 64, 4, 12, 52, 20);
        rightArm.textureSide4 = skinTexture.subMap(64, 64, 4, 12, 48, 20);
        rightArm.textureTop = skinTexture.subMap(64, 64, 4, 4, 44, 16);
        rightArm.textureBottom = skinTexture.subMap(64, 64, 4, 4, 48, 16, 90);
    }

    leftLeg.textureSide1 = skinTexture.subMap(64, 64, 4, 12, 4, 20);
    leftLeg.textureSide2 = skinTexture.subMap(64, 64, 4, 12, 0, 20);
    leftLeg.textureSide3 = skinTexture.subMap(64, 64, 4, 12, 12, 20);
    leftLeg.textureSide4 = skinTexture.subMap(64, 64, 4, 12, 8, 20);
    leftLeg.textureTop = skinTexture.subMap(64, 64, 4, 4, 4, 4);
    leftLeg.textureBottom = skinTexture.subMap(64, 64, 4, 4, 8, 4, 90);

    rightLeg.textureSide1 = skinTexture.subMap(64, 64, 4, 12, 20, 52);
    rightLeg.textureSide2 = skinTexture.subMap(64, 64, 4, 12, 16, 52);
    rightLeg.textureSide3 = skinTexture.subMap(64, 64, 4, 12, 28, 52);
    rightLeg.textureSide4 = skinTexture.subMap(64, 64, 4, 12, 24, 52);
    rightLeg.textureTop = skinTexture.subMap(64, 64, 4, 4, 20, 48);
    rightLeg.textureBottom = skinTexture.subMap(64, 64, 4, 4, 24, 48, 90);

	console.log("Skin set to " + skin + " which is " + ((slim == false) ? "not slim." : "slim."));
}

buildAvatarBody();
reskin();
