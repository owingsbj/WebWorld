// BlockWorld -- a world where an avatar can stack blocks

// Set world properties (that are different from defaults)
world.status = "No blocks";
world.fogDensity = 0.1;
world.persistent = true;
world.playSong("es9pt2.ogg", 0.05);

// Preload sounds so there won't be a delay when first played
world.preloadSound("create.ogg");
world.preloadSound("destroy.ogg");

// Create the ground
ground = new Mesh(); // for the ground
ground.penetratable = false;
ground.impactSound = "grass";
ground.colorTop = new Color(0x00E000); // top green, like grass
ground.color = new Color(0x404000); // all others sides brown
ground.size = [1000, 1000, 250];
ground.position = [0, 0, 0];
meshSize = 100;
ground.meshSize = [meshSize, meshSize];
ground.textureTop = new Texture("moss", 0.005, 0.005);

// - add a few hills and peaks
ruggedness = Math.random();
for (i = 0; i < 150 * ruggedness; i++) {
	x = meshSize * Math.random();
	y = meshSize * Math.random();
	baseSize = meshSize * Math.random() * (1 - ruggedness) + 5;
	if (Math.random() < ruggedness) {
		ground.createPeak(x, y, Math.random() * 0.1, baseSize);
	} else {
		ground.createHill(x, y, Math.random() * 0.01, baseSize);
	}
}

// - add a few gorges (reverse hills)
for (i = 0; i < 10; i++) {
	x = Math.trunc(meshSize * Math.random());
	y = Math.trunc(meshSize * Math.random());
	depth = Math.random() * 0.1;
	baseSize = Math.trunc(Math.random() * 20) + 10;
	ground.createHill(x, y, -depth, baseSize);
}

world.addObject(ground);

// Create the water
water = new Translucency();
water.name = "water";
water.shadowless = true;
water.penetratable = true;
water.insideLayerDensity = 0.25;
water.size = [1000, 1000, 100];
water.position =  [0, 0, -60];  // a little lower than base land level
water.solid = false;
water.density = 1;
water.friction = 0.25;
water.impactSound = "splash";
water.slidingSound = "trickle";
water.translucencyMask = new Color(0xC0202040);
water.color = new Color(0x8080FF);
water.transparency= 0.1;
water.texture = new Texture("water.png", 0.002, 0.002);
water.shininess = 0.1;
water.setTextureVelocityX(SIDE_TOP, 0.00005);
water.colorInsideTop = new Color(0xF0F0F0);
water.fullBrightInsideTop = true;
water.transparencyInsideTop = 0.25;
water.textureInsideTop = new Texture("water.png", 0.002, 0.002);
water.setTextureVelocityX(SIDE_INSIDE_TOP, -0.00005);
water.colorInside1 = new Color(0x000040);  // the inside walls
water.fullBrightInside1 = true;
water.transparencyInside1 = 0.0;
water.colorInside2 = new Color(0x000040);
water.fullBrightInside2 = true;
water.transparencyInside2 = 0.0;
water.colorInside3 = new Color(0x000040);
water.fullBrightInside3 = true;
water.transparencyInside3 = 0.0;
water.colorInside4 = new Color(0x000040);
water.fullBrightInside4 = true;
water.transparencyInside4 = 0.0;
water.colorInsideBottom = new Color(0x000020);  // deep down
water.fullBrightInsideBottom = true;
water.transparencyInsideBottom = 0.7;  // still allow see through
water.colorCutout1 = new Color(0x6060C0);  // the mask
water.transparencyCutout1 = 0.35;
world.addObject(water);

// a second phantom water layer for better visual effect
water2 = new Box();
water2.shadowless = true;
water2.phantom = true;
water2.position = water.position;
water2.size = water.size;
water2.colorTop = new Color(0x8080FF);
water2.transparency = 1.0;
water2.transparencyTop = 0.75;
water2.textureTop = new Texture("water.png", 0.004, 0.004, 45);
water2.shininessTop = 0.1;
water2.setTextureVelocityX(SIDE_TOP, 0.00005001);
world.addObject(water2);

// a third phantom water layer for even better looking water
water3 = water2.clone();
water3.colorTop = new Color(0x80FF80);
water2.textureTop = new Texture("water.png", 0.008, 0.008, 30);
water3.setTextureVelocityY(SIDE_TOP, 0.00000001);
world.addObject(water3);

// Create the sky
sky = new Sphere();
sky.phantom = true;
sky.shadowless = true;
sky.position = [0, 0, -400];
sky.size = [1000, 2500, 2500];
sky.cutoutStart = 0.5; // half dome
sky.rotation = [0, -90.0, 0];  // orient to a dome
sky.hollow = 0.99;
sky.textureInside1 = new Texture("sky.png", 0.25, 0.25);
sky.setTextureVelocityY(SIDE_INSIDE1, 0.0005);
sky.fullBrightInside1 = true;  // bright sky
sky.colorInside1 = new Color(0xd0d0ff);
sky.textureSide1 = new Texture("sky.png");
sky.transparencySide1 = 0.25;
world.addObject(sky);

