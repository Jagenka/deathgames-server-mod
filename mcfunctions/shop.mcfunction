#protecc all players in shop
effect give @a[tag=inshop,gamemode=adventure] minecraft:resistance 1 255 true

#passive money gain
scoreboard players add money timer 1
execute if score money timer >= moneyinterval settings run scoreboard players operation @a[tag=ingame] money += moneyperinterval settings
execute if score money timer >= moneyinterval settings run scoreboard players operation money timer -= moneyinterval settings

#inform players of upgrade prices
#armor upgrade prices
execute as @a[scores={shopinformed=0},tag=inshop,tag=ingame] if score @s armor matches 2 run tellraw @s [{"text":"Upgrade your armor for "},{"score":{"name":"armor2","objective":"prices"}}]
execute as @a[scores={shopinformed=0},tag=inshop,tag=ingame] if score @s armor matches 4 run tellraw @s [{"text":"Upgrade your armor for "},{"score":{"name":"armor3","objective":"prices"}}]
execute as @a[scores={shopinformed=0},tag=inshop,tag=ingame] if score @s armor matches 6 run tellraw @s [{"text":"Upgrade your armor for "},{"score":{"name":"armor4","objective":"prices"}}]
execute as @a[scores={shopinformed=0},tag=inshop,tag=ingame] if score @s armor matches 8 run tellraw @s [{"text":"Upgrade your armor for "},{"score":{"name":"armor5","objective":"prices"}}]
execute as @a[scores={shopinformed=0},tag=inshop,tag=ingame] if score @s armor matches 10 run tellraw @s [{"text":"Upgrade your armor for "},{"score":{"name":"armor6","objective":"prices"}}]
#sword upgrade prices
execute as @a[scores={shopinformed=0},tag=inshop,tag=ingame] if score @s sword matches 2 run tellraw @s [{"text":"Upgrade your sword for "},{"score":{"name":"sword2","objective":"prices"}}]
execute as @a[scores={shopinformed=0},tag=inshop,tag=ingame] if score @s sword matches 4 run tellraw @s [{"text":"Upgrade your sword for "},{"score":{"name":"sword3","objective":"prices"}}]
execute as @a[scores={shopinformed=0},tag=inshop,tag=ingame] if score @s sword matches 6 run tellraw @s [{"text":"Upgrade your sword for "},{"score":{"name":"sword4","objective":"prices"}}]
execute as @a[scores={shopinformed=0},tag=inshop,tag=ingame] if score @s sword matches 8 run tellraw @s [{"text":"Upgrade your sword for "},{"score":{"name":"sword5","objective":"prices"}}]
#axe upgrade prices
execute as @a[scores={shopinformed=0},tag=inshop,tag=ingame] if score @s axe matches 2 run tellraw @s [{"text":"Upgrade your axe for "},{"score":{"name":"axe2","objective":"prices"}}]
execute as @a[scores={shopinformed=0},tag=inshop,tag=ingame] if score @s axe matches 4 run tellraw @s [{"text":"Upgrade your axe for "},{"score":{"name":"axe3","objective":"prices"}}]
execute as @a[scores={shopinformed=0},tag=inshop,tag=ingame] if score @s axe matches 6 run tellraw @s [{"text":"Upgrade your axe for "},{"score":{"name":"axe4","objective":"prices"}}]
execute as @a[scores={shopinformed=0},tag=inshop,tag=ingame] if score @s axe matches 8 run tellraw @s [{"text":"Upgrade your axe for "},{"score":{"name":"axe5","objective":"prices"}}]
#bow upgrade prices
execute as @a[scores={shopinformed=0},tag=inshop,tag=ingame] if score @s bow matches 2 run tellraw @s [{"text":"Upgrade your bow for "},{"score":{"name":"bow2","objective":"prices"}}]
execute as @a[scores={shopinformed=0},tag=inshop,tag=ingame] if score @s bow matches 4 run tellraw @s [{"text":"Upgrade your bow for "},{"score":{"name":"bow3","objective":"prices"}}]
execute as @a[scores={shopinformed=0},tag=inshop,tag=ingame] if score @s bow matches 6 run tellraw @s [{"text":"Upgrade your bow for "},{"score":{"name":"bow4","objective":"prices"}}]
execute as @a[scores={shopinformed=0},tag=inshop,tag=ingame] if score @s bow matches 8 run tellraw @s [{"text":"Upgrade your bow for "},{"score":{"name":"bow5","objective":"prices"}}]
execute as @a[scores={shopinformed=0},tag=inshop,tag=ingame] if score @s bow matches 10 run tellraw @s [{"text":"Upgrade your bow for "},{"score":{"name":"bow6","objective":"prices"}}]
#crossbow upgrade prices
execute as @a[scores={shopinformed=0},tag=inshop,tag=ingame] if score @s crossbow matches 2 run tellraw @s [{"text":"Upgrade your crossbow for "},{"score":{"name":"crossbow2","objective":"prices"}}]
execute as @a[scores={shopinformed=0},tag=inshop,tag=ingame] if score @s crossbow matches 4 run tellraw @s [{"text":"Upgrade your crossbow for "},{"score":{"name":"crossbow3","objective":"prices"}}]
execute as @a[scores={shopinformed=0},tag=inshop,tag=ingame] if score @s crossbow matches 6 run tellraw @s [{"text":"Upgrade your crossbow for "},{"score":{"name":"crossbow4","objective":"prices"}}]
execute as @a[scores={shopinformed=0},tag=inshop,tag=ingame] if score @s crossbow matches 8 run tellraw @s [{"text":"Upgrade your crossbow for "},{"score":{"name":"crossbow5","objective":"prices"}}]
execute as @a[scores={shopinformed=0},tag=inshop,tag=ingame] if score @s crossbow matches 10 run tellraw @s [{"text":"Upgrade your crossbow for "},{"score":{"name":"crossbow6","objective":"prices"}}]

execute as @a[scores={shopinformed=0},tag=inshop,tag=ingame] run scoreboard players set @s shopinformed 1
execute as @a[tag=!inshop] run scoreboard players set @s shopinformed 0

