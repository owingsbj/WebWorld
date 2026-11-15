// BumpersWorld -- Bumper car arena game with 6 courses based on OldBumpers

// World configuration
world.moveType = "drive";
world.status = "Select a course to start!";
world.playSong("theme_song.ogg", 0.1);

// Preload sounds
world.preloadSound("bonk2");
world.preloadSound("bonk4");
world.preloadSound("concrete");
world.preloadSound("loosingSound");
world.preloadSound("winningSound");
world.preloadSound("course1_song");
world.preloadSound("theme_song.ogg");

// State variables
var currentCourse = 1;
var playing = false;
var raceStartTime = 0;
var maxPlayTime = 60;
var cars = []; // array of carData plain JS objects
var trackObjects = [];
var elevator = null;
var lastHitCar = null;

var courseNames = ["Classic", "Water", "Trampoline", "Space", "Bowl", "Levels"];
var courseImages = ["world1.png", "world2.png", "world3.png", "world4.png", "world5.png", "world6.png"];

// Colors helper
var carColors = [
    new Color(1.0, 0.0, 0.0),       // 1: Red
    new Color(0.0, 1.0, 0.0),       // 2: Green
    new Color(0.0, 0.0, 1.0),       // 3: Blue
    new Color(1.0, 1.0, 0.0),       // 4: Yellow
    new Color(0.0, 1.0, 1.0),       // 5: Cyan
    new Color(1.0, 1.0, 0.5),       // 6: Light Yellow
    new Color(1.0, 0.5, 1.0),       // 7: Pink/Magenta
    new Color(1.0, 1.0, 0.5),       // 8: Light Yellow
    new Color(0.25, 1.0, 0.5),      // 9: Light Green
    new Color(1.0, 0.5, 0.25),      // 10: Orange
    new Color(0.5, 0.25, 1.0),      // 11: Purple
    new Color(1.0, 1.0, 1.0)        // 12: White
];

/**
 * Driver control helper functions
 */
function driverForward(d) {
    d.leftarm.transparency = 1;
    d.rightarm.transparency = 1;
    d.leftarmforward.transparency = 0;
    d.rightarmforward.transparency = 0;
    d.head.texture = new Texture("face" + d.type);
}

function driverBack(d) {
    d.leftarm.transparency = 0;
    d.rightarm.transparency = 0;
    d.leftarmforward.transparency = 1;
    d.rightarmforward.transparency = 1;
    d.head.texture = new Texture("face" + d.type);
}

function driverLeft(d) {
    d.leftarm.transparency = 0;
    d.rightarm.transparency = 1;
    d.leftarmforward.transparency = 1;
    d.rightarmforward.transparency = 0;
    d.head.texture = new Texture("face" + d.type);
}

function driverRight(d) {
    d.leftarm.transparency = 1;
    d.rightarm.transparency = 0;
    d.leftarmforward.transparency = 0;
    d.rightarmforward.transparency = 1;
    d.head.texture = new Texture("face" + d.type);
}

function driverStopped(d, crashed) {
    d.leftarm.transparency = 0;
    d.rightarm.transparency = 0;
    d.leftarmforward.transparency = 1;
    d.rightarmforward.transparency = 1;
    if (crashed) {
        d.head.texture = new Texture("face" + d.type + "o");
    } else {
        d.head.texture = new Texture("face" + d.type);
    }
}

/**
 * Car control helper functions
 */
function carLeft(c) {
    c.leftstick.transparency = 0;
    c.rightstick.transparency = 1;
    c.leftstickforward.transparency = 1;
    c.rightstickforward.transparency = 0;
    driverLeft(c.driverData);
}

function carRight(c) {
    c.leftstick.transparency = 1;
    c.rightstick.transparency = 0;
    c.leftstickforward.transparency = 0;
    c.rightstickforward.transparency = 1;
    driverRight(c.driverData);
}

function carForward(c) {
    c.leftstick.transparency = 1;
    c.rightstick.transparency = 1;
    c.leftstickforward.transparency = 0;
    c.rightstickforward.transparency = 0;
    driverForward(c.driverData);
}

function carBack(c) {
    c.leftstick.transparency = 0;
    c.rightstick.transparency = 0;
    c.leftstickforward.transparency = 1;
    c.rightstickforward.transparency = 1;
    driverBack(c.driverData);
}

function carStopped(c, crashed) {
    c.leftstick.transparency = 0;
    c.rightstick.transparency = 0;
    c.leftstickforward.transparency = 1;
    c.rightstickforward.transparency = 1;
    driverStopped(c.driverData, crashed);
}

/**
 * Driver constructor helper function - returns plain JS object holding driver elements
 */
