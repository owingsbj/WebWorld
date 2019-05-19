// Block.world -- a world where an avatar can stack blocks

// Create the ground
ground = new Mesh(); // for the ground
ground.name = "ground";
ground.setImpactSound("grass");
// ground.setSlidingSound("movingGrass");
ground.colorTop = 0x00E000; // top green, like grass
ground.color = 0x404000; // all others sides brown
ground.size = [1000, 1000, 250];
ground.position = [0, 0, -10];
meshSize = 100;
ground.meshSize = [meshSize, meshSize];
ground.setTextureURL(SIDE_TOP, "grass");
ground.setTextureScaleX(SIDE_TOP, 0.001);
ground.setTextureScaleY(SIDE_TOP, 0.001);
// - add a few peaks
for (i = 0; i < 10; i++) {
	x = Math.trunc(meshSize * Math.random());
	y = Math.trunc(meshSize * Math.random());
	z = Math.random() * 0.001;
	baseSize = Math.trunc(Math.random() * 20) + 5;
	for (cx = x - baseSize; cx < x + baseSize; cx++) {
		for (cy = y - baseSize; cy < y + baseSize; cy++) {
			if (cx >= 0 && cx <= meshSize && cy >= 0 && cy <= meshSize) {
				d = Math.sqrt((cx - x) * (cx - x) + (cy - y) * (cy - y));
				if (d < baseSize) {
					ground.setMeshPoint(cx, cy, ground.getMeshPoint(cx, cy) + (baseSize - d) * (baseSize - d) * z);
				}
			}
		}
	}
}
// - add a few gorges
for (i = 0; i < 10; i++) {
	x = Math.trunc(meshSize * Math.random());
	y = Math.trunc(meshSize * Math.random());
	z = Math.random() * 0.001;
	baseSize = Math.trunc(Math.random() * 20) + 10;
	for (cx = x - baseSize; cx < x + baseSize; cx++) {
		for (cy = y - baseSize; cy < y + baseSize; cy++) {
			if (cx >= 0 && cx <= meshSize && cy >= 0 && cy <= meshSize) {
				d = Math.sqrt((cx - x) * (cx - x) + (cy - y) * (cy - y));
				if (d < baseSize) {
					ground.setMeshPoint(cx, cy, ground.getMeshPoint(cx, cy) - (baseSize - d) * (baseSize - d) * z);
				}
			}
		}
	}
}
world.addObject(ground);

// Create water
water = new Translucency();
water.name = "water";
water.penetratable = true;
water.insideLayerDensity = 0.25;
water.position =  [0, 0, -25 - 25 * Math.random()];
water.size = [1000, 1000, 50];
water.solid = false;
water.density = 1;
water.friction = 0.1;
water.impactSound = "water";
water.slidingSound = "movingWater";
water.setInsideColor(0x202040);
water.setInsideTransparency(0.7);
water.color = 0x8080F0;
water.colorTop = 0xA0A0F0;
water.setTransparency(SIDE_TOP, 0.1);
water.setTextureURL(SIDE_TOP, "water");
water.setTextureScaleX(SIDE_TOP, 0.001);
water.setTextureScaleY(SIDE_TOP, 0.001);
water.setTextureVelocityX(SIDE_TOP, 0.0001);
water.setColor(SIDE_INSIDE_TOP, new Color(0xF0F0F0));
water.setTransparency(SIDE_INSIDE_TOP, 0.50);
water.setTextureURL(SIDE_INSIDE_TOP, "water");
water.setTextureScaleX(SIDE_INSIDE_TOP, 0.001);
water.setTextureScaleY(SIDE_INSIDE_TOP, 0.001);
water.setTextureVelocityX(SIDE_INSIDE_TOP, 0.0001);
water.setColor(SIDE_CUTOUT1, new Color(0x6060C0));
water.setTransparency(SIDE_CUTOUT1, 0.35);
water.setFullBright(SIDE_INSIDE1, true);
water.setFullBright(SIDE_INSIDE2, true);
water.setFullBright(SIDE_INSIDE3, true);
water.setFullBright(SIDE_INSIDE4, true);
world.addObject(water);

// Create the sky
sky = new Sphere();
sky.name = "sky";
sky.penetratable = true;
sky.setTransparency(SIDE_ALL, 0.01); // keeps from generating shadows
sky.position = [0, 0, -800];
sky.size = [2000, 5000, 5000];
sky.cutoutStart = 0.5; // half dome
sky.solid = false; // for now.. otherwise physical objects pushed out of world
sky.friction = 0; // for now.. otherwise physical objects slowed down in hollowed area
sky.rotation = [0, 90, 0];  // orient to a dome
sky.hollow = 0.99;
sky.setTextureURL(SIDE_INSIDE1, "sky");
sky.setTextureScaleX(SIDE_INSIDE1, 0.25);
sky.setTextureScaleY(SIDE_INSIDE1, 0.25);
sky.setTextureVelocityY(SIDE_INSIDE1, 0.0005);
sky.setFullBright(SIDE_INSIDE1, true); // bright sky
sky.setColor(SIDE_INSIDE1, new Color(0xd0d0ff));
sky.setTextureURL(SIDE_SIDE1, "sky");
sky.setTransparency(SIDE_SIDE1, 0.25);
world.addObject(sky);

// Create air (to provide friction and balloon floating)
air = new Sphere();
air.name = "air";
air.penetratable = true;
air.position = [0, 0, -100];
air.size = [500, 5000, 5000];
air.transparency = 1.0;
air.cutoutStart = 0.5; // half dome
air.solid = false;
air.rotation = [0, 90, 0];  // to orient to a dome
air.friction = 0.1;
air.density = 0.0001;
world.addObject(air);

// Position the avatar
avatar.position = [0, 0, 25];