execute as @a[scores={shop=1}] unless score @s money >= melon prices run scoreboard players operation @s nomoneymessage = melon prices
execute store success storage deathgames shop_success_1 int 1 as @a[scores={shop=1}] if score @s money >= melon prices run scoreboard players operation @s money -= melon prices
execute if data storage deathgames {shop_success_1:1} run give @a[scores={shop=1}] minecraft:melon_slice 8
execute if data storage deathgames {shop_success_1:1} as @a[scores={shop=1}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"melon","objective":"prices"},"color":"gold"}]

execute as @a[scores={shop=2}] unless score @s money >= arrows prices run scoreboard players operation @s nomoneymessage = arrows prices
execute store success storage deathgames shop_success_2 int 1 as @a[scores={shop=2}] if score @s money >= arrows prices run scoreboard players operation @s money -= arrows prices
execute if data storage deathgames {shop_success_2:1} run give @a[scores={shop=2}] minecraft:arrow 4
execute if data storage deathgames {shop_success_2:1} as @a[scores={shop=2}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"arrows","objective":"prices"},"color":"gold"}]

execute as @a[scores={shop=3}] unless score @s money >= ender_pearl prices run scoreboard players operation @s nomoneymessage = ender_pearl prices
execute store success storage deathgames shop_success_3 int 1 as @a[scores={shop=3}] if score @s money >= ender_pearl prices run scoreboard players operation @s money -= ender_pearl prices
execute if data storage deathgames {shop_success_3:1} run give @a[scores={shop=3}] minecraft:ender_pearl 2
execute if data storage deathgames {shop_success_3:1} as @a[scores={shop=3}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"ender_pearl","objective":"prices"},"color":"gold"}]

execute as @a[scores={shop=4}] unless score @s money >= steak prices run scoreboard players operation @s nomoneymessage = steak prices
execute store success storage deathgames shop_success_4 int 1 as @a[scores={shop=4}] if score @s money >= steak prices run scoreboard players operation @s money -= steak prices
execute if data storage deathgames {shop_success_4:1} run give @a[scores={shop=4}] minecraft:cooked_beef 1
execute if data storage deathgames {shop_success_4:1} as @a[scores={shop=4}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"steak","objective":"prices"},"color":"gold"}]

execute as @a[scores={shop=5}] unless score @s money >= fishing_rod prices run scoreboard players operation @s nomoneymessage = fishing_rod prices
execute store success storage deathgames shop_success_5 int 1 as @a[scores={shop=5}] if score @s money >= fishing_rod prices run scoreboard players operation @s money -= fishing_rod prices
execute if data storage deathgames {shop_success_5:1} run give @a[scores={shop=5}] fishing_rod{Unbreakable:1} 1
execute if data storage deathgames {shop_success_5:1} as @a[scores={shop=5}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"fishing_rod","objective":"prices"},"color":"gold"}]

execute as @a[scores={shop=6}] unless score @s money >= flint prices run scoreboard players operation @s nomoneymessage = flint prices
execute store success storage deathgames shop_success_6 int 1 as @a[scores={shop=6}] if score @s money >= flint prices run scoreboard players operation @s money -= flint prices
execute if data storage deathgames {shop_success_6:1} run give @a[scores={shop=6}] minecraft:blaze_rod{Enchantments:[{id:"minecraft:fire_aspect",lvl:2}]} 1
execute if data storage deathgames {shop_success_6:1} as @a[scores={shop=6}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"flint","objective":"prices"},"color":"gold"}]

execute as @a[scores={shop=7}] unless score @s money >= invisibility prices run scoreboard players operation @s nomoneymessage = invisibility prices
execute store success storage deathgames shop_success_7 int 1 as @a[scores={shop=7}] if score @s money >= invisibility prices run scoreboard players operation @s money -= invisibility prices
execute if data storage deathgames {shop_success_7:1} run give @a[scores={shop=7}] minecraft:potion{Potion:"minecraft:water",CustomPotionEffects:[{Id:14,Duration:1200}],display:{Name:"\"Potion of Invisibility\"",Lore:["60s of Invisibility"]}} 1
execute if data storage deathgames {shop_success_7:1} as @a[scores={shop=7}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"invisibility","objective":"prices"},"color":"gold"}]

execute as @a[scores={shop=8}] unless score @s money >= speed prices run scoreboard players operation @s nomoneymessage = speed prices
execute store success storage deathgames shop_success_8 int 1 as @a[scores={shop=8}] if score @s money >= speed prices run scoreboard players operation @s money -= speed prices
execute if data storage deathgames {shop_success_8:1} run give @a[scores={shop=8}] minecraft:potion{Potion:"minecraft:swiftness"} 1
execute if data storage deathgames {shop_success_8:1} as @a[scores={shop=8}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"speed","objective":"prices"},"color":"gold"}]

execute as @a[scores={shop=9}] unless score @s money >= fire prices run scoreboard players operation @s nomoneymessage = fire prices
execute store success storage deathgames shop_success_9 int 1 as @a[scores={shop=9}] if score @s money >= fire prices run scoreboard players operation @s money -= fire prices
execute if data storage deathgames {shop_success_9:1} run give @a[scores={shop=9}] minecraft:potion{Potion:"minecraft:fire_resistance"} 1
execute if data storage deathgames {shop_success_9:1} as @a[scores={shop=9}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"fire","objective":"prices"},"color":"gold"}]

execute as @a[scores={shop=10}] unless score @s money >= strength prices run scoreboard players operation @s nomoneymessage = strength prices
execute store success storage deathgames shop_success_10 int 1 as @a[scores={shop=10}] if score @s money >= strength prices run scoreboard players operation @s money -= strength prices
execute if data storage deathgames {shop_success_10:1} run give @a[scores={shop=10}] minecraft:potion{Potion:"minecraft:strength"} 1
execute if data storage deathgames {shop_success_10:1} as @a[scores={shop=10}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"strength","objective":"prices"},"color":"gold"}]

execute as @a[scores={shop=11}] unless score @s money >= regeneration prices run scoreboard players operation @s nomoneymessage = regeneration prices
execute store success storage deathgames shop_success_11 int 1 as @a[scores={shop=11}] if score @s money >= regeneration prices run scoreboard players operation @s money -= regeneration prices
execute if data storage deathgames {shop_success_11:1} run give @a[scores={shop=11}] minecraft:potion{Potion:"minecraft:regeneration"} 1
execute if data storage deathgames {shop_success_11:1} as @a[scores={shop=11}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"regeneration","objective":"prices"},"color":"gold"}]

