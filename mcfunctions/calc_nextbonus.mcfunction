#randomize
execute as @r[scores={walked=1..},tag=ingame] run scoreboard players operation bonusrnd gamestate = @s walked
scoreboard players operation bonusrnd gamestate %= 4 constants
#assign bonusrnd to bonusx
execute if score bonusrnd gamestate matches 0 run scoreboard players set bonus1 gamestate 1
execute if score bonusrnd gamestate matches 1 run scoreboard players set bonus2 gamestate 1
execute if score bonusrnd gamestate matches 2 run scoreboard players set bonus3 gamestate 1
execute if score bonusrnd gamestate matches 3 run scoreboard players set bonus4 gamestate 1