function buildDriver(height, bulk, type, car) {
    var shade = 0.6 + Math.random() * 0.4;
    var skin = new Color(1.0 * shade, 0.8 * shade, 0.7 * shade);
    var shirt = new Color(0.5 + Math.random() * 0.5, 0.5 + Math.random() * 0.5, 0.5 + Math.random() * 0.5);
    var pants = new Color(0.5, 0.5, 1.0);

    var driver = new Sphere();
    driver.name = "driver";
    driver.phantom = true;
    driver.monolithic = true;
    driver.size = new Vector(0.5 + bulk, 0.3 + bulk, 0.6 + height);
    driver.taperX = -0.2;
    driver.color = shirt;
    world.addObject(driver);

    var rear = new Sphere();
    rear.fixed = true;
    rear.vertices = 8;
    rear.name = "rear";
    rear.phantom = true;
    rear.monolithic = true;
    rear.size = new Vector(0.5 + bulk, 0.4 + bulk, 0.4);
    rear.position = new Vector(0, 0, -0.25 + height / 2);
    rear.color = pants;
    world.addObject(rear);
    driver.addChild(rear);

    var head = new Sphere();
    head.name = "head";
    head.phantom = true;
    head.size = new Vector(0.25 + bulk / 2, 0.25, 0.3);
    head.position = new Vector(0, 0, 0.425 + height / 2);
    head.rotation = new Quaternion(0, 180, 0);
    var headTaper = Math.random() * 0.4 - 0.2;
    head.taperX = headTaper;
    head.taperY = headTaper;
    head.color = skin;
    head.texture = new Texture("face" + type, 1, 1, 180);
    world.addObject(head);
    driver.addChild(head);

    var nose = new Sphere();
    nose.name = "nose";
    nose.color = skin;
    nose.phantom = true;
    nose.size = new Vector(0.06 + Math.random() * 0.02 - 0.01, 0.08 + Math.random() * 0.02 - 0.01, 0.07 + Math.random() * 0.02 - 0.01);
    nose.taperX = 0.5;
    nose.taperY = 0.5;
    nose.rotation = new Quaternion(0, 180, 0);
    nose.position = new Vector(0, -0.115, -0.02);
    world.addObject(nose);
    head.addChild(nose);

    // Hair for women
    if (type === 3) { // blonde pigtails
        var hairColor = new Color(1, 1, 0.5);
        hairColor.mix(skin);

        var pigleft = new Sphere();
        pigleft.fixed = true;
        pigleft.phantom = true;
        pigleft.size = new Vector(0.1, 0.1, 0.3);
        pigleft.color = hairColor;
        pigleft.position = new Vector(-0.13, 0.05, 0.06);
        pigleft.rotation = new Quaternion(15, 25, 0);
        world.addObject(pigleft);
        head.addChild(pigleft);

        var pigright = new Sphere();
        pigright.fixed = true;
        pigright.phantom = true;
        pigright.size = new Vector(0.1, 0.1, 0.3);
        pigright.color = hairColor;
        pigright.position = new Vector(0.13, 0.05, 0.06);
        pigright.rotation = new Quaternion(15, -25, 0);
        world.addObject(pigright);
        head.addChild(pigright);

    } else if (type === 4) { // brunette bushy hair
        var hairColor = new Color(0.35, 0.18, 0.18);
        hairColor.mix(skin);

        var pigleft = new Sphere();
        pigleft.taperX = 0.25;
        pigleft.fixed = true;
        pigleft.phantom = true;
        pigleft.size = new Vector(0.35, 0.2, 0.4);
        pigleft.color = hairColor;
        pigleft.position = new Vector(-0.03, 0.05, 0.06);
        pigleft.rotation = new Quaternion(15, 30, 0);
        world.addObject(pigleft);
        head.addChild(pigleft);

        var center = new Sphere();
        center.taperY = -0.25;
        center.fixed = true;
        center.phantom = true;
        center.size = new Vector(0.35, 0.2, 0.4);
        center.color = hairColor;
        center.position = new Vector(0, 0.05, 0.04);
        center.rotation = new Quaternion(15, 0, 0);
        world.addObject(center);
        head.addChild(center);

        var pigright = new Sphere();
        pigright.taperX = 0.25;
        pigright.fixed = true;
        pigright.phantom = true;
        pigright.size = new Vector(0.35, 0.2, 0.4);
        pigright.color = hairColor;
        pigright.position = new Vector(0.03, 0.05, 0.06);
        pigright.rotation = new Quaternion(15, -30, 0);
        world.addObject(pigright);
        head.addChild(pigright);
    }

    // Hat for boys
    if (type === 1 && height < 0) {
        var hat = new Sphere();
        hat.fixed = true;
        hat.vertices = 8;
        hat.name = "hat";
        hat.phantom = true;
        hat.cutoutStart = 0;
        hat.cutoutEnd = 0.5;
        hat.size = new Vector(0.3 + bulk / 2, 0.3 + bulk / 2, 0.3 + bulk / 2);
        hat.position = new Vector(0, 0, -0.1);
        hat.rotation = new Quaternion(0, 90, 0);
        hat.color = new Color(0, 0, 0);
        world.addObject(hat);
        head.addChild(hat);

        var brim = new Cylinder();
        brim.fixed = true;
        brim.vertices = 8;
        brim.phantom = true;
        brim.size = new Vector(0.3 + bulk / 2, 0.3 + bulk / 2, 0.01);
        brim.position = new Vector(0, -0.1, -0.1);
        brim.color = new Color(0, 0, 0);
        world.addObject(brim);
        head.addChild(brim);
    }

    // Glasses and sunglasses
    if (Math.random() < 0.35) {
        var h = 0.05 + Math.random() * 0.05;
        var glassesColor = new Color(0, 0, 0);
        var glassTex = Math.random() > 0.5 ? "glasses" : "sunglasses";

        var spec1 = new Cylinder();
        spec1.fixed = true;
        spec1.name = "spec1";
        spec1.phantom = true;
        spec1.size = new Vector(0.1, h, 0.01);
        spec1.rotation = new Quaternion(90, 0, 0);
        spec1.position = new Vector(0.06, -0.12, -0.06);
        spec1.colorSide1 = glassesColor;
        spec1.transparencyTop = 0.01;
        spec1.transparencyBottom = 0.01;
        spec1.textureTop = new Texture(glassTex, 1, 1, 180);
        spec1.textureBottom = new Texture(glassTex);
        world.addObject(spec1);
        head.addChild(spec1);

        var spec2 = new Cylinder();
        spec2.fixed = true;
        spec2.name = "spec2";
        spec2.phantom = true;
        spec2.size = new Vector(0.1, h, 0.01);
        spec2.rotation = new Quaternion(90, 0, 0);
        spec2.position = new Vector(-0.06, -0.12, -0.06);
        spec2.colorSide1 = glassesColor;
        spec2.transparencyTop = 0.01;
        spec2.transparencyBottom = 0.01;
        spec2.textureTop = new Texture(glassTex, 1, 1, 180);
        spec2.textureBottom = new Texture(glassTex);
        world.addObject(spec2);
        head.addChild(spec2);

        var handle1 = new Cylinder();
        handle1.color = glassesColor;
        handle1.fixed = true;
        handle1.phantom = true;
        handle1.size = new Vector(0.01, 0.01, 0.2);
        handle1.rotation = new Quaternion(90, 0, 10);
        handle1.position = new Vector(0.12, -0.02, -0.06);
        world.addObject(handle1);
        head.addChild(handle1);

        var handle2 = new Cylinder();
        handle2.color = glassesColor;
        handle2.fixed = true;
        handle2.phantom = true;
        handle2.size = new Vector(0.01, 0.01, 0.2);
        handle2.rotation = new Quaternion(90, 0, -10);
        handle2.position = new Vector(-0.12, -0.02, -0.06);
        world.addObject(handle2);
        head.addChild(handle2);

        var bridge = new Box();
        bridge.color = glassesColor;
        bridge.fixed = true;
        bridge.phantom = true;
        bridge.size = new Vector(0.05, 0.01, 0.01);
        bridge.position = new Vector(0, -0.12, -0.06);
        world.addObject(bridge);
        head.addChild(bridge);
    }

    var legsize = 0.2 + bulk / 2;

    var leftleg = new Cylinder();
    leftleg.fixed = true;
    leftleg.vertices = 5;
    leftleg.phantom = true;
    leftleg.monolithic = true;
    leftleg.size = new Vector(legsize, legsize, 0.5 + height / 2);
    leftleg.rotation = new Quaternion(90, 0, 0);
    leftleg.position = new Vector(-0.15, -0.25, -0.3);
    leftleg.color = pants;
    world.addObject(leftleg);
    driver.addChild(leftleg);

    var rightleg = new Cylinder();
    rightleg.fixed = true;
    rightleg.vertices = 5;
    rightleg.phantom = true;
    rightleg.monolithic = true;
    rightleg.size = new Vector(legsize, legsize, 0.5 + height / 2);
    rightleg.rotation = new Quaternion(90, 0, 0);
    rightleg.position = new Vector(0.15, -0.25, -0.3);
    rightleg.color = pants;
    world.addObject(rightleg);
    driver.addChild(rightleg);

    var leftarm = new Torus();
    leftarm.phantom = true;
    leftarm.monolithic = true;
    leftarm.cutoutStart = 0;
    leftarm.cutoutEnd = 0.5;
    leftarm.size = new Vector(0.25, 0.6, 0.1);
    leftarm.position = new Vector(-0.20, -0.2, 0.1);
    leftarm.rotation = new Quaternion(-25 - height * 100, 0, 10);
    leftarm.color = skin;
    world.addObject(leftarm);
    driver.addChild(leftarm);

    var rightarm = new Torus();
    rightarm.phantom = true;
    rightarm.monolithic = true;
    rightarm.cutoutStart = 0.5;
    rightarm.cutoutEnd = 1.0;
    rightarm.size = new Vector(0.25, 0.6, 0.1);
    rightarm.position = new Vector(0.20, -0.2, 0.1);
    rightarm.rotation = new Quaternion(-25 - height * 100, 0, -10);
    rightarm.color = skin;
    world.addObject(rightarm);
    driver.addChild(rightarm);

    var leftarmforward = new Torus();
    leftarmforward.phantom = true;
    leftarmforward.monolithic = true;
    leftarmforward.cutoutStart = 0;
    leftarmforward.cutoutEnd = 0.5;
    leftarmforward.size = new Vector(0.2, 0.8, 0.1);
    leftarmforward.position = new Vector(-0.20, -0.3, 0.1);
    leftarmforward.rotation = new Quaternion(-20 - height * 100, 0, 10);
    leftarmforward.color = skin;
    leftarmforward.transparency = 1.0;
    world.addObject(leftarmforward);
    driver.addChild(leftarmforward);

    var rightarmforward = new Torus();
    rightarmforward.phantom = true;
    rightarmforward.monolithic = true;
    rightarmforward.cutoutStart = 0.5;
    rightarmforward.cutoutEnd = 1.0;
    rightarmforward.size = new Vector(0.2, 0.8, 0.1);
    rightarmforward.position = new Vector(0.20, -0.3, 0.1);
    rightarmforward.rotation = new Quaternion(-20 - height * 100, 0, -10);
    rightarmforward.color = skin;
    rightarmforward.transparency = 1.0;
    world.addObject(rightarmforward);
    driver.addChild(rightarmforward);

    return {
        driverObj: driver,
        head: head,
        leftarm: leftarm,
        rightarm: rightarm,
        leftarmforward: leftarmforward,
        rightarmforward: rightarmforward,
        type: type
    };
}