execute as @a[scores={shop=12}] unless score @s money >= harming prices run scoreboard players operation @s nomoneymessage = harming prices
execute store success storage deathgames shop_success_12 int 1 as @a[scores={shop=12}] if score @s money >= harming prices run scoreboard players operation @s money -= harming prices
execute if data storage deathgames {shop_success_12:1} run give @a[scores={shop=12}] minecraft:lingering_potion{Potion:"minecraft:harming"} 1
execute if data storage deathgames {shop_success_12:1} as @a[scores={shop=12}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"harming","objective":"prices"},"color":"gold"}]

execute as @a[scores={shop=13}] unless score @s money >= slowness prices run scoreboard players operation @s nomoneymessage = slowness prices
execute store success storage deathgames shop_success_13 int 1 as @a[scores={shop=13}] if score @s money >= slowness prices run scoreboard players operation @s money -= slowness prices
execute if data storage deathgames {shop_success_13:1} run give @a[scores={shop=13}] minecraft:splash_potion{Potion:"minecraft:slowness"} 1
execute if data storage deathgames {shop_success_13:1} as @a[scores={shop=13}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"slowness","objective":"prices"},"color":"gold"}]

execute as @a[scores={shop=14}] unless score @s money >= golden_apple prices run scoreboard players operation @s nomoneymessage = golden_apple prices
execute store success storage deathgames shop_success_14 int 1 as @a[scores={shop=14}] if score @s money >= golden_apple prices run scoreboard players operation @s money -= golden_apple prices
execute if data storage deathgames {shop_success_14:1} run give @a[scores={shop=14}] minecraft:enchanted_golden_apple 1
execute if data storage deathgames {shop_success_14:1} as @a[scores={shop=14}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"golden_apple","objective":"prices"},"color":"gold"}]

execute as @a[scores={shop=15}] unless score @s money >= totem prices run scoreboard players operation @s nomoneymessage = totem prices
execute store success storage deathgames shop_success_15 int 1 as @a[scores={shop=15}] if score @s money >= totem prices run scoreboard players operation @s money -= totem prices
execute if data storage deathgames {shop_success_15:1} run give @a[scores={shop=15}] minecraft:totem_of_undying 1
execute if data storage deathgames {shop_success_15:1} as @a[scores={shop=15}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"totem","objective":"prices"},"color":"gold"}]

execute as @a[scores={shop=16}] unless score @s money >= poison_arrows prices run scoreboard players operation @s nomoneymessage = poison_arrows prices
execute store success storage deathgames shop_success_16 int 1 as @a[scores={shop=16}] if score @s money >= poison_arrows prices run scoreboard players operation @s money -= poison_arrows prices
execute if data storage deathgames {shop_success_16:1} run give @a[scores={shop=16}] minecraft:tipped_arrow{Potion:"minecraft:poison"} 4
execute if data storage deathgames {shop_success_16:1} as @a[scores={shop=16}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"poison_arrows","objective":"prices"},"color":"gold"}]

execute as @a[scores={shop=17,armor=0}] unless score @s money >= armor1 prices run scoreboard players operation @s nomoneymessage = armor1 prices
execute store success storage deathgames shop_success_17 int 1 as @a[scores={shop=17,armor=0}] if score @s money >= armor1 prices run scoreboard players operation @s money -= armor1 prices
execute as @a[scores={shop=17,armor=2}] unless score @s money >= armor2 prices run scoreboard players operation @s nomoneymessage = armor2 prices
execute store success storage deathgames shop_success_17 int 1 as @a[scores={shop=17,armor=2}] if score @s money >= armor2 prices run scoreboard players operation @s money -= armor2 prices
execute as @a[scores={shop=17,armor=4}] unless score @s money >= armor3 prices run scoreboard players operation @s nomoneymessage = armor3 prices
execute store success storage deathgames shop_success_17 int 1 as @a[scores={shop=17,armor=4}] if score @s money >= armor3 prices run scoreboard players operation @s money -= armor3 prices
execute as @a[scores={shop=17,armor=6}] unless score @s money >= armor4 prices run scoreboard players operation @s nomoneymessage = armor4 prices
execute store success storage deathgames shop_success_17 int 1 as @a[scores={shop=17,armor=6}] if score @s money >= armor4 prices run scoreboard players operation @s money -= armor4 prices
execute as @a[scores={shop=17,armor=8}] unless score @s money >= armor5 prices run scoreboard players operation @s nomoneymessage = armor5 prices
execute store success storage deathgames shop_success_17 int 1 as @a[scores={shop=17,armor=8}] if score @s money >= armor5 prices run scoreboard players operation @s money -= armor5 prices
execute as @a[scores={shop=17,armor=10}] unless score @s money >= armor6 prices run scoreboard players operation @s nomoneymessage = armor6 prices
execute store success storage deathgames shop_success_17 int 1 as @a[scores={shop=17,armor=10}] if score @s money >= armor6 prices run scoreboard players operation @s money -= armor6 prices
#already maxed
execute store result storage deathgames shop_success_17 int 1 as @a[scores={shop=17,armor=12..}] run scoreboard players get 2 constants
execute if data storage deathgames {shop_success_17:1} run scoreboard players add @a[scores={shop=17}] armor 1
execute if data storage deathgames {shop_success_17:1} run scoreboard players set @a[scores={shop=17}] shopinformed 0
execute if data storage deathgames {shop_success_17:1} as @a[scores={shop=17,armor=1}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"armor1","objective":"prices"},"color":"gold"}]
execute if data storage deathgames {shop_success_17:1} as @a[scores={shop=17,armor=3}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"armor2","objective":"prices"},"color":"gold"}]
execute if data storage deathgames {shop_success_17:1} as @a[scores={shop=17,armor=5}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"armor3","objective":"prices"},"color":"gold"}]
execute if data storage deathgames {shop_success_17:1} as @a[scores={shop=17,armor=7}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"armor4","objective":"prices"},"color":"gold"}]
execute if data storage deathgames {shop_success_17:1} as @a[scores={shop=17,armor=9}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"armor5","objective":"prices"},"color":"gold"}]
execute if data storage deathgames {shop_success_17:1} as @a[scores={shop=17,armor=11}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"armor6","objective":"prices"},"color":"gold"}]
#already maxed
execute if data storage deathgames {shop_success_17:2} as @a[scores={shop=17,armor=12..}] run tellraw @s {"text":"You're already maxed!"}