// Create air (to provide friction when falling and balloon floating)
air = new Cylinder();
air.penetratable = true;
air.position = [0, 0, 1000+water.position.z+water.size.z/2];  // need air to connect at water level
air.size = [1000, 1000, 2000];
air.transparency = 1.0;
air.solid = false;
air.friction = 0.1;
air.slidingSound = "wind";
air.density = 0.0002;
world.addObject(air);


/**
 * Make the avatar jump.
 */
function jump() {
	velocity = avatar.velocity;
	velocity.z = 5;
	avatar.velocity = velocity;
}

/**
 * Make the avatar build a hill.
 */
function pile() {
	cell = ground.getMeshCell(avatar.getPosition());
	i = Math.round(cell.x);
	j = Math.round(cell.y);
	avgHeight = (ground.getMeshPoint(i, j) +ground.getMeshPoint(i+1, j) + ground.getMeshPoint(i, j+1) + ground.getMeshPoint(i+1, j+1)) / 4; 
	k = Math.round(250 * avgHeight) / 250 + 0.004;
	ground.setMeshPoint(i, j, k);
	ground.setMeshPoint(i + 1, j, k);
	ground.setMeshPoint(i, j + 1, k);
	ground.setMeshPoint(i + 1, j + 1, k);
	ground.updateRendering();
}

/**
 * Make the avatar dig.
 */
function dig() {
	cell = ground.getMeshCell(avatar.getPosition());
	i = Math.round(cell.x);
	j = Math.round(cell.y);
	avgHeight = (ground.getMeshPoint(i, j) +ground.getMeshPoint(i+1, j) + ground.getMeshPoint(i, j+1) + ground.getMeshPoint(i+1, j+1)) / 4; 
	k = Math.round(250 * avgHeight) / 250 - 0.004;
	ground.setMeshPoint(i, j, k);
	ground.setMeshPoint(i + 1, j, k);
	ground.setMeshPoint(i, j + 1, k);
	ground.setMeshPoint(i + 1, j + 1, k);
	ground.updateRendering();
}

/**
 * Make the avatar sink
 */
function sink() {
	avatar.density = avatar.density * 2;
	console.info("Avatar density = "+avatar.density);
}

/**
 * Make the avatar float
 */
function float() {
	avatar.density = avatar.density / 2;
	console.info("Avatar density = "+avatar.density);
}

blocks = [];

function deleteBlock() {
	world.removeObject(this);
	blocks.splice(blocks.indexOf(this), 1);
	world.status = "" + blocks.length + " blocks";
}

/**
 * Create a block in front of the avatar. This will also remove the block if it
 * already exists at the location. The type of block is chosen by a dialog.
 */