/**
 * Creates a car object with attachments and driver - returns plain JS object holding car elements
 */
function createCar(index) {
    var carColor = carColors[index - 1] || carColors[0];

    var car = new Sphere();
    car.name = "car" + index;
    car.size = new Vector(2, 2, 0.75);
    car.transparency = 1.0;
    car.penetratable = true;
    car.physical = true;
    car.density = 0.25;
    car.elasticity = 0.75;
    car.freedomMove = [false, true, true];
    car.freedomRotate = [false, false, false];
    car.friction = 0.125;
    world.addObject(car);

    var tube = new Torus();
    tube.vertices = 32;
    tube.phantom = true;
    tube.pickable = true;
    tube.monolithic = true;
    tube.penetratable = true;
    tube.size = new Vector(2, 2, 0.75);
    tube.color = carColor.darker().darker();
    world.addObject(tube);
    car.addChild(tube);

    var platform = new Cylinder();
    platform.fixed = true;
    platform.vertices = 5;
    platform.phantom = true;
    platform.monolithic = true;
    platform.penetratable = true;
    platform.size = new Vector(1.5, 1.5, 0.25);
    platform.color = carColor;
    world.addObject(platform);
    car.addChild(platform);

    var seat = new Cylinder();
    seat.vertices = 32;
    seat.phantom = true;
    seat.penetratable = true;
    seat.monolithic = true;
    seat.size = new Vector(0.75, 1, 0.1);
    seat.position = new Vector(0, 0.5, 0.4);
    seat.rotation = new Quaternion(110, 0, 0);
    seat.color = carColor;
    world.addObject(seat);
    car.addChild(seat);

    var leftstick = new Cylinder();
    leftstick.phantom = true;
    leftstick.monolithic = true;
    leftstick.color = carColor;
    leftstick.size = new Vector(0.05, 0.05, 0.5);
    leftstick.position = new Vector(-0.3, -0.2, 0.3);
    leftstick.rotation = new Quaternion(0, -20, 0);
    leftstick.gluedToParent = false;
    world.addObject(leftstick);
    car.addChild(leftstick);

    var rightstick = new Cylinder();
    rightstick.phantom = true;
    rightstick.monolithic = true;
    rightstick.color = carColor;
    rightstick.size = new Vector(0.3, -0.2, 0.3);
    rightstick.rotation = new Quaternion(0, 20, 0);
    rightstick.gluedToParent = false;
    world.addObject(rightstick);
    car.addChild(rightstick);

    var leftstickforward = new Cylinder();
    leftstickforward.phantom = true;
    leftstickforward.monolithic = true;
    leftstickforward.color = carColor;
    leftstickforward.size = new Vector(0.05, 0.05, 0.5);
    leftstickforward.position = new Vector(-0.3, -0.25, 0.3);
    leftstickforward.rotation = new Quaternion(-20, -20, 0);
    leftstickforward.transparency = 1.0;
    leftstickforward.gluedToParent = false;
    world.addObject(leftstickforward);
    car.addChild(leftstickforward);

    var rightstickforward = new Cylinder();
    rightstickforward.phantom = true;
    rightstickforward.monolithic = true;
    rightstickforward.color = carColor;
    rightstickforward.size = new Vector(0.05, 0.05, 0.5);
    rightstickforward.position = new Vector(0.3, -0.25, 0.3);
    rightstickforward.rotation = new Quaternion(-20, 20, 0);
    rightstickforward.transparency = 1.0;
    rightstickforward.gluedToParent = false;
    world.addObject(rightstickforward);
    car.addChild(rightstickforward);

    var height = (index === 1) ? 0 : (Math.random() * 0.6 - 0.3);
    var bulk = (index === 1) ? 0 : (Math.random() * 0.4 - 0.2);
    var type = (index === 1) ? 1 : Math.floor(Math.random() * 4 + 1);

    var driverData = buildDriver(height, bulk, type, car);
    driverData.driverObj.position = new Vector(0, 0.2, 0.1 + driverData.driverObj.size.z / 2);
    driverData.driverObj.rotation = new Quaternion(10, 0, 0);
    driverData.driverObj.fixed = true;
    car.addChild(driverData.driverObj);

    car.position = new Vector(1.9 * index - 12, Math.random() * 20 - 10, 1);
    car.rotation = new Quaternion(0, 0, Math.random() * 360);

    return {
        index: index,
        carObj: car,
        leftstick: leftstick,
        rightstick: rightstick,
        leftstickforward: leftstickforward,
        rightstickforward: rightstickforward,
        driverData: driverData,
        nextAiTime: 0
    };
}

