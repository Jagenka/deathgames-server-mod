#add to killstreak for each kill
scoreboard players add @a[limit=1,scores={kills=1..}] killstreak 1
#calc killstreak bonus - weird selector so the nearest player who died determines killstreak bonus - might not work all the time
execute as @a[limit=1,scores={kills=1..}] run scoreboard players operation @s killstreakbonus = killstreakbonus settings
execute as @a[limit=1,scores={kills=1..}] at @s run scoreboard players operation @s killstreakbonus *= @a[limit=1,tag=killed] killstreak
#give money for killing
scoreboard players operation @a[limit=1,scores={kills=1..}] killstreakbonus += moneyperkill settings
execute as @a[limit=1,scores={kills=1..}] run scoreboard players operation @s money += @s killstreakbonus
#show all players what killstreak the deceased had
execute as @a[limit=1,scores={kills=1..}] at @s run tellraw @a [{"text":"They made "},{"score":{"name":"@a[limit=1,tag=killed]","objective":"killstreak"}},{"text":" kill(s) since their previous death."}]
#show killer how much money he got for that kill
execute as @a[limit=1,scores={kills=1..}] run tellraw @s [{"text":"You received ","italic":true,"color":"gray"},{"score":{"name":"@s","objective":"killstreakbonus"},"italic":true,"color":"gray"},{"text":" Money","italic":true,"color":"gray"}]
#reset killstreak for deceased
scoreboard players set @a[limit=1,tag=killed] killstreak 0
#reset shop teleport after kill - 3 cause if 2.. shop resets values to 0
scoreboard players set @a[tag=killed] teleport 3
#done with killed tag
tag @a[limit=1,tag=killed] remove killed
#reset time since last kill
execute as @a[scores={kills=1..}] run scoreboard players set timesincelastkill timer 0
#done with the kills
scoreboard players remove @a[limit=1,scores={kills=1..}] kills 1