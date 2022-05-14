#armor
execute as @a[scores={armor=-1}] run clear @s #deathgames:armor
execute as @a[scores={armor=-1}] run replaceitem entity @s armor.head air
execute as @a[scores={armor=-1}] run replaceitem entity @s armor.chest air
execute as @a[scores={armor=-1}] run replaceitem entity @s armor.legs air
execute as @a[scores={armor=-1}] run replaceitem entity @s armor.feet air
execute as @a[scores={armor=-1}] run scoreboard players add @s armor 1
execute as @a[scores={armor=1}] run replaceitem entity @s armor.head minecraft:leather_helmet{Unbreakable:1}
execute as @a[scores={armor=1}] run replaceitem entity @s armor.legs minecraft:leather_leggings{Unbreakable:1}
execute as @a[scores={armor=1}] run scoreboard players add @s armor 1
execute as @a[scores={armor=3}] run replaceitem entity @s armor.chest minecraft:leather_chestplate{Unbreakable:1}
execute as @a[scores={armor=3}] run replaceitem entity @s armor.feet minecraft:leather_boots{Unbreakable:1}
execute as @a[scores={armor=3}] run scoreboard players add @s armor 1
execute as @a[scores={armor=5}] run replaceitem entity @s armor.head minecraft:iron_helmet{Unbreakable:1}
execute as @a[scores={armor=5}] run replaceitem entity @s armor.legs minecraft:iron_leggings{Unbreakable:1}
execute as @a[scores={armor=5}] run scoreboard players add @s armor 1
execute as @a[scores={armor=7}] run replaceitem entity @s armor.chest minecraft:iron_chestplate{Unbreakable:1}
execute as @a[scores={armor=7}] run replaceitem entity @s armor.feet minecraft:iron_boots{Unbreakable:1}
execute as @a[scores={armor=7}] run scoreboard players add @s armor 1
execute as @a[scores={armor=9}] run replaceitem entity @s armor.head minecraft:diamond_helmet{Unbreakable:1}
execute as @a[scores={armor=9}] run replaceitem entity @s armor.legs minecraft:diamond_leggings{Unbreakable:1}
execute as @a[scores={armor=9}] run scoreboard players add @s armor 1
execute as @a[scores={armor=11}] run replaceitem entity @s armor.chest minecraft:diamond_chestplate{Unbreakable:1}
execute as @a[scores={armor=11}] run replaceitem entity @s armor.feet minecraft:diamond_boots{Unbreakable:1}
execute as @a[scores={armor=11}] run scoreboard players add @s armor 1
execute as @a[scores={armor=13..}] run scoreboard players set @s armor 12

#sword
execute as @a[scores={sword=-1}] run clear @s #deathgames:swords
execute as @a[scores={sword=-1}] run scoreboard players add @s sword 1
execute as @a[scores={sword=1}] run give @s minecraft:wooden_sword{Unbreakable:1}
execute as @a[scores={sword=1}] run scoreboard players add @s sword 1
execute as @a[scores={sword=3}] run clear @s minecraft:wooden_sword 1
execute as @a[scores={sword=3}] run give @s minecraft:stone_sword{Unbreakable:1}
execute as @a[scores={sword=3}] run scoreboard players add @s sword 1
execute as @a[scores={sword=5}] run clear @s minecraft:stone_sword 1
execute as @a[scores={sword=5}] run give @s minecraft:iron_sword{Unbreakable:1}
execute as @a[scores={sword=5}] run scoreboard players add @s sword 1
execute as @a[scores={sword=7}] run clear @s minecraft:iron_sword 1
execute as @a[scores={sword=7}] run give @s minecraft:diamond_sword{Unbreakable:1}
execute as @a[scores={sword=7}] run scoreboard players add @s sword 1
execute as @a[scores={sword=9}] run clear @s minecraft:diamond_sword 1
execute as @a[scores={sword=9}] run give @s minecraft:netherite_sword{Unbreakable:1}
execute as @a[scores={sword=9}] run scoreboard players add @s sword 1
execute as @a[scores={sword=11..}] run scoreboard players set @s sword 10

#axe
execute as @a[scores={axe=-1}] run clear @s #deathgames:axes
execute as @a[scores={axe=-1}] run scoreboard players add @s axe 1
execute as @a[scores={axe=1}] run give @s minecraft:wooden_axe{Unbreakable:1}
execute as @a[scores={axe=1}] run scoreboard players add @s axe 1
execute as @a[scores={axe=3}] run clear @s minecraft:wooden_axe 1
execute as @a[scores={axe=3}] run give @s minecraft:stone_axe{Unbreakable:1}
execute as @a[scores={axe=3}] run scoreboard players add @s axe 1
execute as @a[scores={axe=5}] run clear @s minecraft:stone_axe 1
execute as @a[scores={axe=5}] run give @s minecraft:iron_axe{Unbreakable:1}
execute as @a[scores={axe=5}] run scoreboard players add @s axe 1
execute as @a[scores={axe=7}] run clear @s minecraft:iron_axe 1
execute as @a[scores={axe=7}] run give @s minecraft:diamond_axe{Unbreakable:1}
execute as @a[scores={axe=7}] run scoreboard players add @s axe 1
execute as @a[scores={axe=9}] run clear @s minecraft:diamond_axe 1
execute as @a[scores={axe=9}] run give @s minecraft:netherite_axe{Unbreakable:1}
execute as @a[scores={axe=9}] run scoreboard players add @s axe 1
execute as @a[scores={axe=11..}] run scoreboard players set @s axe 10

