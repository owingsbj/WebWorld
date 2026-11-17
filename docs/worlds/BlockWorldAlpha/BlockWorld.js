// Block.world -- a world where an avatar can stack blocks

// Set world properties (that are different from defaults)
world.status = "No blocks";
world.fogDensity = 0.1;
world.persistent = true;
world.playSong("es9pt2.ogg", 0.05);
//world.moveType = "confront";
world.renderingThreshold = 100;

// Preload sounds so there won't be a delay when first played
world.preloadSound("create.ogg");
world.preloadSound("destroy.ogg");

// Create the ground
var ground = new Mesh(); // for the ground
ground.penetratable = false;
ground.impactSound = "grass";
ground.colorTop = new Color(0x00E000); // top green, like grass
ground.color = new Color(0x404000); // all others sides brown
ground.size = new Vector(1000, 1000, 250);
ground.position = new Vector(0, 0, 0);
var meshSize = 250;
ground.meshSize = [meshSize, meshSize];
ground.textureTop = new Texture("moss", 0.005, 0.005);

// - add a few hills and peaks
var ruggedness = Math.random();
for (var i = 0; i < 1000 * ruggedness; i++) {
	var x = meshSize * Math.random();
	var y = meshSize * Math.random();
	var baseSize = 0.25 * meshSize * Math.random() + 5;
	if (Math.random() < ruggedness) {
		ground.createPeak(x, y, Math.random() * 0.02, baseSize);
	} else {
		ground.createHill(x, y, Math.random() * 0.01, baseSize);
	}
}

// - add a few ponds/lakes (reverse hills)
for (i = 0; i < 25; i++) {
	x = Math.trunc(meshSize * Math.random());
	y = Math.trunc(meshSize * Math.random());
	var depth = Math.random() * 0.1;
	baseSize = Math.trunc(Math.random() * meshSize / 5) + 10;
	ground.createHill(x, y, -depth, baseSize);
}

// - and level the edges to avoid showing sides of water
for (i = 0; i <= meshSize; i++) {
    ground.setMeshPoint(0, i, 0.5);
    ground.setMeshPoint(1, i, 0.5);
    ground.setMeshPoint(meshSize - 1, i, 0.5);
    ground.setMeshPoint(meshSize, i, 0.5);
    ground.setMeshPoint(i, 0, 0.5);
    ground.setMeshPoint(i, 1, 0.5);
    ground.setMeshPoint(i, meshSize - 1, 0.5);
    ground.setMeshPoint(i, meshSize, 0.5);
}

world.addObject(ground);

// Create the water
var waterTexture = new Texture("water.png", 0.002, 0.002);
waterTexture.velocityX = 0.00005;
var water = new Translucency();
water.name = "water";
water.shadowless = true;
water.penetratable = true;
water.insideLayerDensity = 0.25;
water.size = new Vector(1000, 1000, 100);
water.position =  new Vector(0, 0, -51);  // a little lower than base land
water.solid = false;
water.density = 1;
water.friction = 0.25;
water.impactSound = "splash";
water.slidingSound = "trickle";
water.translucencyMask = new Color(0xC0202040);
water.color = new Color(0x8080FF);
water.transparency= 0.1;
water.texture = waterTexture;
water.shininess = 0.1;
water.colorInsideTop = new Color(0xF0F0F0);
water.fullBrightInsideTop = true;
water.transparencyInsideTop = 0.25;
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
var water2texture = new Texture("water.png", 0.004 ,0.004, 45);
water2texture.velocityX = 0.00005001;
var water2 = new Box();
water2.shadowless = true;
water2.phantom = true;
water2.position = water.position;
water2.size = water.size;
water2.colorTop = new Color(0x8080FF);
water2.transparency = 1.0;
water2.transparencyTop = 0.75;
water2.textureTop = water2texture;
water2.shininessTop = 0.1;
world.addObject(water2);

// a third phantom water layer for even better looking water
var water3texture = new Texture("water.png", 0.008, 0.008, 33);
water3texture.velocityY = 0.00001;
var water3 = water2.clone();
water3.colorTop = new Color(0x80FF80);
water3.textureTop = water3texture;
water3.transparencyTop = 0.75;
water3.shininessTop = 0.1;
world.addObject(water3);

// Create the sky
var skyTexture = new Texture("sky.png", 0.25, 0.25);
skyTexture.velocityY = 0.0005;
var sky = new Sphere();
sky.phantom = true;
sky.shadowless = true;
sky.position = [0, 0, -500];
sky.size = [1500, 3000, 3000];
sky.cutoutStart = 0.5; // half dome
sky.rotation = [0, -90, 0];  // orient to a dome
sky.hollow = 0.99;
sky.textureInside1 = skyTexture;
sky.fullBrightInside1 = true;  // bright sky
sky.colorInside1 = new Color(0xd0d0ff);
sky.textureSide1 = new Texture("sky.png");
sky.transparencySide1 = 0.25;
world.addObject(sky);