// Build all 12 cars
for (var c = 1; c <= 12; c++) {
    var carData = createCar(c);
    cars.push(carData);
}

// Player avatar car is car1's 3D Sphere host object
avatar = cars[0].carObj;

/**
 * Builds the track corresponding to the selected course number (1-6)
 */
function makeTrack(courseNum) {
    // Clear previous track objects
    trackObjects.forEach(function(o) {
        world.removeObject(o);
    });
    trackObjects = [];
    elevator = null;

    if (courseNum === 1) { // Classic
        world.gravity = 9.8;
        maxPlayTime = 60;

        var subfloor = new Box();
        subfloor.name = "subfloor";
        subfloor.size = new Vector(100, 100, 10);
        subfloor.position = new Vector(0, 0, -15);
        subfloor.fixed = true;
        subfloor.transparency = 1.0;
        world.addObject(subfloor);
        trackObjects.push(subfloor);

        var floor = new Box();
        floor.size = new Vector(25, 25, 10);
        floor.position = new Vector(0, 0, -5);
        floor.fixed = true;
        floor.texture = new Texture("track_floor");
        floor.elasticity = 0;
        world.addObject(floor);
        trackObjects.push(floor);

        var w1 = new Cylinder();
        w1.name = "wall1";
        w1.size = new Vector(2, 1, 25);
        w1.position = new Vector(-12, 0, 0.6);
        w1.rotation = new Quaternion(90, 0, 0);
        w1.fixed = true;
        w1.color = new Color(0.75, 0.75, 0.75);
        world.addObject(w1);
        trackObjects.push(w1);

        var w2 = new Cylinder();
        w2.name = "wall2";
        w2.size = new Vector(1, 2, 25);
        w2.position = new Vector(0, -12, 0.6);
        w2.rotation = new Quaternion(0, 90, 0);
        w2.fixed = true;
        w2.color = new Color(0.75, 0.75, 0.75);
        world.addObject(w2);
        trackObjects.push(w2);

        var w3 = new Cylinder();
        w3.name = "wall3";
        w3.size = new Vector(2, 1, 25);
        w3.position = new Vector(12, 0, 0.6);
        w3.rotation = new Quaternion(90, 0, 0);
        w3.fixed = true;
        w3.color = new Color(0.75, 0.75, 0.75);
        world.addObject(w3);
        trackObjects.push(w3);

        var w4 = new Cylinder();
        w4.name = "wall4";
        w4.size = new Vector(1, 2, 25);
        w4.position = new Vector(0, 12, 0.6);
        w4.rotation = new Quaternion(0, 90, 0);
        w4.fixed = true;
        w4.color = new Color(0.75, 0.75, 0.75);
        world.addObject(w4);
        trackObjects.push(w4);

    } else if (courseNum === 2) { // Water
        world.gravity = 9.8;
        maxPlayTime = 60;

        var subfloor = new Box();
        subfloor.name = "subfloor";
        subfloor.size = new Vector(100, 100, 10);
        subfloor.position = new Vector(0, 0, -50);
        subfloor.fixed = true;
        subfloor.transparency = 1.0;
        world.addObject(subfloor);
        trackObjects.push(subfloor);

        var roof = new Box();
        roof.name = "roof";
        roof.penetratable = true;
        roof.size = new Vector(100, 100, 10);
        roof.position = new Vector(0, 0, 7);
        roof.fixed = true;
        roof.transparency = 1.0;
        world.addObject(roof);
        trackObjects.push(roof);

        var floor = new Cylinder();
        floor.monolithic = true;
        floor.size = new Vector(32, 32, 10);
        floor.position = new Vector(0, 0, -5);
        floor.fixed = true;
        floor.texture = new Texture("water", 0.01, 0.1);
        floor.color = new Color(0.5, 0.5, 1.0);
        floor.solid = false;
        floor.density = 10.0;
        floor.elasticity = 0;
        floor.friction = 1.0;
        world.addObject(floor);
        trackObjects.push(floor);

        var wall = new Cylinder();
        wall.name = "wall1";
        wall.penetratable = true;
        wall.monolithic = true;
        wall.size = new Vector(33, 33, 11);
        wall.position = new Vector(0, 0, -5);
        wall.fixed = true;
        wall.hollow = 0.9;
        wall.color = new Color(0.75, 0.75, 0.75);
        world.addObject(wall);
        trackObjects.push(wall);

    } else if (courseNum === 3) { // Trampoline
        world.gravity = 9.8;
        maxPlayTime = 60;

        var subfloor = new Box();
        subfloor.name = "subfloor";
        subfloor.size = new Vector(100, 100, 10);
        subfloor.position = new Vector(0, 0, -100);
        subfloor.fixed = true;
        subfloor.transparency = 1.0;
        world.addObject(subfloor);
        trackObjects.push(subfloor);

        var floor = new Box();
        floor.size = new Vector(25, 25, 10);
        floor.position = new Vector(0, 0, -5);
        floor.hollow = 0.25;
        floor.fixed = true;
        floor.texture = new Texture("track_floor");
        floor.elasticity = 0;
        world.addObject(floor);
        trackObjects.push(floor);

        var tramp = new Box();
        tramp.name = "trampoline";
        tramp.size = new Vector(20, 20, 3.75);
        tramp.position = new Vector(0, 0, -2);
        tramp.elasticity = 0.8;
        tramp.fixed = true;
        tramp.texture = new Texture("aggregate");
        world.addObject(tramp);
        trackObjects.push(tramp);

        var wall1 = new Box();
        wall1.name = "wall1";
        wall1.size = new Vector(25.5, 25.5, 40);
        wall1.hollow = 0.9;
        wall1.position = new Vector(0, 0, 10);
        wall1.fixed = true;
        wall1.penetratable = true;
        wall1.transparency = 0.5;
        wall1.texture = new Texture("concrete");
        world.addObject(wall1);
        trackObjects.push(wall1);

    } else if (courseNum === 4) { // Space
        world.gravity = 0;
        maxPlayTime = 60;

        var sky = new Sphere();
        sky.phantom = true;
        sky.hollow = 0.90;
        sky.size = new Vector(5000, 5000, 5000);
        sky.position = new Vector(0, 0, 0);
        sky.monolithic = true;
        sky.fullBright = true;
        sky.texture = new Texture("space", 0.1, 0.1);
        world.addObject(sky);
        trackObjects.push(sky);

        var planet = new Sphere();
        planet.phantom = true;
        planet.vertices = 64;
        planet.size = new Vector(1000, 1000, 1000);
        planet.position = new Vector(-500, 0, -400);
        planet.texture = new Texture("planet");
        planet.angularVelocity = new Vector(0, 0, 5);
        world.addObject(planet);
        trackObjects.push(planet);

        var moon = new Sphere();
        moon.fixed = true;
        moon.phantom = true;
        moon.size = new Vector(100, 100, 100);
        moon.position = new Vector(500, 0, 100);
        moon.texture = new Texture("moon");
        moon.fullBright = true;
        world.addObject(moon);
        trackObjects.push(moon);

        var floor = new Box();
        floor.name = "wall";
        floor.size = new Vector(25, 25, 5);
        floor.position = new Vector(0, 0, -2.5);
        floor.fixed = true;
        floor.texture = new Texture("mesh", 0.1, 0.1);
        floor.transparency = 0.01;
        world.addObject(floor);
        trackObjects.push(floor);

        var roof = new Box();
        roof.name = "wall";
        roof.size = new Vector(25, 25, 10);
        roof.position = new Vector(0, 0, 15);
        roof.fixed = true;
        roof.texture = new Texture("mesh", 0.1, 0.1);
        roof.transparency = 0.01;
        roof.penetratable = true;
        world.addObject(roof);
        trackObjects.push(roof);

        var wall1 = new Box();
        wall1.name = "wall1";
        wall1.size = new Vector(25.5, 25.5, 10);
        wall1.hollow = 0.9;
        wall1.position = new Vector(0, 0, 5);
        wall1.fixed = true;
        wall1.penetratable = true;
        wall1.transparency = 0.01;
        wall1.texture = new Texture("mesh", 0.1, 0.1);
        world.addObject(wall1);
        trackObjects.push(wall1);

        // Add space domes to cars
        cars.forEach(function(cData) {
            var car = cData.carObj;
            car.size = new Vector(2, 2, 2);
            var dome = new Sphere();
            dome.phantom = true;
            dome.penetratable = true;
            dome.size = new Vector(1.6, 1.6, 1.4);
            dome.position = new Vector(0, 0, 0.6);
            dome.transparency = 0.7;
            dome.shininess = 0.1;
            car.addChild(dome);
            world.addObject(dome);
            trackObjects.push(dome);
        });

    } else if (courseNum === 5) { // Bowl
        world.gravity = 9.8;
        maxPlayTime = 60;

        var floor = new Sphere();
        floor.size = new Vector(50, 50, 50);
        floor.position = new Vector(0, 0, 10);
        floor.hollow = 0.85;
        floor.cutoutEnd = 0.5;
        floor.rotation = new Quaternion(0, 90, 0);
        floor.texture = new Texture("track_floor");
        floor.elasticity = 0;
        floor.friction = 0;
        floor.penetratable = true;
        world.addObject(floor);
        trackObjects.push(floor);

        var wall1 = new Sphere();
        wall1.name = "wall1";
        wall1.size = new Vector(50, 50, 50);
        wall1.hollow = 0.9;
        wall1.position = new Vector(0, 0, 10);
        wall1.fixed = true;
        wall1.penetratable = true;
        wall1.transparency = 0.01;
        wall1.texture = new Texture("mesh", 0.01, 0.01);
        world.addObject(wall1);
        trackObjects.push(wall1);

        cars.forEach(function(cData) {
            var car = cData.carObj;
            car.size = new Vector(2.5, 2.5, 2.0);
            car.elasticity = 0.25;
        });

    } else if (courseNum === 6) { // Levels
        world.gravity = 9.8;
        maxPlayTime = 120;

        var subfloor = new Box();
        subfloor.name = "subfloor";
        subfloor.size = new Vector(100, 100, 10);
        subfloor.position = new Vector(0, 0, -100);
        subfloor.fixed = true;
        subfloor.transparency = 1.0;
        world.addObject(subfloor);
        trackObjects.push(subfloor);

        var floor = new Box();
        floor.size = new Vector(25, 25, 4);
        floor.position = new Vector(0, 0, -1.5);
        floor.fixed = true;
        floor.monolithic = true;
        floor.texture = new Texture("track_floor");
        floor.elasticity = 0;
        world.addObject(floor);
        trackObjects.push(floor);

        var level2 = new Box();
        level2.size = new Vector(25, 25, 1);
        level2.position = new Vector(0, 0, 5);
        level2.hollow = 0.25;
        level2.fixed = true;
        level2.monolithic = true;
        level2.texture = new Texture("concrete", 0.01, 0.01);
        level2.transparency = 0.01;
        level2.elasticity = 0;
        world.addObject(level2);
        trackObjects.push(level2);

        var level3 = new Box();
        level3.size = new Vector(25, 25, 1);
        level3.position = new Vector(0, 0, 10);
        level3.hollow = 0.25;
        level3.fixed = true;
        level3.monolithic = true;
        level3.texture = new Texture("concrete", 0.01, 0.01);
        level3.transparency = 0.01;
        level3.elasticity = 0;
        world.addObject(level3);
        trackObjects.push(level3);

        elevator = new Box();
        elevator.size = new Vector(7.5, 7.5, 1);
        elevator.position = new Vector(0, 0, 0.01);
        elevator.elasticity = 0;
        world.addObject(elevator);
        trackObjects.push(elevator);

        var wall1 = new Box();
        wall1.name = "wall1";
        wall1.size = new Vector(25.5, 25.5, 20);
        wall1.hollow = 0.9;
        wall1.position = new Vector(0, 0, 10);
        wall1.fixed = true;
        wall1.penetratable = true;
        wall1.monolithic = true;
        wall1.transparency = 0.5;
        wall1.texture = new Texture("concrete");
        world.addObject(wall1);
        trackObjects.push(wall1);
    }
}

