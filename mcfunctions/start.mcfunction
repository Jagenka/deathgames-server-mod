#forceload some chunks
forceload add -10 -10 10 10
forceload add -42 -95 -34 -87
forceload add 34 -95 42 -87
forceload add 87 -42 95 -34
forceload add 87 34 95 42
forceload add 34 87 42 95
forceload add -42 87 -34 95
forceload add -95 34 -87 42
forceload add -95 -42 -87 -34

#calculate lives per player
scoreboard players reset * lives
execute as @a[tag=!admin,team=orange] run scoreboard players add orange lives 1
execute as @a[tag=!admin,team=purple] run scoreboard players add purple lives 1
execute as @a[tag=!admin,team=lightblue] run scoreboard players add lightblue lives 1
execute as @a[tag=!admin,team=gray] run scoreboard players add gray lives 1
execute as @a[tag=!admin,team=blue] run scoreboard players add blue lives 1
execute as @a[tag=!admin,team=green] run scoreboard players add green lives 1
execute as @a[tag=!admin,team=red] run scoreboard players add red lives 1
execute as @a[tag=!admin,team=yellow] run scoreboard players add yellow lives 1
scoreboard players operation orange lives *= livesperplayer settings
scoreboard players operation purple lives *= livesperplayer settings
scoreboard players operation lightblue lives *= livesperplayer settings
scoreboard players operation gray lives *= livesperplayer settings
scoreboard players operation blue lives *= livesperplayer settings
scoreboard players operation green lives *= livesperplayer settings
scoreboard players operation red lives *= livesperplayer settings
scoreboard players operation yellow lives *= livesperplayer settings

#reset platforms in corners
execute positioned -122 62 -122 run fill ~ ~ ~ ~4 ~ ~4 red_concrete
execute positioned 117 63 -122 run fill ~ ~ ~ ~4 ~ ~4 red_concrete
execute positioned 117 70 117 run fill ~ ~ ~ ~4 ~ ~4 red_concrete
execute positioned -122 68 117 run fill ~ ~ ~ ~4 ~ ~4 red_concrete

#reset some scores
scoreboard players reset * pretty_lives
scoreboard players reset * killstreak
scoreboard players reset * livesshop
scoreboard players reset * teleport
scoreboard players reset * tptimer
scoreboard players reset * shop
scoreboard players reset * kills
scoreboard players reset * deaths
scoreboard players reset * bonustime
scoreboard players reset * refundrequest
scoreboard players reset * deathpenalty
scoreboard players set bonus1 gamestate 0
scoreboard players set bonus2 gamestate 0
scoreboard players set bonus3 gamestate 0
scoreboard players set bonus4 gamestate 0
scoreboard players set bonustodo gamestate 0
scoreboard players set money timer 0
scoreboard players set shuffle timer 0
scoreboard players set shufflespawns gamestate 0
#timesincelastkill delay at start
scoreboard players set timesincelastkill timer 0
scoreboard players operation timesincelastkill timer -= startglowdelay settings
#reset for offline players
scoreboard players reset * armor
scoreboard players reset * sword
scoreboard players reset * axe
scoreboard players reset * bow
scoreboard players reset * crossbow
#set for online players. shop won't work otherwise
scoreboard players set @a armor 0
scoreboard players set @a sword 0
scoreboard players set @a axe 0
scoreboard players set @a bow 0
scoreboard players set @a crossbow 0

#reset xp bar
xp set @a 0 points

#calculate sum for bonus timing
scoreboard players operation bonussum timer = bonusspawn settings
scoreboard players operation bonussum timer += bonusstay settings

#starting timer for earlier first bonus
scoreboard players operation bonus timer = bonusspawn settings
scoreboard players operation bonus timer -= bonusbegin settings

#manage beginning tags
tag @a[tag=!admin,team=] add spectator
tag @a[tag=!admin,team=!] add ingame
tag @a remove died

#give starting money
scoreboard players reset * money
execute as @a[team=!] run scoreboard players operation @s money = startmoney settings

#prepare players
clear @a[tag=!admin,tag=ingame]
effect clear @a[tag=!admin]
effect give @a[tag=!admin] minecraft:instant_health 1 100 true
effect give @a[tag=!admin] minecraft:saturation 1 255 true

