#reset score for alive players
scoreboard players set @a[tag=!died] deathpenalty 0
#count up for dead players
scoreboard players add @a[tag=died] deathpenalty 1
#message to player
execute as @a[tag=died] if score @s deathpenalty >= deathpenalty settings run tellraw @s "You just lost a life due to not respawning"

#game over if already at 0 lives
execute as @a[tag=died,team=orange,tag=ingame] if score @s deathpenalty >= deathpenalty settings if score orange lives matches ..0 run tag @s remove ingame
execute as @a[tag=died,team=purple,tag=ingame] if score @s deathpenalty >= deathpenalty settings if score purple lives matches ..0 run tag @s remove ingame
execute as @a[tag=died,team=lightblue,tag=ingame] if score @s deathpenalty >= deathpenalty settings if score lightblue lives matches ..0 run tag @s remove ingame
execute as @a[tag=died,team=gray,tag=ingame] if score @s deathpenalty >= deathpenalty settings if score gray lives matches ..0 run tag @s remove ingame
execute as @a[tag=died,team=blue,tag=ingame] if score @s deathpenalty >= deathpenalty settings if score blue lives matches ..0 run tag @s remove ingame
execute as @a[tag=died,team=green,tag=ingame] if score @s deathpenalty >= deathpenalty settings if score green lives matches ..0 run tag @s remove ingame
execute as @a[tag=died,team=red,tag=ingame] if score @s deathpenalty >= deathpenalty settings if score red lives matches ..0 run tag @s remove ingame
execute as @a[tag=died,team=yellow,tag=ingame] if score @s deathpenalty >= deathpenalty settings if score yellow lives matches ..0 run tag @s remove ingame

#removes a life
execute as @a[tag=died,team=orange] if score @s deathpenalty >= deathpenalty settings run scoreboard players remove orange lives 1
execute as @a[tag=died,team=purple] if score @s deathpenalty >= deathpenalty settings run scoreboard players remove purple lives 1
execute as @a[tag=died,team=lightblue] if score @s deathpenalty >= deathpenalty settings run scoreboard players remove lightblue lives 1
execute as @a[tag=died,team=gray] if score @s deathpenalty >= deathpenalty settings run scoreboard players remove gray lives 1
execute as @a[tag=died,team=blue] if score @s deathpenalty >= deathpenalty settings run scoreboard players remove blue lives 1
execute as @a[tag=died,team=green] if score @s deathpenalty >= deathpenalty settings run scoreboard players remove green lives 1
execute as @a[tag=died,team=red] if score @s deathpenalty >= deathpenalty settings run scoreboard players remove red lives 1
execute as @a[tag=died,team=yellow] if score @s deathpenalty >= deathpenalty settings run scoreboard players remove yellow lives 1

#-deathpenalty interval
execute as @a[tag=died] if score @s deathpenalty >= deathpenalty settings run scoreboard players operation @s deathpenalty -= deathpenalty settings