execute as @a[scores={shop=18,bow=0}] unless score @s money >= bow1 prices run scoreboard players operation @s nomoneymessage = bow1 prices
execute store success storage deathgames shop_success_18 int 1 as @a[scores={shop=18,bow=0}] if score @s money >= bow1 prices run scoreboard players operation @s money -= bow1 prices
execute as @a[scores={shop=18,bow=2}] unless score @s money >= bow2 prices run scoreboard players operation @s nomoneymessage = bow2 prices
execute store success storage deathgames shop_success_18 int 1 as @a[scores={shop=18,bow=2}] if score @s money >= bow2 prices run scoreboard players operation @s money -= bow2 prices
execute as @a[scores={shop=18,bow=4}] unless score @s money >= bow3 prices run scoreboard players operation @s nomoneymessage = bow3 prices
execute store success storage deathgames shop_success_18 int 1 as @a[scores={shop=18,bow=4}] if score @s money >= bow3 prices run scoreboard players operation @s money -= bow3 prices
execute as @a[scores={shop=18,bow=6}] unless score @s money >= bow4 prices run scoreboard players operation @s nomoneymessage = bow4 prices
execute store success storage deathgames shop_success_18 int 1 as @a[scores={shop=18,bow=6}] if score @s money >= bow4 prices run scoreboard players operation @s money -= bow4 prices
execute as @a[scores={shop=18,bow=8}] unless score @s money >= bow5 prices run scoreboard players operation @s nomoneymessage = bow5 prices
execute store success storage deathgames shop_success_18 int 1 as @a[scores={shop=18,bow=8}] if score @s money >= bow5 prices run scoreboard players operation @s money -= bow5 prices
execute as @a[scores={shop=18,bow=10}] unless score @s money >= bow6 prices run scoreboard players operation @s nomoneymessage = bow6 prices
execute store success storage deathgames shop_success_18 int 1 as @a[scores={shop=18,bow=10}] if score @s money >= bow6 prices run scoreboard players operation @s money -= bow6 prices
#already maxed
execute store result storage deathgames shop_success_18 int 1 as @a[scores={shop=18,bow=12..}] run scoreboard players get 2 constants
execute if data storage deathgames {shop_success_18:1} run scoreboard players add @a[scores={shop=18}] bow 1
execute if data storage deathgames {shop_success_18:1} run scoreboard players set @a[scores={shop=18}] shopinformed 0
execute if data storage deathgames {shop_success_18:1} as @a[scores={shop=18,bow=1}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"bow1","objective":"prices"},"color":"gold"}]
execute if data storage deathgames {shop_success_18:1} as @a[scores={shop=18,bow=3}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"bow2","objective":"prices"},"color":"gold"}]
execute if data storage deathgames {shop_success_18:1} as @a[scores={shop=18,bow=5}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"bow3","objective":"prices"},"color":"gold"}]
execute if data storage deathgames {shop_success_18:1} as @a[scores={shop=18,bow=7}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"bow4","objective":"prices"},"color":"gold"}]
execute if data storage deathgames {shop_success_18:1} as @a[scores={shop=18,bow=9}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"bow5","objective":"prices"},"color":"gold"}]
execute if data storage deathgames {shop_success_18:1} as @a[scores={shop=18,bow=11}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"bow6","objective":"prices"},"color":"gold"}]
#already maxed
execute if data storage deathgames {shop_success_18:2} as @a[scores={shop=18,bow=12..}] run tellraw @s {"text":"You're already maxed!"}

execute as @a[scores={shop=19,crossbow=0}] unless score @s money >= crossbow1 prices run scoreboard players operation @s nomoneymessage = crossbow1 prices
execute store success storage deathgames shop_success_19 int 1 as @a[scores={shop=19,crossbow=0}] if score @s money >= crossbow1 prices run scoreboard players operation @s money -= crossbow1 prices
execute as @a[scores={shop=19,crossbow=2}] unless score @s money >= crossbow2 prices run scoreboard players operation @s nomoneymessage = crossbow2 prices
execute store success storage deathgames shop_success_19 int 1 as @a[scores={shop=19,crossbow=2}] if score @s money >= crossbow2 prices run scoreboard players operation @s money -= crossbow2 prices
execute as @a[scores={shop=19,crossbow=4}] unless score @s money >= crossbow3 prices run scoreboard players operation @s nomoneymessage = crossbow3 prices
execute store success storage deathgames shop_success_19 int 1 as @a[scores={shop=19,crossbow=4}] if score @s money >= crossbow3 prices run scoreboard players operation @s money -= crossbow3 prices
execute as @a[scores={shop=19,crossbow=6}] unless score @s money >= crossbow4 prices run scoreboard players operation @s nomoneymessage = crossbow4 prices
execute store success storage deathgames shop_success_19 int 1 as @a[scores={shop=19,crossbow=6}] if score @s money >= crossbow4 prices run scoreboard players operation @s money -= crossbow4 prices
execute as @a[scores={shop=19,crossbow=8}] unless score @s money >= crossbow5 prices run scoreboard players operation @s nomoneymessage = crossbow5 prices
execute store success storage deathgames shop_success_19 int 1 as @a[scores={shop=19,crossbow=8}] if score @s money >= crossbow5 prices run scoreboard players operation @s money -= crossbow5 prices
execute as @a[scores={shop=19,crossbow=10}] unless score @s money >= crossbow6 prices run scoreboard players operation @s nomoneymessage = crossbow6 prices
execute store success storage deathgames shop_success_19 int 1 as @a[scores={shop=19,crossbow=10}] if score @s money >= crossbow6 prices run scoreboard players operation @s money -= crossbow6 prices
#already maxed
execute store result storage deathgames shop_success_19 int 1 as @a[scores={shop=19,crossbow=12..}] run scoreboard players get 2 constants
execute if data storage deathgames {shop_success_19:1} run scoreboard players add @a[scores={shop=19}] crossbow 1
execute if data storage deathgames {shop_success_19:1} run scoreboard players set @a[scores={shop=19}] shopinformed 0
execute if data storage deathgames {shop_success_19:1} as @a[scores={shop=19,crossbow=1}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"crossbow1","objective":"prices"},"color":"gold"}]
execute if data storage deathgames {shop_success_19:1} as @a[scores={shop=19,crossbow=3}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"crossbow2","objective":"prices"},"color":"gold"}]
execute if data storage deathgames {shop_success_19:1} as @a[scores={shop=19,crossbow=5}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"crossbow3","objective":"prices"},"color":"gold"}]
execute if data storage deathgames {shop_success_19:1} as @a[scores={shop=19,crossbow=7}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"crossbow4","objective":"prices"},"color":"gold"}]
execute if data storage deathgames {shop_success_19:1} as @a[scores={shop=19,crossbow=9}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"crossbow5","objective":"prices"},"color":"gold"}]
execute if data storage deathgames {shop_success_19:1} as @a[scores={shop=19,crossbow=11}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"crossbow6","objective":"prices"},"color":"gold"}]
#already maxed
execute if data storage deathgames {shop_success_19:2} as @a[scores={shop=19,crossbow=12..}] run tellraw @s {"text":"You're already maxed!"}

