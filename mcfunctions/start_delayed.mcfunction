#remove stuck arrows
kill @e[type=minecraft:arrow]
#remove dropped items
kill @e[type=minecraft:item]

#update shop display
function deathgames:shopsigns
function deathgames:shopdisplay

#show timesincelastkill bossbar
bossbar set minecraft:timesincelastkill players @a

#kill ginos traps
kill @e[type=minecraft:armor_stand,name=stp]

#choose random bonus platform
function deathgames:calc_nextbonus

#show begin message
title @a times 20 40 20
title @a title "GLHF"

#finally, start the game
scoreboard players set running gamestate 1
execute store result storage minecraft:deathgames game_running byte 1 run scoreboard players get 1 constants