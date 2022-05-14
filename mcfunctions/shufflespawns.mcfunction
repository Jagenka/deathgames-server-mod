#mark all spawns as free
#execute as @e[type=armor_stand,tag=teamspawn,tag=spawn] run tag @s add free
#tp all color armor stands to center
execute as @e[type=armor_stand,tag=teamspawn,tag=color] run tp @s 0 80 0
#randomize spawns
function deathgames:shufflespawns_helper_randomize
#tp armor_stands
tp @e[type=armor_stand,tag=teamspawn,tag=color,tag=spawn1] @e[type=armor_stand,tag=teamspawn,tag=spawn1,limit=1]
tp @e[type=armor_stand,tag=teamspawn,tag=color,tag=spawn2] @e[type=armor_stand,tag=teamspawn,tag=spawn2,limit=1]
tp @e[type=armor_stand,tag=teamspawn,tag=color,tag=spawn3] @e[type=armor_stand,tag=teamspawn,tag=spawn3,limit=1]
tp @e[type=armor_stand,tag=teamspawn,tag=color,tag=spawn4] @e[type=armor_stand,tag=teamspawn,tag=spawn4,limit=1]
tp @e[type=armor_stand,tag=teamspawn,tag=color,tag=spawn5] @e[type=armor_stand,tag=teamspawn,tag=spawn5,limit=1]
tp @e[type=armor_stand,tag=teamspawn,tag=color,tag=spawn6] @e[type=armor_stand,tag=teamspawn,tag=spawn6,limit=1]
tp @e[type=armor_stand,tag=teamspawn,tag=color,tag=spawn7] @e[type=armor_stand,tag=teamspawn,tag=spawn7,limit=1]
tp @e[type=armor_stand,tag=teamspawn,tag=color,tag=spawn8] @e[type=armor_stand,tag=teamspawn,tag=spawn8,limit=1]
#color spawns
schedule function deathgames:shufflespawns_helper_color 5t
#shuffle notification only if game running, cause shuffling is being run from start function
execute if score running gamestate matches 1 run tellraw @a "Spawns shuffled!"