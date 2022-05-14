#remove died tag
tag @a[x=0,y=50,z=0,distance=0..5] remove died
#set spawn to black box
spawnpoint @a[team=!,tag=ingame] 0 51 0
#clear poison
effect clear @a[x=0,y=50,z=0,distance=0..5,gamemode=adventure] poison
#spawn invulnerability
effect give @a[x=0,y=50,z=0,distance=0..5,gamemode=adventure] minecraft:resistance 5 255 true
#tp teams
execute as @e[type=armor_stand,tag=teamspawn,tag=color,tag=orange] run tp @a[x=0,y=50,z=0,distance=0..5,team=orange] @s
execute as @e[type=armor_stand,tag=teamspawn,tag=color,tag=purple] run tp @a[x=0,y=50,z=0,distance=0..5,team=purple] @s
execute as @e[type=armor_stand,tag=teamspawn,tag=color,tag=lightblue] run tp @a[x=0,y=50,z=0,distance=0..5,team=lightblue] @s
execute as @e[type=armor_stand,tag=teamspawn,tag=color,tag=gray] run tp @a[x=0,y=50,z=0,distance=0..5,team=gray] @s
execute as @e[type=armor_stand,tag=teamspawn,tag=color,tag=blue] run tp @a[x=0,y=50,z=0,distance=0..5,team=blue] @s
execute as @e[type=armor_stand,tag=teamspawn,tag=color,tag=green] run tp @a[x=0,y=50,z=0,distance=0..5,team=green] @s
execute as @e[type=armor_stand,tag=teamspawn,tag=color,tag=red] run tp @a[x=0,y=50,z=0,distance=0..5,team=red] @s
execute as @e[type=armor_stand,tag=teamspawn,tag=color,tag=yellow] run tp @a[x=0,y=50,z=0,distance=0..5,team=yellow] @s
#tp everyone else
tp @a[x=0,y=50,z=0,distance=0..5,team=] 0 80 0
#keep spectators in arena
tp @a[tag=spectator,tag=!inarena] 0 80 0