/**
 * Resets positions of all cars
 */
function resetCars() {
    cars.forEach(function(cData) {
        var car = cData.carObj;
        var i = cData.index;
        car.velocity = new Vector(0, 0, 0);
        car.angularVelocity = new Vector(0, 0, 0);
        car.thrust = new Vector(0, 0, 0);
        car.torque = new Vector(0, 0, 0);

        if (currentCourse === 6) {
            car.position = new Vector(1.9 * i - 12, Math.random() * 20 - 10, 1 + Math.floor(Math.random() * 3) * 5);
        } else if (currentCourse === 4) {
            car.position = new Vector(1.9 * i - 12, Math.random() * 20 - 10, 2);
        } else {
            car.position = new Vector(1.9 * i - 12, Math.random() * 20 - 10, 1);
        }
        car.rotation = new Quaternion(0, 0, 0);
        carStopped(cData, false);
    });
}

/**
 * Contender AI behavior loop
 */
function updateAI() {
    for (var i = 1; i < cars.length; i++) {
        var cData = cars[i];
        var car = cData.carObj;
        if (!playing) {
            car.thrust = new Vector(0, 0, 0);
            car.torque = new Vector(0, 0, 0);
            carStopped(cData, false);
            continue;
        }
        if (!cData.nextAiTime || world.time > cData.nextAiTime) {
            var action = Math.floor(Math.random() * 5);
            if (action === 0) {
                car.thrust = new Vector(0, 4, -4);
                car.torque = new Vector(0, 0, 0);
                carBack(cData);
            } else if (action === 1) {
                car.thrust = new Vector(0, -8, 4);
                car.torque = new Vector(0, 0, 0);
                carForward(cData);
            } else if (action === 2) {
                car.torque = new Vector(0, 0, 60);
                carLeft(cData);
            } else if (action === 3) {
                car.torque = new Vector(0, 0, -60);
                carRight(cData);
            } else {
                car.thrust = new Vector(0, 0, 0);
                car.torque = new Vector(0, 0, 0);
                carStopped(cData, false);
            }
            cData.nextAiTime = world.time + 1000 + Math.random() * 3000;
        }
    }
}

