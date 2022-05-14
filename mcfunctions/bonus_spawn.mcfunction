#message in chat
execute if score bonus1 gamestate matches 1 run tellraw @a "Desert Corner Platform = Bonus Money"
execute if score bonus2 gamestate matches 1 run tellraw @a "Water Corner Platform = Bonus Money"
execute if score bonus3 gamestate matches 1 run tellraw @a "Village Corner Platform = Bonus Money"
execute if score bonus4 gamestate matches 1 run tellraw @a "Nether Corner Platform = Bonus Money"
#recolor platforms
execute if score bonus1 gamestate matches 1 positioned -122 62 -122 run fill ~ ~ ~ ~4 ~ ~4 lime_concrete
execute if score bonus2 gamestate matches 1 positioned 117 63 -122 run fill ~ ~ ~ ~4 ~ ~4 lime_concrete
execute if score bonus3 gamestate matches 1 positioned 117 70 117 run fill ~ ~ ~ ~4 ~ ~4 lime_concrete
execute if score bonus4 gamestate matches 1 positioned -122 68 117 run fill ~ ~ ~ ~4 ~ ~4 lime_concrete
#done spawning
scoreboard players set bonustodo gamestate 1