// Create air (to provide friction when falling and balloon floating)
var air = new Cylinder();
air.penetratable = true;
air.position = new Vector(0, 0, 1000+water.position.z+water.size.z/2);  // need air to connect at water level
air.size = new Vector(1000, 1000, 2000);
air.transparency = 1.0;
air.solid = false;
air.friction = 0.1;
air.slidingSound = "wind";
air.density = 0.0002;
world.addObject(air);


// plant some patches of grass
var windBendBehavior = new WindBendBehavior();
for (var i = 0; i < 000; i++) {
    var patchx = Math.random() * ground.size.x - ground.size.x / 2;
    var patchy = Math.random() * ground.size.y - ground.size.y / 2;
//    var box = new Box();
//    box.position = new Vector(patchx, patchy, ground.getMeshValue(new Vector(patchx, patchy, 0)) + 1);
//    box.phantom = true;
//    world.addObject(box);
    var patchSize = 10;
    for (var j = 0; j < patchSize; j++) {
        var plantx = patchx + Math.random() * 10;
        var planty = patchy + Math.random() * 10;
        var groundHeight = ground.getMeshValue(new Vector(plantx, planty, 0));
        var grassSize = 2;
        var grassPlant = new Plant();
        grassPlant.type = "grass";
        grassPlant.size = new Vector(grassSize, grassSize, 1)
        //grassPlant.rotation.yaw(Math.random() * 180);
       grassPlant.texture = new Texture("blades.png");
        grassPlant.position = new Vector(plantx, planty, groundHeight + grassPlant.size.z / 4);
        //grassPlant.addBehavior(windBendBehavior);
        world.addObject(grassPlant);
    }
}


/**
 * Make the avatar jump.
 */
function jump() {
	var velocity = avatar.velocity;
	velocity.z = 5;
	avatar.velocity = velocity;
}

/**
 * Make the avatar build a hill.
 */
