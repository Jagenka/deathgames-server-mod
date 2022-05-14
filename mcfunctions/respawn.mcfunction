#dead players not ingame for now, may be changed later in function depending on lives
tag @a[team=!,scores={deaths=1..}] remove ingame
#make dead player spectator at first so that in tags_and_gamemodes players can respawn - kinda confused about this # moved to tags_and_gamemodes
tag @a[team=!,tag=!ingame] add spectator
#dead players might have been killed -> will be managed in kills
tag @a[team=!,scores={deaths=1..}] add killed
#tag dead player as died - gets reset in blackbox and at game start
tag @a[team=!,scores={deaths=1..}] add died

#reset success value
execute store result storage deathgames respawn_orange int 1 run scoreboard players get 0 constants
execute store result storage deathgames respawn_purple int 1 run scoreboard players get 0 constants
execute store result storage deathgames respawn_lightblue int 1 run scoreboard players get 0 constants
execute store result storage deathgames respawn_gray int 1 run scoreboard players get 0 constants
execute store result storage deathgames respawn_blue int 1 run scoreboard players get 0 constants
execute store result storage deathgames respawn_green int 1 run scoreboard players get 0 constants
execute store result storage deathgames respawn_red int 1 run scoreboard players get 0 constants
execute store result storage deathgames respawn_yellow int 1 run scoreboard players get 0 constants

#respawn players if lives > 0
execute store success storage deathgames respawn_orange int 1 if score orange lives matches 1.. run tag @r[team=orange,tag=!ingame] add ingame
execute store success storage deathgames respawn_purple int 1 if score purple lives matches 1.. run tag @r[team=purple,tag=!ingame] add ingame
execute store success storage deathgames respawn_lightblue int 1 if score lightblue lives matches 1.. run tag @r[team=lightblue,tag=!ingame] add ingame
execute store success storage deathgames respawn_gray int 1 if score gray lives matches 1.. run tag @r[team=gray,tag=!ingame] add ingame
execute store success storage deathgames respawn_blue int 1 if score blue lives matches 1.. run tag @r[team=blue,tag=!ingame] add ingame
execute store success storage deathgames respawn_green int 1 if score green lives matches 1.. run tag @r[team=green,tag=!ingame] add ingame
execute store success storage deathgames respawn_red int 1 if score red lives matches 1.. run tag @r[team=red,tag=!ingame] add ingame
execute store success storage deathgames respawn_yellow int 1 if score yellow lives matches 1.. run tag @r[team=yellow,tag=!ingame] add ingame

#remove lives if a player respawned and phase says so
execute if data storage deathgames {respawn_orange:1} run scoreboard players remove orange lives 1
execute if data storage deathgames {respawn_purple:1} run scoreboard players remove purple lives 1
execute if data storage deathgames {respawn_lightblue:1} run scoreboard players remove lightblue lives 1
execute if data storage deathgames {respawn_gray:1} run scoreboard players remove gray lives 1
execute if data storage deathgames {respawn_blue:1} run scoreboard players remove blue lives 1
execute if data storage deathgames {respawn_green:1} run scoreboard players remove green lives 1
execute if data storage deathgames {respawn_red:1} run scoreboard players remove red lives 1
execute if data storage deathgames {respawn_yellow:1} run scoreboard players remove yellow lives 1

#that's all we have to do for deaths
scoreboard players remove @a[scores={deaths=1..}] deaths 1

#tp respawning players to black box
tp @a[tag=ingame,tag=spectator,tag=!died] 0 50 0
tag @a[tag=ingame,tag=spectator,tag=!died] remove spectator

#manage kills
function deathgames:kills
#manage if teams do be eliminated
function deathgames:eliminated


#debug
#execute as @r[team=orange,tag=!ingame] run tellraw @a [{"selector":"@s"}]