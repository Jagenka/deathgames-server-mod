scoreboard players set @s refund 0
execute if score @s axe matches 2 run scoreboard players operation @s refund = axe1 upgradesum
execute if score @s axe matches 4 run scoreboard players operation @s refund = axe2 upgradesum
execute if score @s axe matches 6 run scoreboard players operation @s refund = axe3 upgradesum
execute if score @s axe matches 8 run scoreboard players operation @s refund = axe4 upgradesum
execute if score @s axe matches 10 run scoreboard players operation @s refund = axe5 upgradesum
scoreboard players operation @s refund *= refund% settings
scoreboard players operation @s refund /= 100 constants
#round down
scoreboard players operation @s refund /= 5 constants
scoreboard players operation @s refund *= 5 constants
#refund done
scoreboard players set @s refundrequest 0
#give money
scoreboard players operation @s money += @s refund
#reset axe
scoreboard players set @s axe -1