execute as @a[scores={shop=20,sword=0}] unless score @s money >= sword1 prices run scoreboard players operation @s nomoneymessage = sword1 prices
execute store success storage deathgames shop_success_20 int 1 as @a[scores={shop=20,sword=0}] if score @s money >= sword1 prices run scoreboard players operation @s money -= sword1 prices
execute as @a[scores={shop=20,sword=2}] unless score @s money >= sword2 prices run scoreboard players operation @s nomoneymessage = sword2 prices
execute store success storage deathgames shop_success_20 int 1 as @a[scores={shop=20,sword=2}] if score @s money >= sword2 prices run scoreboard players operation @s money -= sword2 prices
execute as @a[scores={shop=20,sword=4}] unless score @s money >= sword3 prices run scoreboard players operation @s nomoneymessage = sword3 prices
execute store success storage deathgames shop_success_20 int 1 as @a[scores={shop=20,sword=4}] if score @s money >= sword3 prices run scoreboard players operation @s money -= sword3 prices
execute as @a[scores={shop=20,sword=6}] unless score @s money >= sword4 prices run scoreboard players operation @s nomoneymessage = sword4 prices
execute store success storage deathgames shop_success_20 int 1 as @a[scores={shop=20,sword=6}] if score @s money >= sword4 prices run scoreboard players operation @s money -= sword4 prices
execute as @a[scores={shop=20,sword=8}] unless score @s money >= sword5 prices run scoreboard players operation @s nomoneymessage = sword5 prices
execute store success storage deathgames shop_success_20 int 1 as @a[scores={shop=20,sword=8}] if score @s money >= sword5 prices run scoreboard players operation @s money -= sword5 prices
#already maxed
execute store result storage deathgames shop_success_20 int 1 as @a[scores={shop=20,sword=10..}] run scoreboard players get 2 constants
execute if data storage deathgames {shop_success_20:1} run scoreboard players add @a[scores={shop=20}] sword 1
execute if data storage deathgames {shop_success_20:1} run scoreboard players set @a[scores={shop=20}] shopinformed 0
execute if data storage deathgames {shop_success_20:1} as @a[scores={shop=20,sword=1}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"sword1","objective":"prices"},"color":"gold"}]
execute if data storage deathgames {shop_success_20:1} as @a[scores={shop=20,sword=3}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"sword2","objective":"prices"},"color":"gold"}]
execute if data storage deathgames {shop_success_20:1} as @a[scores={shop=20,sword=5}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"sword3","objective":"prices"},"color":"gold"}]
execute if data storage deathgames {shop_success_20:1} as @a[scores={shop=20,sword=7}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"sword4","objective":"prices"},"color":"gold"}]
execute if data storage deathgames {shop_success_20:1} as @a[scores={shop=20,sword=9}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"sword5","objective":"prices"},"color":"gold"}]
#already maxed
execute if data storage deathgames {shop_success_20:2} as @a[scores={shop=20,sword=10..}] run tellraw @s {"text":"You're already maxed!"}

execute as @a[scores={shop=21,axe=0}] unless score @s money >= axe1 prices run scoreboard players operation @s nomoneymessage = axe1 prices
execute store success storage deathgames shop_success_21 int 1 as @a[scores={shop=21,axe=0}] if score @s money >= axe1 prices run scoreboard players operation @s money -= axe1 prices
execute as @a[scores={shop=21,axe=2}] unless score @s money >= axe2 prices run scoreboard players operation @s nomoneymessage = axe2 prices
execute store success storage deathgames shop_success_21 int 1 as @a[scores={shop=21,axe=2}] if score @s money >= axe2 prices run scoreboard players operation @s money -= axe2 prices
execute as @a[scores={shop=21,axe=4}] unless score @s money >= axe3 prices run scoreboard players operation @s nomoneymessage = axe3 prices
execute store success storage deathgames shop_success_21 int 1 as @a[scores={shop=21,axe=4}] if score @s money >= axe3 prices run scoreboard players operation @s money -= axe3 prices
execute as @a[scores={shop=21,axe=6}] unless score @s money >= axe4 prices run scoreboard players operation @s nomoneymessage = axe4 prices
execute store success storage deathgames shop_success_21 int 1 as @a[scores={shop=21,axe=6}] if score @s money >= axe4 prices run scoreboard players operation @s money -= axe4 prices
execute as @a[scores={shop=21,axe=8}] unless score @s money >= axe5 prices run scoreboard players operation @s nomoneymessage = axe5 prices
execute store success storage deathgames shop_success_21 int 1 as @a[scores={shop=21,axe=8}] if score @s money >= axe5 prices run scoreboard players operation @s money -= axe5 prices
#already maxed
execute store result storage deathgames shop_success_21 int 1 as @a[scores={shop=21,axe=10..}] run scoreboard players get 2 constants
execute if data storage deathgames {shop_success_21:1} run scoreboard players add @a[scores={shop=21}] axe 1
execute if data storage deathgames {shop_success_21:1} run scoreboard players set @a[scores={shop=21}] shopinformed 0
execute if data storage deathgames {shop_success_21:1} as @a[scores={shop=21,axe=1}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"axe1","objective":"prices"},"color":"gold"}]
execute if data storage deathgames {shop_success_21:1} as @a[scores={shop=21,axe=3}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"axe2","objective":"prices"},"color":"gold"}]
execute if data storage deathgames {shop_success_21:1} as @a[scores={shop=21,axe=5}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"axe3","objective":"prices"},"color":"gold"}]
execute if data storage deathgames {shop_success_21:1} as @a[scores={shop=21,axe=7}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"axe4","objective":"prices"},"color":"gold"}]
execute if data storage deathgames {shop_success_21:1} as @a[scores={shop=21,axe=9}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"axe5","objective":"prices"},"color":"gold"}]
#already maxed
execute if data storage deathgames {shop_success_21:2} as @a[scores={shop=21,axe=10..}] run tellraw @s {"text":"You're already maxed!"}