/**
 * Elevator animation for Course 6
 */
var elevatorGoingUp = true;
function updateElevator() {
    if (!elevator || currentCourse !== 6) return;
    var pos = elevator.position;
    if (elevatorGoingUp) {
        pos.z += 0.05;
        if (pos.z >= 9.5) elevatorGoingUp = false;
    } else {
        pos.z -= 0.05;
        if (pos.z <= 0.01) elevatorGoingUp = true;
    }
    elevator.position = pos;
}

/**
 * Bowl physics adaptation for Course 5
 */
function updateBowlPhysics() {
    if (currentCourse !== 5) return;
    cars.forEach(function(cData) {
        var car = cData.carObj;
        var pos = car.position;
        var rot = car.rotation;
        var poslen = Math.sqrt(pos.x * pos.x + pos.y * pos.y);
        var bowlangle = Math.atan2(pos.y, pos.x);
        var yawRad = (rot.yaw || 0) * Math.PI / 180.0;
        var tilt = -Math.sin(bowlangle + yawRad) * poslen * 1.8;
        var lean = Math.cos(bowlangle + yawRad) * poslen * 1.8;
        car.rotation = new Quaternion(tilt, lean, rot.yaw || 0);
    });
}

/**
 * Player car collision callback
 */
avatar.onCollide = function(nearObject) {
    if (!playing || !nearObject) return;

    if (nearObject.name && nearObject.name.indexOf("car") === 0 && nearObject !== avatar) {
        if (nearObject !== lastHitCar) {
            lastHitCar = nearObject;

            // Stop driver of hit car
            for (var k = 0; k < cars.length; k++) {
                if (cars[k].carObj === nearObject || cars[k].carObj.name === nearObject.name) {
                    carStopped(cars[k], true);
                    break;
                }
            }

            var rotDiff = (nearObject.rotation.yaw || 0) - (avatar.rotation.yaw || 0);
            var alignment = Math.pow(Math.cos(rotDiff * Math.PI / 180.0), 4);
            var velLen = avatar.velocity.length();
            var hitscore = Math.floor(velLen * alignment * 1200 / maxPlayTime);

            if (currentCourse === 6) {
                var levelMult = 1;
                if (avatar.position.z >= 7.5) {
                    levelMult = 100;
                } else if (avatar.position.z >= 2.5) {
                    levelMult = 10;
                }
                hitscore *= levelMult;
            }

            if (hitscore > 25) {
                world.playSound("bonk2", 0.5);
                world.score = (world.score || 0) + hitscore;
                console.info("Hit! + " + hitscore + " points!");
            } else {
                world.playSound("bonk4", 1.0, 0.5);
            }
        }
    } else if (nearObject.name && nearObject.name.indexOf("wall") === 0) {
        world.playSound("concrete", 1.0);
        world.score = Math.max(0, (world.score || 0) - 1);
    } else if (nearObject.name === "subfloor") {
        stopPlaying();
        world.playSound("loosingSound", 1.0);
        if (confirm("Sorry, you fell off!\nDo you want to play again?")) {
            startPlaying();
        }
    }
};