function createBlock() {
	position = getPositionAheadOfAvatar();
	block = getBlockAtPosition(position);
	if (block != null) {
		world.removeObject(block);
		blocks.splice(blocks.indexOf(block), 1);
		world.playSound("destroy.ogg", 1);
	} else {
		blockType = select("Select a block type", 
				["brick", "firepit", "fountain", "plant"],
				["brick.jpg", "fire.png", "drop.png", "plant.png"]);
		if (blockType == "brick" ) {
			block = new Box();
			block.penetratable = false;
			block.impactSound = "concrete";
			block.texture = new Texture("brick.jpg", 1, 1);
			block.shininess = 0.1;  // so the bump map shows
			block.size = new Vector(2, 2, 2);
			block.position = position;
			block.pickable = true;
			block.angularVelocity = new Vector(0, 0, 1);
			block.actions = [new Action("delete", deleteBlock)];
			world.addObject(block);
		} else if (blockType == "firepit") {
			block = new Box();
			block.penetratable = false;
			block.impactSound = "concrete";
			block.texture = new Texture("brick.jpg", 1, 2);
			block.textureTop = new Texture("brick.jpg", 0.25, 4);
			block.shininess = 0.1;  // so the bump map shows
			block.shininessTop = 0.1;  // so the bump map shows
			block.size = new Vector(2, 2, 1);
			block.position = position.add(0, 0, -0.5);
			block.hollow = 0.5;
			block.pickable = true;
			block.actions = [new Action("delete", deleteBlock)];
			rocks = new Box();
			rocks.size = new Vector(1, 1, 0.6);
			rocks.texture = new Texture("gravel.jpg", 0.5, 0.5);
			block.addChild(rocks);
			fire = new ParticleEmitter();
			fire.position = new Vector(0, 0, 0.5);
			block.addChild(fire);
			fire.particleImage = "fire.png";
			fire.particleCount = 100;
			fire.particleRate = 100;
			fire.particleLifetime = 250;
			fire.particleSize = 0.25;
			fire.particleGrowthRate = -0.02;
			fire.particlePositionRandom = new Vector(0.25, 0.25, 0);
			fire.particleVelocity = new Vector(0, 0, 2);
			fire.particleVelocityRandom = new Vector(0.25, 0.25, 0.5);
			world.addObject(block);
			fire.startAnimation();
		} else if (blockType == "fountain") {
			block = new Box();
			block.penetratable = false;
			block.impactSound = "concrete";
			block.texture = new Texture("brick.jpg", 1, 2);
			block.textureTop = new Texture("brick.jpg", 0.25, 4);
			block.shininess = 0.1;  // so the bump map shows
			block.shininessTop = 0.1;  // so the bump map shows
			block.size = new Vector(2, 2, 1);
			block.position = position.add(0, 0, -0.5);
			block.hollow = 0.5;
			block.pickable = true;
			block.actions = [new Action("delete", deleteBlock)];
			rocks = new Box();
			rocks.size = new Vector(1, 1, 0.6);
			rocks.texture = new Texture("gravel.jpg", 0.5, 0.5);
			block.addChild(rocks);
			fountain = new ParticleEmitter();
			fountain.position = new Vector(0, 0, 0.25);
			block.addChild(fountain);
			fountain.particleImage = "drop.png";
			fountain.particleCount = 250;
			fountain.particleRate = 100;
			fountain.particleLifetime = 500;
			fountain.particleSize = 0.1;
			fountain.particleGrowthRate = -0.01;
			fountain.particleGravity = 0.5;
			// block.particlePositionRandom = new Vector(0.25, 0.25, 0);
			fountain.particleVelocity = new Vector(0, 0, 4);
			fountain.particleVelocityRandom = new Vector(1, 1, 1);
			world.addObject(block);
			fountain.startAnimation();
		} else if (blockType == "plant") {
			block = new Box();
			block.penetratable = false;
			block.impactSound = "concrete";
			block.texture = new Texture("brick.jpg", 1, 2);
			block.textureTop = new Texture("brick.jpg", 0.25, 4);
			block.shininess = 0.1;  // so the bump map shows
			block.shininessTop = 0.1;  // so the bump map shows
			block.size = new Vector(2, 2, 1);
			block.position = position.add(0, 0, -0.5);
			block.hollow = 0.5;
			block.pickable = true;
			block.actions = [new Action("delete", deleteBlock)];
			dirt = new Box();
			dirt.size = new Vector(1, 1, 0.6);
			dirt.texture = new Texture("dirt.jpg", 0.5, 0.5);
			block.addChild(dirt);
			plant = new Plant();
			plant.position = new Vector(0, 0, 1);
			plant.size = new Vector(2, 2, 2);
			plant.texture = new Texture("plant.png");
			block.addChild(plant);
			world.addObject(block);
		}
		blocks.push(block);
		world.playSound("create.ogg", 1);
	}
	world.status = "" + blocks.length + " blocks";
}

/**
 * Figure out the block block at position or return null if no block exists.
 */
function getBlockAtPosition(position) {
	atPosition = false;
	for (i = 0; i < blocks.length; i++) {
		block = blocks[i];
		if (block == null) {
			return null;
		}
		if (block.getPosition().subtract(position).length() < 1) {
			return block;
		}
	}
	return null;
}

/**
 * Figure out a good location to place a block.
 */
function getPositionAheadOfAvatar() {
	position = avatar.position;
	position.x = Math.round(position.x / 2) * 2;
	position.y = Math.round(position.y / 2) * 2;
	position.z = Math.round(position.z / 2 - 0.5) * 2 + 1.25;

	// Check the block directly ahead of the avatar
	x = 0;
	if (-Math.sin(Math.PI / 180.0 * avatar.rotation.yaw) > 0.7) {
		x = 2;
	} else if (-Math.sin(Math.PI / 180.0 * avatar.rotation.yaw) < -0.7) {
		x = -2;
	}
	y = 0;
	if (-Math.cos(Math.PI / 180.0 * avatar.rotation.yaw) > 0.7) {
		y = 2;
	} else if (-Math.cos(Math.PI / 180.0 * avatar.rotation.yaw) < -0.7) {
		y = -2;
	}
	position.x += x;
	position.y += y;
	
	return position;
}

// setup actions for the avatar
jumpAction = new Action("Jump", jump);
pileAction = new Action("Pile", pile);
digAction = new Action("Dig", dig);
sinkAction = new Action("Sink", sink);
floatAction = new Action("Float", float);
createBlockAction = new Action("Block", createBlock);
avatar.actions = [jumpAction, pileAction, digAction, sinkAction, floatAction, createBlockAction];

// Position the avatar
avatar.position = new Vector(0, 0, 5);
