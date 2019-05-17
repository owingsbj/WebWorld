// Block.world -- a world where an avatar can stack blocks

// Create the ground
ground = new WWMesh(); // for the ground
ground.setName("ground");
ground.setImpactSound("grass");
// ground.setSlidingSound("movingGrass");
ground.setColor(WWSimpleShape.SIDE_TOP, new WWColor(0x00E000)); // green, like grass
ground.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0x404000)); // all others brown
ground.setSize(new WWVector(1000, 1000, 250));
ground.setPosition(new WWVector(0, 0, -10));
meshSize = 100;
ground.setMeshSize(meshSize, meshSize);
ground.setTextureURL(WWSimpleShape.SIDE_TOP, "grass");
ground.setTextureScaleX(WWSimpleShape.SIDE_TOP, 0.001);
ground.setTextureScaleY(WWSimpleShape.SIDE_TOP, 0.001);
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
water = new WWTranslucency();
water.setName("water");
water.setPenetratable(true);
water.setInsideLayerDensity(0.25);
water.setPosition(new WWVector(0, 0, -25 - 25 * Math.random()));
water.setSize(new WWVector(1000, 1000, 50));
water.setSolid(false);
water.setDensity(1);
water.setFriction(0.1);
water.setImpactSound("water");
water.setSlidingSound("movingWater");
water.setInsideColor(0x202040);
water.setInsideTransparency(0.7);
water.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0x8080F0));
water.setColor(WWSimpleShape.SIDE_TOP, new WWColor(0xA0A0F0));
water.setTransparency(WWSimpleShape.SIDE_TOP, 0.1);
water.setTextureURL(WWSimpleShape.SIDE_TOP, "water");
water.setTextureScaleX(WWSimpleShape.SIDE_TOP, 0.001);
water.setTextureScaleY(WWSimpleShape.SIDE_TOP, 0.001);
water.setTextureVelocityX(WWSimpleShape.SIDE_TOP, 0.0001);
water.setColor(WWSimpleShape.SIDE_INSIDE_TOP, new WWColor(0xF0F0F0));
water.setTransparency(WWSimpleShape.SIDE_INSIDE_TOP, 0.50);
water.setTextureURL(WWSimpleShape.SIDE_INSIDE_TOP, "water");
water.setTextureScaleX(WWSimpleShape.SIDE_INSIDE_TOP, 0.001);
water.setTextureScaleY(WWSimpleShape.SIDE_INSIDE_TOP, 0.001);
water.setTextureVelocityX(WWSimpleShape.SIDE_INSIDE_TOP, 0.0001);
water.setColor(WWSimpleShape.SIDE_CUTOUT1, new WWColor(0x6060C0));
water.setTransparency(WWSimpleShape.SIDE_CUTOUT1, 0.35);
water.setFullBright(WWSimpleShape.SIDE_INSIDE1, true);
water.setFullBright(WWSimpleShape.SIDE_INSIDE2, true);
water.setFullBright(WWSimpleShape.SIDE_INSIDE3, true);
water.setFullBright(WWSimpleShape.SIDE_INSIDE4, true);
world.addObject(water);

// Create the sky
WWSimpleShape
sky = new WWSphere();
sky.setName("sky");
sky.setPenetratable(true);
sky.setTransparency(WWSimpleShape.SIDE_ALL, 0.01); // keeps from generating shadows
sky.setPosition(new WWVector(0, 0, -800));
sky.setSize(new WWVector(2000, 5000, 5000));
sky.setCutoutStart(0.5); // half dome
sky.setSolid(false); // for now.. otherwise physical objects pushed out of world
sky.setFriction(0); // for now.. otherwise physical objects slowed down in hollowed area
sky.setRotation(new WWVector(0, 90, 0));
sky.setHollow(0.99);
sky.setTextureURL(WWSimpleShape.SIDE_INSIDE1, "sky");
sky.setTextureScaleX(WWSimpleShape.SIDE_INSIDE1, 0.25);
sky.setTextureScaleY(WWSimpleShape.SIDE_INSIDE1, 0.25);
// sky.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0xc0c0f0));
sky.setTextureVelocityY(WWSimpleShape.SIDE_INSIDE1, 0.0005);
// for future
// sky.setTextureURL(Side.INSIDE1, "http://www.moonglow.net/latest");
// sky.setTextureRefreshInterval(Side.INSIDE1,"60000");
// TODO fog color should match average color of sky
sky.setFullBright(WWSimpleShape.SIDE_INSIDE1, true); // bright sky
sky.setColor(WWSimpleShape.SIDE_INSIDE1, new WWColor(0xd0d0ff));
sky.setTextureURL(WWSimpleShape.SIDE_SIDE1, "sky");
sky.setTransparency(WWSimpleShape.SIDE_SIDE1, 0.25);
world.addObject(sky);

// Create air (to provide friction and balloon floating)
WWSimpleShape
air = new WWSphere();
air.setName("air");
air.setPenetratable(true);
air.setPosition(new WWVector(0, 0, -100));
air.setSize(new WWVector(500, 5000, 5000));
air.setTransparency(WWSimpleShape.SIDE_ALL, 1.0);
air.setCutoutStart(0.5); // half dome
air.setSolid(false);
air.setRotation(new WWVector(0, 90, 0));
air.setFriction(0.1);
air.setDensity(0.0001);
world.addObject(air);

// Position the avatar
avatar.setPosition(0, 0, 25);
