# BUILT-INS

WebWorld has some built-in textures, sounds, animations and behaviors.  You can use these in your worlds.  (They aren't the greatest, but they are there!)

## Textures

| name | description | 
|--|--|
| aggregate | rocks that are connected together like in an aggregate.  Works for rock walls. |
| android\_skin | the Android robot, used as a skin of the egg avatar
| bark | tree bark.  Good for tree trunks. |
| brick | simple brick wall pattern |
| checker | black and white squares.  Good for any checkerboard design. |
| concrete | concrete surface. |
| drymud | the cracked look of dried mud. |
| earth | a texture image of the earth.  Can be put on a sphere. |
| finish | an image of the word FINISH for the ending of a course |
| grass | a grassy texture |
| green\_goggle_dog | the green goggle dog logo shown at the start of WebWorld |
| happy\_skin | a happy face, used as the skin of the egg avatar |
| ice | the look of an icy surface |
| ivy | an ivy covered wall |
| land  | a tree-like canopy.  Useful for the surface of far away hills |
| moon | a texture image of the moon.  Can be put on a sphere. |
| moss | a moss texture good for green land |
| redstripe | red and white stripe texture |
| redx | a red x texture.  Used when other textures fail to load. |
| sand | texture of sand, like on a beach |
| sky | a mostly sunny sky with light clouds |
| space | a picture of deep space |
| start | an image of the word START for the starting of a course |
| stucco | stucco from the side of a building |
| sweety\_kitty | can't forget sweety cat!  She has green headphones to listen to the audio. |
| track | the look of an asphalt raceway |
| tread | the tread of a tire  |
| tree | a gif image of a tree with transparent sections for use on the Plant 3D object |
| water | a water texture.  Animate for moving water. |
| wood | texture of wooden floor or wall |

# Sounds

| name | description |
|--|--|
| awful |  |
| bang | a firework or small explosion |
| beep | a beep sound |
| bell | the ding of a bell |
| bonk1 | bonk sound, good for pinball |
| bonk2 | another bonk sound  |
| bonk3 | a third bonk sound |
| bonk4 | a fourth bonk sound |
| car | the sound of an  car engine |
| carCrash | the sound of a car crash (crunching metal and breaking glass) |
| carScrape | the sound of metal scraping |
| carSlide | the sound of tires squeaking |
| chainlink | the sound of running a pole along a chain link fence |
| chords |  |
| concrete | hitting on rock or concrete |
| down |  |
| formula_car | sound of a high revving engine |
| grass | landing on a grassy or mossy surface  |
| guitar | a pluck of a guitar |
| honk | the honk of a horn  |
| loosingSound |  |
| movingGrass | the rustling sound of moving through grass or plants |
| movingWater | a gurgling sound of moving water or moving through water |
| musicbox | one note played on a music box |
| paddle | a pinball paddle sound |
| phaser | sci-fi sound of the zap of a ray gun |
| piano | a note played on a piano |
| saucer | sci-fi sound of a flying saucer |
| skate | a rolling skateboard |
| softbell | a soft ding of a bell |
| splash | dropping into water |
| success | a sound appropriate for when a coin or other object is successfully obtained in a game |
| throw | cartoon sound of a ball being thrown fast |
| thruster | exhaust sound from a jet engine |
| thud | hitting a mostly solid surface such as the ground |
| tick | a clock tick sound |
| warp | sci-fi sound of entering warp speed |
| whack | cartoon sound of a ball hitting a person |
| winningSound |  |
| wood | hitting a wooden surface |


# Animations

| name | description |
|--|--|
| walking | a walking animation for an avatar.  There should be child objects named "left leg" and "right leg" which will be animated.  There can also be "left arm" and "right arm" which will swing. |
| swimming | a freestyle swimming animation for an avatar.  There should be objects named "head", "torso", "left arm", "right arm", "left leg" and "right leg". |
| treading | a treading-water animation for an avatar.  Needs "torso", "left arm", "right arm", "left leg" and "right leg".  
| falling | a falling animation for an avatar.  There should be objects named "head", "torso", "left arm", "right arm", "left leg" and "right leg". |
| running | a running animation for an avatar. |

# Behaviors

| name | description |
|--|--|
| HumanMotionBehavior | Adding this behavior to a 3D object will make it animate like a human when moving due to forces applied.  This includes walking, running, falling, swimming and treading animations.   The object or subobjects should have names to indicate the part of the body: head, neck, torso, left arm, right arm, left forearm, right forearm, left hand, right hand, left leg, right leg, left calf, right calf, left foot, right foot. |
| SquishyBehavior | Adding this behavior will cause a 3D object to deform briefly when its velocity quickly changes.  This makes it look like it is made of a squishy substance. |
| WindBendBehavior | Adding this to a plant will cause it to bend in the "wind". |