#bow
execute as @a[scores={bow=-1}] run clear @s minecraft:bow 1
execute as @a[scores={bow=-1}] run scoreboard players add @s bow 1
execute as @a[scores={bow=1}] run give @s minecraft:bow{Unbreakable:1}
execute as @a[scores={bow=1}] run scoreboard players add @s bow 1
execute as @a[scores={bow=3}] run clear @s minecraft:bow 1
execute as @a[scores={bow=3}] run give @s minecraft:bow{Unbreakable:1,Enchantments:[{id:"minecraft:power",lvl:1}]} 1
execute as @a[scores={bow=3}] run scoreboard players add @s bow 1
execute as @a[scores={bow=5}] run clear @s minecraft:bow 1
execute as @a[scores={bow=5}] run give @s minecraft:bow{Unbreakable:1,Enchantments:[{id:"minecraft:power",lvl:2}]} 1
execute as @a[scores={bow=5}] run scoreboard players add @s bow 1
execute as @a[scores={bow=7}] run clear @s minecraft:bow 1
execute as @a[scores={bow=7}] run give @s minecraft:bow{Unbreakable:1,Enchantments:[{id:"minecraft:power",lvl:3}]} 1
execute as @a[scores={bow=7}] run scoreboard players add @s bow 1
execute as @a[scores={bow=9}] run clear @s minecraft:bow 1
execute as @a[scores={bow=9}] run give @s minecraft:bow{Unbreakable:1,Enchantments:[{id:"minecraft:power",lvl:4}]} 1
execute as @a[scores={bow=9}] run scoreboard players add @s bow 1
execute as @a[scores={bow=11}] run clear @s minecraft:bow 1
execute as @a[scores={bow=11}] run give @s minecraft:bow{Unbreakable:1,Enchantments:[{id:"minecraft:power",lvl:5}]} 1
execute as @a[scores={bow=11}] run scoreboard players add @s bow 1
execute as @a[scores={bow=13..}] run scoreboard players set @s bow 12

#crossbow
execute as @a[scores={crossbow=-1}] run clear @s minecraft:crossbow 1
execute as @a[scores={crossbow=-1}] run scoreboard players add @s crossbow 1
execute as @a[scores={crossbow=1}] run give @s minecraft:crossbow{Unbreakable:1}
execute as @a[scores={crossbow=1}] run scoreboard players add @s crossbow 1
execute as @a[scores={crossbow=3}] run clear @s minecraft:crossbow 1
execute as @a[scores={crossbow=3}] run give @s minecraft:crossbow{Unbreakable:1,Enchantments:[{id:"minecraft:quick_charge",lvl:1}]}
execute as @a[scores={crossbow=3}] run scoreboard players add @s crossbow 1
execute as @a[scores={crossbow=5}] run clear @s minecraft:crossbow 1
execute as @a[scores={crossbow=5}] run give @s minecraft:crossbow{Unbreakable:1,Enchantments:[{id:"minecraft:quick_charge",lvl:2}]}
execute as @a[scores={crossbow=5}] run scoreboard players add @s crossbow 1
execute as @a[scores={crossbow=7}] run clear @s minecraft:crossbow 1
execute as @a[scores={crossbow=7}] run give @s minecraft:crossbow{Unbreakable:1,Enchantments:[{id:"minecraft:quick_charge",lvl:3}]}
execute as @a[scores={crossbow=7}] run scoreboard players add @s crossbow 1
execute as @a[scores={crossbow=9}] run clear @s minecraft:crossbow 1
execute as @a[scores={crossbow=9}] run give @s minecraft:crossbow{Unbreakable:1,Enchantments:[{id:"minecraft:quick_charge",lvl:4}]}
execute as @a[scores={crossbow=9}] run scoreboard players add @s crossbow 1
execute as @a[scores={crossbow=11}] run clear @s minecraft:crossbow 1
execute as @a[scores={crossbow=11}] run give @s minecraft:crossbow{Unbreakable:1,Enchantments:[{id:"minecraft:quick_charge",lvl:5}]}
execute as @a[scores={crossbow=11}] run scoreboard players add @s crossbow 1
execute as @a[scores={crossbow=13..}] run scoreboard players set @s crossbow 12