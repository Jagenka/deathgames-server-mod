#location tags
tag @a remove inarena
tag @a[x=-128,y=48,z=-128,dx=255,dy=103,dz=255] add inarena
tag @a remove inshop
tag @a[x=-7,y=55,z=-7,dx=14,dy=6,dz=14] add inshop
tag @a remove inlobby
tag @a[x=-29,y=17,z=-29,dx=58,dy=18,dz=58] add inlobby

#if game running run game logic
execute if data storage minecraft:deathgames {game_running:1b} run function deathgames:main_ingame

#tp to spawn if game is not running
execute if data storage minecraft:deathgames {game_running:0b} run tp @a[tag=!admin,tag=inarena] 0 42 0
#set spawnpoint if game not running
execute if data storage minecraft:deathgames {game_running:0b} run spawnpoint @a 0 42 0

#stuff to do in spawn box
gamemode adventure @a[x=0,y=42,z=0,distance=0..4]
tag @a[x=0,y=42,z=0,distance=0..4] remove spectator
tag @a[x=0,y=42,z=0,distance=0..4] remove ingame
effect clear @a[x=0,y=42,z=0,distance=0..4]
clear @a[x=0,y=42,z=0,distance=0..4]
tp @a[x=0,y=42,z=0,distance=0..4] 0 20 0

#lobby stuffs
function deathgames:lobby

#not gay stuffs
function deathgames:gino