execute as @a[scores={shop=22}] unless score @s money >= snare prices run scoreboard players operation @s nomoneymessage = snare prices
execute store success storage deathgames shop_success_22 int 1 as @a[scores={shop=22}] if score @s money >= snare prices run scoreboard players operation @s money -= snare prices
execute if data storage deathgames {shop_success_22:1} run give @a[scores={shop=22}] bat_spawn_egg{display:{Name:'{"text":"Simple Trap Kit","color":"gray","bold":true}',Lore:['{"text":"Places a snare trap."}']},CustomModelData:420690,simple_trap:1b,Enchantments:[{}],EntityTag:{Tags:["simpletrap"]},HideFlags:24,CanPlaceOn:["acacia_bark","acacia_door","acacia_fence","acacia_leaves","acacia_log","acacia_planks","acacia_sapling","acacia_slab","acacia_stairs","activator_rail","allium","andesite","anvil","azure_bluet","banner","barrier","beacon","white_bed","orange_bed","magenta_bed","light_blue_bed","yellow_bed","lime_bed","pink_bed","gray_bed","light_gray_bed","cyan_bed","purple_bed","blue_bed","brown_bed","green_bed","red_bed","black_bed","bedrock","beetroots","birch_bark","birch_door","birch_fence","birch_fence_gate","birch_leaves","birch_log","birch_planks","birch_sapling","birch_slab","birch_stairs","black_carpet","black_concrete","black_concrete_powder","black_glazed_terracotta","black_shulker_box","black_stained_glass","black_stained_glass_pane","black_terracotta","black_wool","blue_carpet","blue_concrete","blue_concrete_powder","blue_glazed_terracotta","blue_orchid","blue_shulker_box","blue_stained_glass","blue_stained_glass_pane","blue_terracotta","blue_wool","bone_block","bookshelf","brewing_stand","brick_slab","brick_stairs","bricks","brown_carpet","brown_concrete","brown_concrete_powder","brown_glazed_terracotta","brown_mushroom","brown_mushroom_block","brown_shulker_box","brown_stained_glass","brown_stained_glass_pane","brown_terracotta","brown_wool","cactus","cake","carrots","cauldron","chain_command_block","chest","chipped_anvil","chiseled_quartz","chiseled_red_sandstone","chiseled_sandstone","chiseled_stone_bricks","chorus_flower","chorus_plant","clay","coal_block","coal_ore","coarse_dirt","cobblestone","cobblestone_slab","cobblestone_stairs","cobblestone_wall","cobweb","cocoa","command_block","comparator","cracked_stone_bricks","crafting_table","creeper_head","cut_red_sandstone","cut_sandstone","cyan_carpet","cyan_concrete","cyan_concrete_powder","cyan_glazed_terracotta","cyan_shulker_box","cyan_stained_glass","cyan_stained_glass_pane","cyan_terracotta","cyan_wool","damaged_anvil","dandelion","dark_oak_bark","dark_oak_door","dark_oak_fence","dark_oak_fence_gate","dark_oak_leaves","dark_oak_log","dark_oak_planks","dark_oak_sapling","dark_oak_slab","dark_oak_stairs","dark_prismarine","daylight_detector","dead_bush","detector_rail","diamond_block","diamond_ore","diorite","dirt","dispenser","dragon_egg","dragon_head","dropper","emerald_block","emerald_ore","enchanting_table","end_gateway","end_portal","end_portal_frame","end_rod","end_stone","end_stone_bricks","ender_chest","farmland","fern","fire","flower_pot","flowing_lava","flowing_water","frosted_ice","furnace","glass","glass_pane","glowstone","gold_block","gold_ore","granite","grass","grass_block","grass_path","gravel","gray_carpet","gray_concrete","gray_concrete_powder","gray_glazed_terracotta","gray_shulker_box","gray_stained_glass","gray_stained_glass_pane","gray_terracotta","gray_wool","green_carpet","green_concrete","green_concrete_powder","green_glazed_terracotta","green_shulker_box","green_stained_glass","green_stained_glass_pane","green_terracotta","green_wool","hay_bale","heavy_weighted_pressure_plate","hopper","ice","infested_chiseled_stone_bricks","infested_cobblestone","infested_cracked_stone_bricks","infested_mossy_stone_bricks","infested_stone","infested_stone_bricks","iron_bars","iron_block","iron_door","iron_ore","iron_trapdoor","jack_o_lantern","jukebox","jungle_bark","jungle_door","jungle_fence","jungle_fence_gate","jungle_leaves","jungle_log","jungle_planks","jungle_sapling","jungle_slab","jungle_stairs","ladder","lapis_block","lapis_ore","large_fern","lava","lever","light_blue_carpet","light_blue_concrete","light_blue_concrete_powder","light_blue_glazed_terracotta","light_blue_shulker_box","light_blue_stained_glass","light_blue_stained_glass_pane","light_blue_terracotta","light_blue_wool","light_gray_carpet","light_gray_concrete","light_gray_concrete_powder","light_gray_glazed_terracotta","light_gray_shulker_box","light_gray_stained_glass","light_gray_stained_glass_pane","light_gray_terracotta","light_gray_wool","light_weighted_pressure_plate","lilac","lily_pad","lime_carpet","lime_concrete","lime_concrete_powder","lime_glazed_terracotta","lime_shulker_box","lime_stained_glass","lime_stained_glass_pane","lime_terracotta","lime_wool","magenta_carpet","magenta_concrete","magenta_concrete_powder","magenta_glazed_terracotta","magenta_shulker_box","magenta_stained_glass","magenta_stained_glass_pane","magenta_terracotta","magenta_wool","magma_block","melon_block","melon_plant","melon_stem","mob_spawner","mossy_cobblestone","mossy_cobblestone_wall","mossy_stone_bricks","mycelium","nether_brick_fence","nether_brick_slab","nether_brick_stairs","nether_bricks","nether_portal","nether_quartz_ore","nether_wart","nether_wart_block","netherrack","note_block","oak_bark","oak_door","oak_fence","oak_fence_gate","oak_leaves","oak_log","oak_planks","oak_sapling","oak_slab","oak_stairs","observer","obsidian","orange_carpet","orange_concrete","orange_concrete_powder","orange_glazed_terracotta","orange_shulker_box","orange_stained_glass","orange_stained_glass_pane","orange_terracotta","orange_tulip","orange_wool","oxeye_daisy","packed_ice","peony","petrified_oak_slab","pink_carpet","pink_concrete","pink_concrete_powder","pink_glazed_terracotta","pink_shulker_box","pink_stained_glass","pink_stained_glass_pane","pink_terracotta","pink_tulip","pink_wool","piston","piston_head","player_head","podzol","polished_andesite","polished_diorite","polished_granite","poppy","potatoes","powered_rail","prismarine","prismarine_bricks","carved_pumpkin","pumpkin_stem","purple_carpet","purple_concrete","purple_concrete_powder","purple_glazed_terracotta","purple_shulker_box","purple_stained_glass","purple_stained_glass_pane","purple_terracotta","purple_wool","purpur_block","purpur_pillar","purpur_slab","purpur_stairs","smooth_quartz","quartz_pillar","quartz_slab","quartz_stairs","rail","red_carpet","red_concrete","red_concrete_powder","red_glazed_terracotta","red_mushroom","red_mushroom_block","red_nether_bricks","red_sand","red_sandstone","red_sandstone_slab","red_sandstone_stairs","red_shulker_box","red_stained_glass","red_stained_glass_pane","red_terracotta","red_tulip","red_wool","redstone_block","redstone_lamp","redstone_ore","redstone_torch","redstone_wire","repeater","repeating_command_block","rose_bush","sand","sandstone","sandstone_slab","sandstone_stairs","sea_lantern","sign","skeleton_skull","slime_block","smooth_red_sandstone","smooth_sandstone","smooth_stone","snow","snow_block","soul_sand","sponge","spruce_bark","spruce_door","spruce_fence","spruce_fence_gate","spruce_leaves","spruce_log","spruce_planks","spruce_sapling","spruce_slab","spruce_stairs","sticky_piston","stone","stone_brick_slab","stone_brick_stairs","stone_bricks","stone_button","stone_pressure_plate","stone_slab","structure_block","structure_void","sugar_cane","sunflower","tall_grass","terracotta","tnt","torch","trapped_chest","tripwire","tripwire_hook","vine","wall_sign","wall_torch","water","wet_sponge","wheat","white_carpet","white_concrete","white_concrete_powder","white_glazed_terracotta","white_shulker_box","white_stained_glass","white_stained_glass_pane","white_terracotta","white_tulip","white_wool","wither_skeleton_skull","oak_button","spruce_button","acacia_button","birch_button","dark_oak_button","jungle_button","oak_trapdoor","spruce_trapdoor","acacia_trapdoor","dark_oak_trapdoor","birch_trapdoor","jungle_trapdoor","yellow_carpet","yellow_concrete","yellow_concrete_powder","yellow_glazed_terracotta","yellow_shulker_box","yellow_stained_glass","yellow_stained_glass_pane","yellow_terracotta","yellow_wool","zombie_head","redstone_wall_torch","dark_oak_pressure_plate","acacia_pressure_plate","jungle_pressure_plate","spruce_pressure_plate","birch_pressure_plate","oak_pressure_plate","mushroom_stem","white_banner","orange_banner","magenta_banner","light_blue_banner","yellow_banner","lime_banner","pink_banner","gray_banner","light_gray_banner","cyan_banner","purple_banner","blue_banner","brown_banner","green_banner","red_banner","black_banner"]} 1
execute if data storage deathgames {shop_success_22:1} as @a[scores={shop=22}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"snare","objective":"prices"},"color":"gold"}]

