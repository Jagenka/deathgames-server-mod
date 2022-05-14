#track time
scoreboard players add timesincelastkill timer 1

#bossbar display
scoreboard players operation bossbar timer = timesincelastkill timer
scoreboard players operation bossbar timer *= 100 constants
scoreboard players operation bossbar timer /= reveal settings
execute store result bossbar minecraft:timesincelastkill value run scoreboard players get bossbar timer

#glow players after too long
execute if score timesincelastkill timer >= reveal settings run effect give @a[tag=ingame] minecraft:glowing 2 0 true
execute if score timesincelastkill timer < reveal settings run effect clear @a[tag=ingame] minecraft:glowing
#close shop
execute if score timesincelastkill timer >= reveal settings run fill -5 63 -5 5 63 5 minecraft:black_stained_glass keep
#change bossbar design
execute if score timesincelastkill timer >= reveal settings run bossbar set minecraft:timesincelastkill name "Shop closed & Players revealed"
execute if score timesincelastkill timer >= reveal settings run bossbar set minecraft:timesincelastkill color red

#open shop
execute if score timesincelastkill timer < reveal settings run fill -5 63 -5 5 63 5 air replace minecraft:black_stained_glass
#change bossbar design
execute if score timesincelastkill timer < reveal settings run bossbar set minecraft:timesincelastkill name "Time since last kill"
execute if score timesincelastkill timer < reveal settings run bossbar set minecraft:timesincelastkill color white