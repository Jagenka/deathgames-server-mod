scoreboard players set @s refund 0
execute if score @s bow matches 2 run scoreboard players operation @s refund = bow1 upgradesum
execute if score @s bow matches 4 run scoreboard players operation @s refund = bow2 upgradesum
execute if score @s bow matches 6 run scoreboard players operation @s refund = bow3 upgradesum
execute if score @s bow matches 8 run scoreboard players operation @s refund = bow4 upgradesum
execute if score @s bow matches 10 run scoreboard players operation @s refund = bow5 upgradesum
execute if score @s bow matches 12 run scoreboard players operation @s refund = bow6 upgradesum
scoreboard players operation @s refund *= refund% settings
scoreboard players operation @s refund /= 100 constants
#round down
scoreboard players operation @s refund /= 5 constants
scoreboard players operation @s refund *= 5 constants
#refund done
scoreboard players set @s refundrequest 0
#give money
scoreboard players operation @s money += @s refund
#reset bow
scoreboard players set @s bow -1