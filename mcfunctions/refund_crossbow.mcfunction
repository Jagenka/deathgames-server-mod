scoreboard players set @s refund 0
execute if score @s crossbow matches 2 run scoreboard players operation @s refund = crossbow1 upgradesum
execute if score @s crossbow matches 4 run scoreboard players operation @s refund = crossbow2 upgradesum
execute if score @s crossbow matches 6 run scoreboard players operation @s refund = crossbow3 upgradesum
execute if score @s crossbow matches 8 run scoreboard players operation @s refund = crossbow4 upgradesum
execute if score @s crossbow matches 10 run scoreboard players operation @s refund = crossbow5 upgradesum
execute if score @s crossbow matches 12 run scoreboard players operation @s refund = crossbow6 upgradesum
scoreboard players operation @s refund *= refund% settings
scoreboard players operation @s refund /= 100 constants
#round down
scoreboard players operation @s refund /= 5 constants
scoreboard players operation @s refund *= 5 constants
#refund done
scoreboard players set @s refundrequest 0
#give money
scoreboard players operation @s money += @s refund
#reset crossbow
scoreboard players set @s crossbow -1