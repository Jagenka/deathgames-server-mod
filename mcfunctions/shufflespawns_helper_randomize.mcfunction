scoreboard players reset * shufflespawns

#remove spawn association
execute as @e[type=armor_stand,tag=teamspawn,tag=color] run tag @s remove spawn1
execute as @e[type=armor_stand,tag=teamspawn,tag=color] run tag @s remove spawn2
execute as @e[type=armor_stand,tag=teamspawn,tag=color] run tag @s remove spawn3
execute as @e[type=armor_stand,tag=teamspawn,tag=color] run tag @s remove spawn4
execute as @e[type=armor_stand,tag=teamspawn,tag=color] run tag @s remove spawn5
execute as @e[type=armor_stand,tag=teamspawn,tag=color] run tag @s remove spawn6
execute as @e[type=armor_stand,tag=teamspawn,tag=color] run tag @s remove spawn7
execute as @e[type=armor_stand,tag=teamspawn,tag=color] run tag @s remove spawn8

#8 is free spawn
scoreboard players set spawn1 shufflespawns 8
scoreboard players set spawn2 shufflespawns 8
scoreboard players set spawn3 shufflespawns 8
scoreboard players set spawn4 shufflespawns 8
scoreboard players set spawn5 shufflespawns 8
scoreboard players set spawn6 shufflespawns 8
scoreboard players set spawn7 shufflespawns 8
scoreboard players set spawn8 shufflespawns 8

#%= does exclusive range 0..7

execute as @r[scores={walked=1..}] run scoreboard players operation rnd shufflespawns = @s walked
scoreboard players operation rnd shufflespawns %= 8 constants
#change walked stat maybe
#shift and mark
function deathgames:shufflespawns_helper_mark
#tag color armor_stand
execute as @e[type=armor_stand,tag=teamspawn,tag=color,tag=orange] run function deathgames:shufflespawns_helper_tagstand

execute as @r[scores={walked=1..}] run scoreboard players operation rnd shufflespawns = @s walked
scoreboard players operation rnd shufflespawns %= 7 constants
#change walked stat maybe
#shift and mark
function deathgames:shufflespawns_helper_mark
#tag color armor_stand
execute as @e[type=armor_stand,tag=teamspawn,tag=color,tag=purple] run function deathgames:shufflespawns_helper_tagstand

execute as @r[scores={walked=1..}] run scoreboard players operation rnd shufflespawns = @s walked
scoreboard players operation rnd shufflespawns %= 6 constants
#change walked stat maybe
#shift and mark
function deathgames:shufflespawns_helper_mark
#tag color armor_stand
execute as @e[type=armor_stand,tag=teamspawn,tag=color,tag=green] run function deathgames:shufflespawns_helper_tagstand

execute as @r[scores={walked=1..}] run scoreboard players operation rnd shufflespawns = @s walked
scoreboard players operation rnd shufflespawns %= 5 constants
#change walked stat maybe
#shift and mark
function deathgames:shufflespawns_helper_mark
#tag color armor_stand
execute as @e[type=armor_stand,tag=teamspawn,tag=color,tag=red] run function deathgames:shufflespawns_helper_tagstand

execute as @r[scores={walked=1..}] run scoreboard players operation rnd shufflespawns = @s walked
scoreboard players operation rnd shufflespawns %= 4 constants
#change walked stat maybe
#shift and mark
function deathgames:shufflespawns_helper_mark
#tag color armor_stand
execute as @e[type=armor_stand,tag=teamspawn,tag=color,tag=yellow] run function deathgames:shufflespawns_helper_tagstand

execute as @r[scores={walked=1..}] run scoreboard players operation rnd shufflespawns = @s walked
scoreboard players operation rnd shufflespawns %= 3 constants
#change walked stat maybe
#shift and mark
function deathgames:shufflespawns_helper_mark
#tag color armor_stand
execute as @e[type=armor_stand,tag=teamspawn,tag=color,tag=blue] run function deathgames:shufflespawns_helper_tagstand

execute as @r[scores={walked=1..}] run scoreboard players operation rnd shufflespawns = @s walked
scoreboard players operation rnd shufflespawns %= 2 constants
#change walked stat maybe
#shift and mark
function deathgames:shufflespawns_helper_mark
#tag color armor_stand
execute as @e[type=armor_stand,tag=teamspawn,tag=color,tag=gray] run function deathgames:shufflespawns_helper_tagstand

#remaining 8th spawn
scoreboard players set rnd shufflespawns 0
#shift and mark
function deathgames:shufflespawns_helper_mark
#tag color armor_stand
execute as @e[type=armor_stand,tag=teamspawn,tag=color,tag=lightblue] run function deathgames:shufflespawns_helper_tagstand