// Happy, the egg avatar

skin = avatarCustomization.getCustomProperty("skin", "happy");

avatar = new Sphere();
avatar.color = new Color(1, 1, 1);  //white
avatar.name = "Happy";
avatar.size = [1, 1, 1];
avatar.taper = [0.25, 0.25];
avatar.texture = new Texture(skin+"_skin.png");

//Make the egg quite a bit lighter than water but heavier than air, so it floats well in water.
avatar.density = 0.25;

avatar.addBehavior(new SquishyBehavior());

