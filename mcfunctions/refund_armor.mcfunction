scoreboard players set @s refund 0
execute if score @s armor matches 2 run scoreboard players operation @s refund = armor1 upgradesum
execute if score @s armor matches 4 run scoreboard players operation @s refund = armor2 upgradesum
execute if score @s armor matches 6 run scoreboard players operation @s refund = armor3 upgradesum
execute if score @s armor matches 8 run scoreboard players operation @s refund = armor4 upgradesum
execute if score @s armor matches 10 run scoreboard players operation @s refund = armor5 upgradesum
execute if score @s armor matches 12 run scoreboard players operation @s refund = armor6 upgradesum
scoreboard players operation @s refund *= refund% settings
scoreboard players operation @s refund /= 100 constants
#round down
scoreboard players operation @s refund /= 5 constants
scoreboard players operation @s refund *= 5 constants
#refund done
scoreboard players set @s refundrequest 0
#give money
scoreboard players operation @s money += @s refund
#reset armor
scoreboard players set @s armor -1