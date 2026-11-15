
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

skins = {
    name: ["steve", "verne", "rick", "blue", "stick", "creep"],
    picture: ["steve.jpg", "verne.jpg", "rick.jpg", "blue.jpg", "stick.jpg", "creep.jpg"],
    slim: [true, false, true, true, false, false]
};

// Action to change the skin of the avatar
function changeSkin() {
	var s = select("Select a skin", skins.name, skins.picture);
	if (s != null) {
		avatarCustomization.setCustomProperty("skin", skins.name[s]+"_skin.png");
		avatarCustomization.setCustomProperty("slim", skins.slim[s]);
		reskin();
	}
}

function roundShapes() {
    rounded = !rounded;
	avatarCustomization.setCustomProperty("rounded", rounded);
	avatar.removeChild(body);
	buildAvatarBody();
	reskin();
}

avatar.actions = [
	new Action("Skin", changeSkin),
	new Action("Round", roundShapes)
];