function pile() {
	var cell = ground.getMeshCell(avatar.getPosition());
	var i = Math.round(cell.x);
	var j = Math.round(cell.y);
	var avgHeight = (ground.getMeshPoint(i, j) +ground.getMeshPoint(i+1, j) + ground.getMeshPoint(i, j+1) + ground.getMeshPoint(i+1, j+1)) / 4; 
	var k = Math.round(250 * avgHeight) / 250 + 0.004;
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
	var cell = ground.getMeshCell(avatar.getPosition());
	var i = Math.round(cell.x);
	var j = Math.round(cell.y);
	var avgHeight = (ground.getMeshPoint(i, j) +ground.getMeshPoint(i+1, j) + ground.getMeshPoint(i, j+1) + ground.getMeshPoint(i+1, j+1)) / 4; 
	var k = Math.round(250 * avgHeight) / 250 - 0.004;
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

var blocks = [];

function jumpBlock() {
    this.velocity.z += 10;
}

function rollBlock() {
	this.setAngularVelocity(this.angularVelocity.clone().add(0, 250, 0));
}

function spinBlock() {
	this.setAngularVelocity(this.angularVelocity.clone().add(0, 0, 250));
}

function physicBlock() {
	this.physical = true;
}

function stopBlock() {
    this.physical = false;
	this.setTorque(this.torque.zero());
	this.setAngularVelocity(this.angularVelocity.zero());
	this.setThrust(this.thrust.zero());
	this.setVelocity(this.velocity.zero());
}

function deleteBlock() {
	world.removeObject(this);
	blocks.splice(blocks.indexOf(this), 1);
	world.status = "" + blocks.length + " blocks";
}

/**
 * Create a block in front of the avatar. This will also remove the block if it
 * already exists at the location. The type of block is chosen by a dialog.
 */
function createBlockAtAvatar() {
	var position = getAvailableBlockPosition(getPositionAheadOfAvatar());
	position.add(0, -4, 0);
	createBlock(position);
}

function createBlockAbove() {
	var position = this.position;
	position.add(0, 0, 1);
	createBlock(position);
}

function createBlock(position) {	
	var blockType = select("Select a block type", 
			["brick", "boulder", "firepit", "fountain", "tree"],
			["brick.jpg", "boulder.png", "fire.png", "drop.png", "tree.png"]);
	var block;
	if (blockType == 0 ) {
		block = new Box();
		block.roundedTop = true;
		block.roundedBottom = true;
		block.roundedSides = true;
		block.penetratable = false;
		block.impactSound = "concrete";
		block.texture = new Texture("brick.jpg", 1, 1);
		block.shininess = 0.1;  // so the bump map shows
		block.size = new Vector(1.9999, 1.9999, 2);
		block.position = position;
		world.addObject(block);
		block.addBehavior(new SquishyBehavior());
	} else if (blockType == 1 ) {
		block = new Sphere();
		block.penetratable = false;
		block.impactSound = "concrete";
		block.texture = new Texture("concrete", 1, 1);
		block.shininess = 0.1;  // so the bump map shows
		block.size = new Vector(1.9999, 1.9999, 2);
		block.position = position;
		world.addObject(block);
		block.addBehavior(new SquishyBehavior());
	} else if (blockType == 2) {
		block = new Box();
		block.penetratable = false;
		block.impactSound = "concrete";
		block.texture = new Texture("brick.jpg", 1, 2);
		block.textureTop = new Texture("brick.jpg", 0.25, 4);
		block.shininess = 0.1;  // so the bump map shows
		block.shininessTop = 0.1;  // so the bump map shows
		block.size = new Vector(1.9999, 1.9999, 1);
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
	} else if (blockType == 3) {
		block = new Box();
		block.penetratable = false;
		block.impactSound = "concrete";
		block.texture = new Texture("brick.jpg", 1, 2);
		block.textureTop = new Texture("brick.jpg", 0.25, 4);
		block.shininess = 0.1;  // so the bump map shows
		block.shininessTop = 0.1;  // so the bump map shows
		block.size = new Vector(1.9999, 1.9999, 1);
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
	} else if (blockType == 4) {
		block = new Box();
		block.penetratable = false;
		block.impactSound = "concrete";
		block.texture = new Texture("brick.jpg", 1, 2);
		block.textureTop = new Texture("brick.jpg", 0.25, 4);
		block.shininess = 0.1;  // so the bump map shows
		block.shininessTop = 0.1;  // so the bump map shows
		block.size = new Vector(1.9999, 1.9999, 1);
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
		plant.texture = new Texture("tree.png");
		plant.addBehavior(new WindBendBehavior());
		block.addChild(plant);
		world.addObject(block);
	}
	block.pickable = true;
	block.actions = [
	    new Action("jump", jumpBlock),
		new Action("roll", rollBlock),
		new Action("spin", spinBlock),
		new Action("physics", physicBlock),
		new Action("stop", stopBlock),
		new Action("delete", deleteBlock)
	];
    block.density = 10.0;
//    block.physical = true;
//	block.onTimer = function() {    // turns the object non-physical after 5 seconds
//		this.physical = false;
//		this.velocity = new Vector(0, 0, 0);
//		this.angularVelocity = new Vector(0, 0, 0);
//	};
	block.timer = 5000;
	blocks.push(block);
	world.playSound("create.ogg", 1);
	world.status = "" + blocks.length + " blocks";
}

/**
 * Figure position to place a block either at or above the given position.
 */
function getAvailableBlockPosition(position) {
	do {
		var blockAtPosition = false;
		for (var i = 0; i < blocks.length; i++) {
			var block = blocks[i];
			if (block == null) {
				return position;
			}
			if (block.getPosition().clone().subtract(position).length() < 1) {
				blockAtPosition = true;
				position.z = position.z + 1;
			}
		}
	} while (blockAtPosition);
	return position;
}

/**
 * Figure out a good location to place a block.
 */
function getPositionAheadOfAvatar() {
	var x = Math.round(avatar.position.x / 2) * 2;
	var y = Math.round(avatar.position.y / 2) * 2;
    var z = Math.round(avatar.position.z / 2 - 0.5) * 2 + 1.25;

	// Check the block directly ahead of the avatar
	if (-Math.sin(Math.PI / 180.0 * avatar.rotation.yaw) > 0.7) {
		x += 2;
	} else if (-Math.sin(Math.PI / 180.0 * avatar.rotation.yaw) < -0.7) {
		x -= 2;
	}
	if (-Math.cos(Math.PI / 180.0 * avatar.rotation.yaw) > 0.7) {
		y += 2;
	} else if (-Math.cos(Math.PI / 180.0 * avatar.rotation.yaw) < -0.7) {
		y -= 2;
	}

	return new Vector(x, y, z);
}

function teleport() {
    var px = (Math.random() - 0.5) * ground.size.x * 0.9;
    var py = (Math.random()  - 0.5) * ground.size.y * 0.9;
    avatar.position = new Vector(px, py, Math.max(1, ground.getMeshValue(new Vector(px, py, 0))));
}

// setup actions for the avatar
avatar.actions = [
	new Action("jump", jump),
	new Action("pile", pile),
	new Action("dig", dig),
	new Action("sink", sink),
	new Action("float", float),
	new Action("block", createBlockAtAvatar),
	new Action("teleport", teleport)
];

// Position the avatar
var px = Math.random() * ground.size.x - ground.size.x / 2;
var py = Math.random() * ground.size.y - ground.size.y / 2;
avatar.position = new Vector(px, py, 25);
