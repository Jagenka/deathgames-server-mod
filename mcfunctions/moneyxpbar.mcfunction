#get current xp
execute as @a[tag=ingame] store result score @s xp run xp query @s levels
#calc xp difference
execute as @a[tag=ingame] run scoreboard players operation @s xpdifference = @s money
execute as @a[tag=ingame] run scoreboard players operation @s xpdifference -= @s xp
#add levels
execute as @a[tag=ingame] if score @s xpdifference matches 88.. run xp add @s 50 levels
execute as @a[tag=ingame] if score @s xpdifference matches 38.. run xp add @s 20 levels
execute as @a[tag=ingame] if score @s xpdifference matches 18.. run xp add @s 10 levels
execute as @a[tag=ingame] if score @s xpdifference matches 8.. run xp add @s 5 levels
execute as @a[tag=ingame] if score @s xpdifference matches 3.. run xp add @s 2 levels
execute as @a[tag=ingame] if score @s xpdifference matches 1.. run xp add @s 1 levels
#subtract levels
execute as @a[tag=ingame] if score @s xpdifference matches ..-88 run xp add @s -50 levels
execute as @a[tag=ingame] if score @s xpdifference matches ..-38 run xp add @s -20 levels
execute as @a[tag=ingame] if score @s xpdifference matches ..-18 run xp add @s -10 levels
execute as @a[tag=ingame] if score @s xpdifference matches ..-8 run xp add @s -5 levels
execute as @a[tag=ingame] if score @s xpdifference matches ..-3 run xp add @s -2 levels
execute as @a[tag=ingame] if score @s xpdifference matches ..-1 run xp add @s -1 levels