execute as @a[scores={shop=23}] unless score @s money >= shield prices run scoreboard players operation @s nomoneymessage = shield prices
execute store success storage deathgames shop_success_23 int 1 as @a[scores={shop=23}] if score @s money >= shield prices run scoreboard players operation @s money -= shield prices
execute if data storage deathgames {shop_success_23:1} run give @a[scores={shop=23}] minecraft:shield{Damage:257} 1
execute if data storage deathgames {shop_success_23:1} as @a[scores={shop=23}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"shield","objective":"prices"},"color":"gold"}]

execute as @a[scores={shop=24}] unless score @s money >= life prices run scoreboard players operation @s nomoneymessage = life prices
execute store success storage deathgames shop_success_24 int 1 as @a[scores={shop=24}] if score @s money >= life prices run scoreboard players operation @s money -= life prices
execute if data storage deathgames {shop_success_24:1} run scoreboard players add @a[scores={shop=24}] livesshop 1
execute if data storage deathgames {shop_success_24:1} as @a[scores={shop=24}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"life","objective":"prices"},"color":"gold"}]

execute as @a[scores={shop=25}] unless score @s money >= trident prices run scoreboard players operation @s nomoneymessage = trident prices
execute store success storage deathgames shop_success_25 int 1 as @a[scores={shop=25}] if score @s money >= trident prices run scoreboard players operation @s money -= trident prices
execute if data storage deathgames {shop_success_25:1} run give @a[scores={shop=25}] minecraft:trident{Enchantments:[{id:"minecraft:loyalty",lvl:1}]} 1
execute if data storage deathgames {shop_success_25:1} as @a[scores={shop=25}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"trident","objective":"prices"},"color":"gold"}]

