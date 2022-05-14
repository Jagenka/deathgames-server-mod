#reset global bonus timer
scoreboard players set bonus timer 0
#disable all bonuses
scoreboard players set bonus1 gamestate 0
scoreboard players set bonus2 gamestate 0
scoreboard players set bonus3 gamestate 0
scoreboard players set bonus4 gamestate 0
#message in chat
tellraw @a "Bonus has disappeared."
#recolor platforms
execute positioned -122 62 -122 run fill ~ ~ ~ ~4 ~ ~4 red_concrete
execute positioned 117 63 -122 run fill ~ ~ ~ ~4 ~ ~4 red_concrete
execute positioned 117 70 117 run fill ~ ~ ~ ~4 ~ ~4 red_concrete
execute positioned -122 68 117 run fill ~ ~ ~ ~4 ~ ~4 red_concrete
#choose random bonus platform
function deathgames:calc_nextbonus
#done despawning
scoreboard players set bonustodo gamestate 0