
// Have some ground for egg avatar to stand on
ground = new Box(); // for the ground
ground.penetratable = false;
ground.impactSound = "grass";
ground.colorTop = new Color(0x00E000); // top green, like grass
ground.color = new Color(0x404000); // all others sides brown
ground.size = [1000, 1000, 250];
ground.position = [0, 0, -126];
ground.textureTop = new Texture("moss", 0.005, 0.005);
world.addObject(ground);

// Action to change the skin of the avatar
function changeSkin() {
	skin = avatarCustomization.getCustomProperty("skin", "happy");
	skin = select("Select a skin", 
			["happy",  "jack",  "jill", "casper", "bugs", "cupid", "mongo",  "patch",  "santa", "robot"],
			["happy_skin.png", "jack_skin.png", "jill_skin.png", "casper_skin.png", "bugs_skin.png", "cupid_skin.png", "mongo_skin.png", "patch_skin.jpg", "santa_skin.jpg", "robot_skin.png"]);
	if (skin != null) {
		avatarCustomization.setCustomProperty("skin", skin);
		avatar.texture = new Texture(skin + "_skin.png");
	}
}

avatar.actions = [
	new Action("Skin", changeSkin)
];