execute as @a[scores={shop=26}] unless score @s money >= milk prices run scoreboard players operation @s nomoneymessage = milk prices
execute store success storage deathgames shop_success_26 int 1 as @a[scores={shop=26}] if score @s money >= milk prices run scoreboard players operation @s money -= milk prices
execute if data storage deathgames {shop_success_26:1} run give @a[scores={shop=26}] minecraft:milk_bucket 1
execute if data storage deathgames {shop_success_26:1} as @a[scores={shop=26}] run tellraw @s [{"text":"You just spent "},{"score":{"name":"milk","objective":"prices"},"color":"gold"}]

#FREE 27
#FREE 28

#reset all success values
execute store result storage deathgames shop_success_1 int 1 run scoreboard players get 0 constants
execute store result storage deathgames shop_success_2 int 1 run scoreboard players get 0 constants
execute store result storage deathgames shop_success_3 int 1 run scoreboard players get 0 constants
execute store result storage deathgames shop_success_4 int 1 run scoreboard players get 0 constants
execute store result storage deathgames shop_success_5 int 1 run scoreboard players get 0 constants
execute store result storage deathgames shop_success_6 int 1 run scoreboard players get 0 constants
execute store result storage deathgames shop_success_7 int 1 run scoreboard players get 0 constants
execute store result storage deathgames shop_success_8 int 1 run scoreboard players get 0 constants
execute store result storage deathgames shop_success_9 int 1 run scoreboard players get 0 constants
execute store result storage deathgames shop_success_10 int 1 run scoreboard players get 0 constants
execute store result storage deathgames shop_success_11 int 1 run scoreboard players get 0 constants
execute store result storage deathgames shop_success_12 int 1 run scoreboard players get 0 constants
execute store result storage deathgames shop_success_13 int 1 run scoreboard players get 0 constants
execute store result storage deathgames shop_success_14 int 1 run scoreboard players get 0 constants
execute store result storage deathgames shop_success_15 int 1 run scoreboard players get 0 constants
execute store result storage deathgames shop_success_16 int 1 run scoreboard players get 0 constants
execute store result storage deathgames shop_success_17 int 1 run scoreboard players get 0 constants
execute store result storage deathgames shop_success_18 int 1 run scoreboard players get 0 constants
execute store result storage deathgames shop_success_19 int 1 run scoreboard players get 0 constants
execute store result storage deathgames shop_success_20 int 1 run scoreboard players get 0 constants
execute store result storage deathgames shop_success_21 int 1 run scoreboard players get 0 constants
execute store result storage deathgames shop_success_22 int 1 run scoreboard players get 0 constants
execute store result storage deathgames shop_success_23 int 1 run scoreboard players get 0 constants
execute store result storage deathgames shop_success_24 int 1 run scoreboard players get 0 constants
execute store result storage deathgames shop_success_25 int 1 run scoreboard players get 0 constants
execute store result storage deathgames shop_success_26 int 1 run scoreboard players get 0 constants
execute store result storage deathgames shop_success_27 int 1 run scoreboard players get 0 constants
execute store result storage deathgames shop_success_28 int 1 run scoreboard players get 0 constants

#reset value after done
scoreboard players set @a[scores={shop=1..}] shop 0

#nomoneymessage
execute as @a[scores={nomoneymessage=1..}] run tellraw @s [{"text":"Not enough money, you need "},{"score":{"name":"@s","objective":"nomoneymessage"}}]
scoreboard players set @a nomoneymessage 0

#clear buckets for milk buyers
clear @a[tag=!admin] bucket

#clear glass bottles for potion buyers
clear @a[tag=!admin] minecraft:glass_bottle

#fishing for lily pads
clear @a[tag=!admin] minecraft:lily_pad

#shop tp timer
#initiate teleport upon entering shop
execute as @a[tag=inshop,tag=ingame] if score running gamestate matches 1 unless score @s teleport matches 1.. run scoreboard players set @s teleport 1
#count ticks in shop
scoreboard players add @a[scores={teleport=1}] tptimer 1
#tp player out of shop into blackbox
execute as @a[scores={teleport=1}] if score @s tptimer >= shoptptime settings run scoreboard players set @s teleport 2
scoreboard players set @a[scores={teleport=2..}] tptimer 0
tp @a[gamemode=adventure,scores={teleport=2}] 0 50 0
scoreboard players set @a[scores={teleport=2..}] teleport 0
#calc time to teleport in seconds
execute as @a[scores={teleport=1}] store result score @s shopcountdown run scoreboard players get shoptptime settings
execute as @a[scores={teleport=1}] run scoreboard players operation @s shopcountdown -= @s tptimer
#round up
execute as @a[scores={teleport=1}] run scoreboard players operation @s shopcountdown += 20 constants
execute as @a[scores={teleport=1}] run scoreboard players operation @s shopcountdown /= 20 constants
#show countdown to player
execute as @a[scores={teleport=1}] run title @s times 0 3 0
execute as @a[scores={teleport=1}] run title @s subtitle [{"text":"t minus ","color":"dark_gray","bold":false},{"score":{"name":"@s","objective":"shopcountdown"},"color":"dark_gray","bold":false},{"text":" to teleport","color":"dark_gray","bold":false}]
execute as @a[scores={teleport=1}] run title @s title ""

#livesshopping
execute as @a[scores={livesshop=1..},team=orange] run scoreboard players add orange lives 1
execute as @a[scores={livesshop=1..},team=purple] run scoreboard players add purple lives 1
execute as @a[scores={livesshop=1..},team=lightblue] run scoreboard players add lightblue lives 1
execute as @a[scores={livesshop=1..},team=gray] run scoreboard players add gray lives 1
execute as @a[scores={livesshop=1..},team=blue] run scoreboard players add blue lives 1
execute as @a[scores={livesshop=1..},team=green] run scoreboard players add green lives 1
execute as @a[scores={livesshop=1..},team=red] run scoreboard players add red lives 1
execute as @a[scores={livesshop=1..},team=yellow] run scoreboard players add yellow lives 1
scoreboard players remove @a[scores={livesshop=1..}] livesshop 1