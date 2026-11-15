// HelloWorld -- a very simple world

// all that is needed is an object for the avatar to stand on
ground = new Box();
ground.size = [100, 100, 1];
ground.texture = "ice";
world.addObject(ground);

// and then the avatar should be placed on top of the object
avatar.position = [0, 0, 2];