#determine who is eliminated or who is not
scoreboard players set orange eliminated 1
scoreboard players set purple eliminated 1
scoreboard players set lightblue eliminated 1
scoreboard players set gray eliminated 1
scoreboard players set blue eliminated 1
scoreboard players set green eliminated 1
scoreboard players set red eliminated 1
scoreboard players set yellow eliminated 1
execute as @r[tag=!admin,team=orange] run scoreboard players set orange eliminated 0
execute as @r[tag=!admin,team=purple] run scoreboard players set purple eliminated 0
execute as @r[tag=!admin,team=lightblue] run scoreboard players set lightblue eliminated 0
execute as @r[tag=!admin,team=gray] run scoreboard players set gray eliminated 0
execute as @r[tag=!admin,team=blue] run scoreboard players set blue eliminated 0
execute as @r[tag=!admin,team=green] run scoreboard players set green eliminated 0
execute as @r[tag=!admin,team=red] run scoreboard players set red eliminated 0
execute as @r[tag=!admin,team=yellow] run scoreboard players set yellow eliminated 0

#clear weather
weather clear 1000000

#teleport players to map
tp @a[tag=!admin] 0 50 0

#spawn fix spawn armorstands
kill @e[type=minecraft:armor_stand,tag=teamspawn,tag=spawn]
summon minecraft:armor_stand -38 63 -91 {Marker:1b,Invisible:1b,Tags:["teamspawn","spawn","spawn1"],Rotation:[0f,0f]}
summon minecraft:armor_stand 38 63 -91 {Marker:1b,Invisible:1b,Tags:["teamspawn","spawn","spawn2"],Rotation:[0f,0f]}
summon minecraft:armor_stand 91 63 -38 {Marker:1b,Invisible:1b,Tags:["teamspawn","spawn","spawn3"],Rotation:[90f,0f]}
summon minecraft:armor_stand 91 72 38 {Marker:1b,Invisible:1b,Tags:["teamspawn","spawn","spawn4"],Rotation:[90f,0f]}
summon minecraft:armor_stand 38 72 91 {Marker:1b,Invisible:1b,Tags:["teamspawn","spawn","spawn5"],Rotation:[180f,0f]}
summon minecraft:armor_stand -38 72 91 {Marker:1b,Invisible:1b,Tags:["teamspawn","spawn","spawn6"],Rotation:[180f,0f]}
summon minecraft:armor_stand -91 70 38 {Marker:1b,Invisible:1b,Tags:["teamspawn","spawn","spawn7"],Rotation:[270f,0f]}
summon minecraft:armor_stand -91 63 -38 {Marker:1b,Invisible:1b,Tags:["teamspawn","spawn","spawn8"],Rotation:[270f,0f]}

#prepare player spawns
kill @e[type=minecraft:armor_stand,tag=teamspawn,tag=color]
execute as @r[tag=!admin,team=orange] run summon minecraft:armor_stand 0 80 0 {Marker:1b,Invisible:1b,Tags:["teamspawn","color","orange"]}
execute as @r[tag=!admin,team=purple] run summon minecraft:armor_stand 0 80 0 {Marker:1b,Invisible:1b,Tags:["teamspawn","color","purple"]}
execute as @r[tag=!admin,team=green] run summon minecraft:armor_stand 0 80 0 {Marker:1b,Invisible:1b,Tags:["teamspawn","color","green"]}
execute as @r[tag=!admin,team=red] run summon minecraft:armor_stand 0 80 0 {Marker:1b,Invisible:1b,Tags:["teamspawn","color","red"]}
execute as @r[tag=!admin,team=yellow] run summon minecraft:armor_stand 0 80 0 {Marker:1b,Invisible:1b,Tags:["teamspawn","color","yellow"]}
execute as @r[tag=!admin,team=blue] run summon minecraft:armor_stand 0 80 0 {Marker:1b,Invisible:1b,Tags:["teamspawn","color","blue"]}
execute as @r[tag=!admin,team=gray] run summon minecraft:armor_stand 0 80 0 {Marker:1b,Invisible:1b,Tags:["teamspawn","color","gray"]}
execute as @r[tag=!admin,team=lightblue] run summon minecraft:armor_stand 0 80 0 {Marker:1b,Invisible:1b,Tags:["teamspawn","color","lightblue"]}

#change running state temporarily
execute store result storage minecraft:deathgames game_running byte 1 run scoreboard players get 69 constants

#shuffle spawns
#schedule function deathgames:shufflespawns 10t
function deathgames:shufflespawns

#schedule delayed code
#schedule function deathgames:start_delayed 20t
function deathgames:start_delayed