/**
 * Main game loop called periodically
 */
avatar.timer = 100;
avatar.onTimer = function() {
    avatar.timer = 100;

    updateAI();
    updateElevator();
    updateBowlPhysics();

    if (playing) {
        var elapsedSec = Math.floor((world.time - raceStartTime) / 1000);
        var remaining = Math.max(0, maxPlayTime - elapsedSec);
        var mins = Math.floor(remaining / 60);
        var secs = remaining % 60;
        var timeStr = mins + ":" + (secs < 10 ? "0" : "") + secs;

        world.status = courseNames[currentCourse - 1] + " | Time: " + timeStr + " | Score: " + (world.score || 0);

        if (elapsedSec >= maxPlayTime) {
            stopPlaying();
            world.playSound("winningSound", 0.5);
            if (confirm("Game Over! Your final score is " + (world.score || 0) + ".\nPlay again?")) {
                startPlaying();
            }
        }
    }
};

/**
 * Start playing game
 */
function startPlaying() {
    raceStartTime = world.time;
    world.score = 0;
    playing = true;
    lastHitCar = null;
    resetCars();
    world.playSong("course1_song", 0.1);
    world.status = courseNames[currentCourse - 1] + " | Playing!";
}

/**
 * Stop playing game
 */
function stopPlaying() {
    playing = false;
    world.stopPlayingSong();
    cars.forEach(function(cData) {
        var car = cData.carObj;
        car.velocity = new Vector(0, 0, 0);
        car.angularVelocity = new Vector(0, 0, 0);
        car.thrust = new Vector(0, 0, 0);
        car.torque = new Vector(0, 0, 0);
        carStopped(cData, false);
    });
    world.status = courseNames[currentCourse - 1] + " | Course Ready";
}

/**
 * Select a course from menu
 */
function selectCourseMenu() {
    var choice = select("Select Course", courseNames, courseImages);
    var index = courseNames.indexOf(choice);
    if (index >= 0) {
        currentCourse = index + 1;
        makeTrack(currentCourse);
        stopPlaying();
        if (currentCourse === 4) {
            world.avatarActions = [new Action("Up", function() {
                avatar.thrust = new Vector(0, 0, 25);
            }), new Action("Down", function() {
                avatar.thrust = new Vector(0, 0, -25);
            })];
        } else {
            world.avatarActions = [];
        }
    }
}

// Set up World Actions
world.worldActions = [
    new Action("Start", function() { startPlaying(); }),
    new Action("Course", function() { selectCourseMenu(); })
];

// Initialize default track (Course 1: Classic)
makeTrack(1);
resetCars();
