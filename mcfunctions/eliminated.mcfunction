#ensure at least 0 lives
execute if score orange lives matches ..-1 run scoreboard players set orange lives 0
execute if score purple lives matches ..-1 run scoreboard players set purple lives 0
execute if score lightblue lives matches ..-1 run scoreboard players set lightblue lives 0
execute if score gray lives matches ..-1 run scoreboard players set gray lives 0
execute if score blue lives matches ..-1 run scoreboard players set blue lives 0
execute if score green lives matches ..-1 run scoreboard players set green lives 0
execute if score red lives matches ..-1 run scoreboard players set red lives 0
execute if score yellow lives matches ..-1 run scoreboard players set yellow lives 0

#count ingame players
scoreboard players set playercount ingame 0
execute as @a[tag=ingame] run scoreboard players add playercount ingame 1

#count players in team
scoreboard players set orange ingame 0
scoreboard players set purple ingame 0
scoreboard players set lightblue ingame 0
scoreboard players set gray ingame 0
scoreboard players set blue ingame 0
scoreboard players set green ingame 0
scoreboard players set red ingame 0
scoreboard players set yellow ingame 0
execute as @a[tag=ingame,team=orange] run scoreboard players add orange ingame 1
execute as @a[tag=ingame,team=purple] run scoreboard players add purple ingame 1
execute as @a[tag=ingame,team=lightblue] run scoreboard players add lightblue ingame 1
execute as @a[tag=ingame,team=gray] run scoreboard players add gray ingame 1
execute as @a[tag=ingame,team=blue] run scoreboard players add blue ingame 1
execute as @a[tag=ingame,team=green] run scoreboard players add green ingame 1
execute as @a[tag=ingame,team=red] run scoreboard players add red ingame 1
execute as @a[tag=ingame,team=yellow] run scoreboard players add yellow ingame 1

#display game over message
execute if score orange ingame matches 0 unless score orange eliminated matches 1.. store success score resetdisplay eliminated run scoreboard players set orange eliminated 2
execute if score purple ingame matches 0 unless score purple eliminated matches 1.. store success score resetdisplay eliminated run scoreboard players set purple eliminated 2
execute if score lightblue ingame matches 0 unless score lightblue eliminated matches 1.. store success score resetdisplay eliminated run scoreboard players set lightblue eliminated 2
execute if score gray ingame matches 0 unless score gray eliminated matches 1.. store success score resetdisplay eliminated run scoreboard players set gray eliminated 2
execute if score blue ingame matches 0 unless score blue eliminated matches 1.. store success score resetdisplay eliminated run scoreboard players set blue eliminated 2
execute if score green ingame matches 0 unless score green eliminated matches 1.. store success score resetdisplay eliminated run scoreboard players set green eliminated 2
execute if score red ingame matches 0 unless score red eliminated matches 1.. store success score resetdisplay eliminated run scoreboard players set red eliminated 2
execute if score yellow ingame matches 0 unless score yellow eliminated matches 1.. store success score resetdisplay eliminated run scoreboard players set yellow eliminated 2
execute if score orange eliminated matches 2 run tellraw @a {"text":"Game Over for Team Orange","bold":true,"color":"gold"}
execute if score purple eliminated matches 2 run tellraw @a {"text":"Game Over for Team Purple","bold":true,"color":"dark_purple"}
execute if score lightblue eliminated matches 2 run tellraw @a {"text":"Game Over for Team Light Blue","bold":true,"color":"aqua"}
execute if score gray eliminated matches 2 run tellraw @a {"text":"Game Over for Team Gray","bold":true,"color":"dark_gray"}
execute if score blue eliminated matches 2 run tellraw @a {"text":"Game Over for Team Blue","bold":true,"color":"blue"}
execute if score green eliminated matches 2 run tellraw @a {"text":"Game Over for Team Green","bold":true,"color":"green"}
execute if score red eliminated matches 2 run tellraw @a {"text":"Game Over for Team Red","bold":true,"color":"red"}
execute if score yellow eliminated matches 2 run tellraw @a {"text":"Game Over for Team Yellow","bold":true,"color":"yellow"}
execute if score orange eliminated matches 2 run scoreboard players set orange eliminated 1
execute if score purple eliminated matches 2 run scoreboard players set purple eliminated 1
execute if score lightblue eliminated matches 2 run scoreboard players set lightblue eliminated 1
execute if score gray eliminated matches 2 run scoreboard players set gray eliminated 1
execute if score blue eliminated matches 2 run scoreboard players set blue eliminated 1
execute if score green eliminated matches 2 run scoreboard players set green eliminated 1
execute if score red eliminated matches 2 run scoreboard players set red eliminated 1
execute if score yellow eliminated matches 2 run scoreboard players set yellow eliminated 1

