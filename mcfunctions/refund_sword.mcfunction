scoreboard players set @s refund 0
execute if score @s sword matches 2 run scoreboard players operation @s refund = sword1 upgradesum
execute if score @s sword matches 4 run scoreboard players operation @s refund = sword2 upgradesum
execute if score @s sword matches 6 run scoreboard players operation @s refund = sword3 upgradesum
execute if score @s sword matches 8 run scoreboard players operation @s refund = sword4 upgradesum
execute if score @s sword matches 10 run scoreboard players operation @s refund = sword5 upgradesum
execute if score @s sword matches 12 run scoreboard players operation @s refund = sword6 upgradesum
scoreboard players operation @s refund *= refund% settings
scoreboard players operation @s refund /= 100 constants
#round down
scoreboard players operation @s refund /= 5 constants
scoreboard players operation @s refund *= 5 constants
#refund done
scoreboard players set @s refundrequest 0
#give money
scoreboard players operation @s money += @s refund
#reset sword
scoreboard players set @s sword -1