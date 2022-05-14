#spawn active particles
execute as @e[name=stp] if score @s snaretrap-chan matches 200 at @s run particle minecraft:nautilus ~ ~-0.015 ~ 0 0 0 0 0 force @a[distance=..10]

#not gay management
#spawn setup particles
execute as @e[name=stp] if score @s snaretrap-chan matches 0..199 at @s run particle minecraft:crit ~ ~0.2 ~ 0.05 0.1 0.05 0.1 1 force @a[distance=..10]
#init if not set
execute as @e[name=stp] unless score @s snaretrap-chan matches 0..200 run scoreboard players set @s snaretrap-chan 0
#increment value
execute as @e[name=stp] if score @s snaretrap-chan matches ..199 run scoreboard players add @s snaretrap-chan 1

#not gay creator
#summon stp armor_stand
execute as @e[tag=simpletrap] at @s run summon minecraft:armor_stand ~ ~ ~ {Invisible:1b,Marker:1b,CustomName:'{"text":"stp"}'}
#tp bat way down low
execute as @e[tag=simpletrap] at @s run tp @s ~ ~-200 ~
#kill bat
execute as @e[tag=simpletrap] at @s run kill @s

#not gay checker
#give trapee effects
execute as @e[name=stp] at @s if score @s snaretrap-chan matches 200 if entity @p[distance=..1,gamemode=adventure] run effect give @p[distance=..1,gamemode=adventure] minecraft:slowness 6 100 true
execute as @e[name=stp] at @s if score @s snaretrap-chan matches 200 if entity @p[distance=..1,gamemode=adventure] run effect give @p[distance=..1,gamemode=adventure] minecraft:blindness 7 5 true
execute as @e[name=stp] at @s if score @s snaretrap-chan matches 200 if entity @p[distance=..1,gamemode=adventure] run playsound minecraft:entity.iron_golem.damage master @a[distance=..20] ~ ~ ~ 1 1 0
execute as @e[name=stp] at @s if score @s snaretrap-chan matches 200 if entity @p[distance=..1,gamemode=adventure] run particle minecraft:large_smoke ~ ~ ~ 0 0 0 0.05 50 force @a[distance=..30]
#remove trap
execute as @e[name=stp] at @s if score @s snaretrap-chan matches 200 if entity @p[distance=..1,gamemode=adventure] run kill @s