#reset pretty_lives for later refilling
execute if score resetdisplay eliminated matches 1 run scoreboard players reset * pretty_lives
scoreboard players set resetdisplay eliminated 0

#calc ingame teams
execute store result score teams ingame run scoreboard players get 8 constants
scoreboard players operation teams ingame -= orange eliminated
scoreboard players operation teams ingame -= purple eliminated
scoreboard players operation teams ingame -= lightblue eliminated
scoreboard players operation teams ingame -= gray eliminated
scoreboard players operation teams ingame -= blue eliminated
scoreboard players operation teams ingame -= green eliminated
scoreboard players operation teams ingame -= red eliminated
scoreboard players operation teams ingame -= yellow eliminated

#reset lives just to be sure
execute if score orange eliminated matches 1 run scoreboard players set orange lives 0
execute if score purple eliminated matches 1 run scoreboard players set purple lives 0
execute if score lightblue eliminated matches 1 run scoreboard players set lightblue lives 0
execute if score gray eliminated matches 1 run scoreboard players set gray lives 0
execute if score blue eliminated matches 1 run scoreboard players set blue lives 0
execute if score green eliminated matches 1 run scoreboard players set green lives 0
execute if score red eliminated matches 1 run scoreboard players set red lives 0
execute if score yellow eliminated matches 1 run scoreboard players set yellow lives 0

#remove ingame tag of all eliminated team's players
execute if score orange eliminated matches 1 run tag @a[team=orange] remove ingame
execute if score purple eliminated matches 1 run tag @a[team=purple] remove ingame
execute if score lightblue eliminated matches 1 run tag @a[team=lightblue] remove ingame
execute if score gray eliminated matches 1 run tag @a[team=gray] remove ingame
execute if score blue eliminated matches 1 run tag @a[team=blue] remove ingame
execute if score green eliminated matches 1 run tag @a[team=green] remove ingame
execute if score red eliminated matches 1 run tag @a[team=red] remove ingame
execute if score yellow eliminated matches 1 run tag @a[team=yellow] remove ingame

#end game if only 1 team or less left
execute if score teams ingame matches ..1 if score running gamestate matches 1 run scoreboard players set running gamestate 2

#pretty display
execute if score orange eliminated matches 0 store result score *Orange: pretty_lives as @a[team=orange,limit=1] run scoreboard players get orange lives
execute if score purple eliminated matches 0 store result score *Purple: pretty_lives as @a[team=purple,limit=1] run scoreboard players get purple lives
execute if score lightblue eliminated matches 0 store result score *Light_Blue: pretty_lives as @a[team=lightblue,limit=1] run scoreboard players get lightblue lives
execute if score gray eliminated matches 0 store result score *Gray: pretty_lives as @a[team=gray,limit=1] run scoreboard players get gray lives
execute if score blue eliminated matches 0 store result score *Blue: pretty_lives as @a[team=blue,limit=1] run scoreboard players get blue lives
execute if score green eliminated matches 0 store result score *Green: pretty_lives as @a[team=green,limit=1] run scoreboard players get green lives
execute if score red eliminated matches 0 store result score *Red: pretty_lives as @a[team=red,limit=1] run scoreboard players get red lives
execute if score yellow eliminated matches 0 store result score *Yellow: pretty_lives as @a[team=yellow,limit=1] run scoreboard players get yellow lives
