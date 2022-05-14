#Game Over message
execute if score running gamestate matches 2 run title @a times 10 180 10
execute if score running gamestate matches 2 run title @a title "Game Over"
#message who won
execute if score running gamestate matches 2 if score orange eliminated matches 0 run title @a subtitle {"text":"Team Orange won!","color":"gold"}
execute if score running gamestate matches 2 if score purple eliminated matches 0 run title @a subtitle {"text":"Team Purple won!","color":"dark_purple"}
execute if score running gamestate matches 2 if score lightblue eliminated matches 0 run title @a subtitle {"text":"Team Light Blue won!","color":"aqua"}
execute if score running gamestate matches 2 if score gray eliminated matches 0 run title @a subtitle {"text":"Team Gray won!","color":"dark_gray"}
execute if score running gamestate matches 2 if score blue eliminated matches 0 run title @a subtitle {"text":"Team Blue won!","color":"blue"}
execute if score running gamestate matches 2 if score green eliminated matches 0 run title @a subtitle {"text":"Team Green won!","color":"green"}
execute if score running gamestate matches 2 if score red eliminated matches 0 run title @a subtitle {"text":"Team Red won!","color":"red"}
execute if score running gamestate matches 2 if score yellow eliminated matches 0 run title @a subtitle {"text":"Team Yellow won!","color":"yellow"}
#disable bossbar
execute if score running gamestate matches 2 run bossbar set minecraft:timesincelastkill players
execute if score running gamestate matches 2 run scoreboard players set running gamestate 3
#cooldown timer
execute if score running gamestate matches 3 run scoreboard players add cooldown gamestate 1
execute if score running gamestate matches 3 if score cooldown gamestate matches 200.. run scoreboard players set running gamestate 4
execute if score running gamestate matches 4 run scoreboard players set cooldown gamestate 0
#tp all players to lobby
execute if score running gamestate matches 4 run tp @a[tag=!admin] 0 42 0
#clear inventories
execute if score running gamestate matches 4 run clear @a[tag=!admin]
#everyone leave team - danke Loki
execute if score running gamestate matches 4 run team leave *
#display names must have team
execute if score running gamestate matches 4 run team join display_orange *Orange:
execute if score running gamestate matches 4 run team join display_purple *Purple:
execute if score running gamestate matches 4 run team join display_lblue *Light_Blue:
execute if score running gamestate matches 4 run team join display_gray *Gray:
execute if score running gamestate matches 4 run team join display_blue *Blue:
execute if score running gamestate matches 4 run team join display_green *Green:
execute if score running gamestate matches 4 run team join display_red *Red:
execute if score running gamestate matches 4 run team join display_yellow *Yellow:
#done
execute if score running gamestate matches 4 run execute store result storage minecraft:deathgames game_running byte 1 run scoreboard players get 0 constants
execute if score running gamestate matches 4 run scoreboard players set running gamestate 0