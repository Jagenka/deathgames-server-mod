#add time on platform for players on platform
execute if score bonus1 gamestate matches 1 if score bonustodo gamestate matches 1 run scoreboard players add @a[x=-122,y=62,z=-122,dx=4,dy=4,dz=4,tag=ingame] bonustime 1
execute if score bonus2 gamestate matches 1 if score bonustodo gamestate matches 1 run scoreboard players add @a[x=117,y=63,z=-122,dx=4,dy=4,dz=4,tag=ingame] bonustime 1
execute if score bonus3 gamestate matches 1 if score bonustodo gamestate matches 1 run scoreboard players add @a[x=117,y=70,z=117,dx=4,dy=4,dz=4,tag=ingame] bonustime 1
execute if score bonus4 gamestate matches 1 if score bonustodo gamestate matches 1 run scoreboard players add @a[x=-122,y=68,z=117,dx=4,dy=4,dz=4,tag=ingame] bonustime 1
#give money after interval is reached
execute as @a if score @s bonustime >= bonusinterval settings run scoreboard players operation @s money += bonusmoney settings
execute as @a if score @s bonustime >= bonusinterval settings run scoreboard players operation @s bonustime -= bonusinterval settings
#count up global bonus timer
scoreboard players add bonus timer 1

#calc time to bonus in seconds
scoreboard players operation timetobonussec timer = bonusspawn settings
scoreboard players operation timetobonussec timer -= bonus timer
#round up
scoreboard players operation timetobonussec timer += 19 constants
scoreboard players operation timetobonussec timer /= 20 constants

#calc time to bonus in seconds
scoreboard players operation timetobonusgone timer = bonussum timer
scoreboard players operation timetobonusgone timer -= bonus timer
#round up
scoreboard players operation timetobonusgone timer += 19 constants
scoreboard players operation timetobonusgone timer /= 20 constants

#if over threshold -> spawn bonus platform
execute unless score bonustodo gamestate matches 1.. if score bonus timer >= bonusspawn settings run function deathgames:bonus_spawn
#if over combined threshold -> despawn bonus platform
execute if score bonus timer >= bonussum timer run function deathgames:bonus_despawn
#display where bonus will be
execute if score bonus1 gamestate matches 1 if score bonustodo gamestate matches 0 as @a run title @s actionbar [{"text":"Bonus Money Platform: Desert Corner in ","bold":true,"color":"dark_red"},{"score":{"name":"timetobonussec","objective":"timer"},"bold":true,"color":"dark_red"},{"text":"sec","bold":true,"color":"dark_red"}]
execute if score bonus2 gamestate matches 1 if score bonustodo gamestate matches 0 as @a run title @s actionbar [{"text":"Bonus Money Platform: Water Corner in ","bold":true,"color":"dark_red"},{"score":{"name":"timetobonussec","objective":"timer"},"bold":true,"color":"dark_red"},{"text":"sec","bold":true,"color":"dark_red"}]
execute if score bonus3 gamestate matches 1 if score bonustodo gamestate matches 0 as @a run title @s actionbar [{"text":"Bonus Money Platform: Village Corner in ","bold":true,"color":"dark_red"},{"score":{"name":"timetobonussec","objective":"timer"},"bold":true,"color":"dark_red"},{"text":"sec","bold":true,"color":"dark_red"}]
execute if score bonus4 gamestate matches 1 if score bonustodo gamestate matches 0 as @a run title @s actionbar [{"text":"Bonus Money Platform: Nether Corner in ","bold":true,"color":"dark_red"},{"score":{"name":"timetobonussec","objective":"timer"},"bold":true,"color":"dark_red"},{"text":"sec","bold":true,"color":"dark_red"}]
#display where bonus currenly is
execute if score bonus1 gamestate matches 1 if score bonustodo gamestate matches 1 as @a run title @s actionbar [{"text":"Bonus Money Platform: Desert Corner for another ","bold":true,"color":"dark_green"},{"score":{"name":"timetobonusgone","objective":"timer"},"bold":true,"color":"dark_green"},{"text":"sec","bold":true,"color":"dark_green"}]
execute if score bonus2 gamestate matches 1 if score bonustodo gamestate matches 1 as @a run title @s actionbar [{"text":"Bonus Money Platform: Water Corner for another ","bold":true,"color":"dark_green"},{"score":{"name":"timetobonusgone","objective":"timer"},"bold":true,"color":"dark_green"},{"text":"sec","bold":true,"color":"dark_green"}]
execute if score bonus3 gamestate matches 1 if score bonustodo gamestate matches 1 as @a run title @s actionbar [{"text":"Bonus Money Platform: Village Corner for another ","bold":true,"color":"dark_green"},{"score":{"name":"timetobonusgone","objective":"timer"},"bold":true,"color":"dark_green"},{"text":"sec","bold":true,"color":"dark_green"}]
execute if score bonus4 gamestate matches 1 if score bonustodo gamestate matches 1 as @a run title @s actionbar [{"text":"Bonus Money Platform: Nether Corner for another ","bold":true,"color":"dark_green"},{"score":{"name":"timetobonusgone","objective":"timer"},"bold":true,"color":"dark_green"},{"text":"sec","bold":true,"color":"dark_green"}]
