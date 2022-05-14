#remove all display armor stands
kill @e[x=0,y=58,z=0,distance=0..20,type=minecraft:armor_stand]

#south wall
execute positioned -7 58 8 run summon minecraft:armor_stand ~ ~-1.7 ~-.26 {Marker:1b,NoBasePlate:1b,Invisible:1b,Pose:{Head:[0f,0f,0f]},ArmorItems:[{},{},{},{id:tipped_arrow,Count:1,tag:{Potion:"minecraft:poison"}}]}
execute positioned -5 58 8 run summon minecraft:armor_stand ~ ~-1.7 ~-.26 {Marker:1b,NoBasePlate:1b,Invisible:1b,Pose:{Head:[0f,0f,0f]},ArmorItems:[{},{},{},{id:arrow,Count:1}]}
execute positioned -3 58 8 run summon minecraft:armor_stand ~ ~-1.7 ~-.26 {Marker:1b,NoBasePlate:1b,Invisible:1b,Pose:{Head:[0f,0f,0f]},ArmorItems:[{},{},{},{id:bow,Count:1}]}
execute positioned -1 58 8 run summon minecraft:armor_stand ~ ~-1.7 ~-.26 {Marker:1b,NoBasePlate:1b,Invisible:1b,Pose:{Head:[0f,0f,0f]},ArmorItems:[{},{},{},{id:crossbow,Count:1}]}
execute positioned 1 58 8 run summon minecraft:armor_stand ~ ~-1.7 ~-.26 {Marker:1b,NoBasePlate:1b,Invisible:1b,Pose:{Head:[0f,0f,0f]},ArmorItems:[{},{},{},{id:iron_chestplate,Count:1}]}
execute positioned 3 58 8 run summon minecraft:armor_stand ~ ~-1.7 ~-.26 {Marker:1b,NoBasePlate:1b,Invisible:1b,Pose:{Head:[0f,0f,0f]},ArmorItems:[{},{},{},{id:iron_axe,Count:1}]}
execute positioned 5 58 8 run summon minecraft:armor_stand ~ ~-1.7 ~-.26 {Marker:1b,NoBasePlate:1b,Invisible:1b,Pose:{Head:[0f,0f,0f]},ArmorItems:[{},{},{},{id:iron_sword,Count:1}]}
#shield
execute positioned 7 58 8 run summon armor_stand ~-0.05 ~-0.85 ~-0.250000001 {Invisible:1b,Invulnerable:1b,PersistenceRequired:1b,NoBasePlate:1b,NoGravity:1b,ShowArms:1b,Marker:1b,Rotation:[180f],ArmorItems:[{},{},{},{}],HandItems:[{},{id:"shield",Count:1b}],Pose:{LeftArm:[0f,90f,90f]}}
#execute positioned 327 58 376 run summon minecraft:armor_stand ~ ~-1.7 ~-.26 {Marker:1b,NoBasePlate:1b,Invisible:1b,Pose:{Head:[0f,0f,0f]},ArmorItems:[{},{},{},{id:stick,Count:1}]}

#north wall
execute positioned -7 58 -8 run summon minecraft:armor_stand ~ ~-1.7 ~.25 {Marker:1b,NoBasePlate:1b,Invisible:1b,Pose:{Head:[0f,180f,0f]},ArmorItems:[{},{},{},{id:potion,Count:1,tag:{Potion:"minecraft:invisibility"}}]}
execute positioned -5 58 -8 run summon minecraft:armor_stand ~ ~-1.7 ~.25 {Marker:1b,NoBasePlate:1b,Invisible:1b,Pose:{Head:[0f,180f,0f]},ArmorItems:[{},{},{},{id:potion,Count:1,tag:{Potion:"minecraft:strength"}}]}
execute positioned -3 58 -8 run summon minecraft:armor_stand ~ ~-1.7 ~.25 {Marker:1b,NoBasePlate:1b,Invisible:1b,Pose:{Head:[0f,180f,0f]},ArmorItems:[{},{},{},{id:potion,Count:1,tag:{Potion:"minecraft:swiftness"}}]}
execute positioned -1 58 -8 run summon minecraft:armor_stand ~ ~-1.7 ~.25 {Marker:1b,NoBasePlate:1b,Invisible:1b,Pose:{Head:[0f,180f,0f]},ArmorItems:[{},{},{},{id:potion,Count:1,tag:{Potion:"minecraft:regeneration"}}]}
execute positioned 1 58 -8 run summon minecraft:armor_stand ~ ~-1.7 ~.25 {Marker:1b,NoBasePlate:1b,Invisible:1b,Pose:{Head:[0f,180f,0f]},ArmorItems:[{},{},{},{id:potion,Count:1,tag:{Potion:"minecraft:fire_resistance"}}]}
execute positioned 3 58 -8 run summon minecraft:armor_stand ~ ~-1.7 ~.25 {Marker:1b,NoBasePlate:1b,Invisible:1b,Pose:{Head:[0f,180f,0f]},ArmorItems:[{},{},{},{id:lingering_potion,Count:1,tag:{Potion:"minecraft:harming"}}]}
execute positioned 5 58 -8 run summon minecraft:armor_stand ~ ~-1.7 ~.25 {Marker:1b,NoBasePlate:1b,Invisible:1b,Pose:{Head:[0f,180f,0f]},ArmorItems:[{},{},{},{id:splash_potion,Count:1,tag:{Potion:"minecraft:slowness"}}]}
execute positioned 7 58 -8 run summon minecraft:armor_stand ~ ~-1.7 ~.25 {Marker:1b,NoBasePlate:1b,Invisible:1b,Pose:{Head:[0f,180f,0f]},ArmorItems:[{},{},{},{id:enchanted_golden_apple,Count:1}]}

#east wall
execute positioned 8 58 -5 run summon minecraft:armor_stand ~-.26 ~-1.7 ~ {Marker:1b,NoBasePlate:1b,Invisible:1b,Rotation:[90f],Pose:{Head:[0f,180f,0f]},ArmorItems:[{},{},{},{id:melon_slice,Count:1}]}
execute positioned 8 58 -3 run summon minecraft:armor_stand ~-.26 ~-1.7 ~ {Marker:1b,NoBasePlate:1b,Invisible:1b,Rotation:[90f],Pose:{Head:[0f,180f,0f]},ArmorItems:[{},{},{},{id:cooked_beef,Count:1}]}
execute positioned 8 58 -1 run summon minecraft:armor_stand ~-.26 ~-1.7 ~ {Marker:1b,NoBasePlate:1b,Invisible:1b,Rotation:[90f],Pose:{Head:[0f,180f,0f]},ArmorItems:[{},{},{},{id:milk_bucket,Count:1}]}
execute positioned 8 58 1 run summon minecraft:armor_stand ~-.26 ~-1.7 ~ {Marker:1b,NoBasePlate:1b,Invisible:1b,Rotation:[90f],Pose:{Head:[0f,180f,0f]},ArmorItems:[{},{},{},{id:ender_pearl,Count:1}]}
execute positioned 8 58 3 run summon minecraft:armor_stand ~-.26 ~-1.7 ~ {Marker:1b,NoBasePlate:1b,Invisible:1b,Rotation:[90f],Pose:{Head:[0f,180f,0f]},ArmorItems:[{},{},{},{id:barrier,Count:1}]}
execute positioned 8 58 5 run summon minecraft:armor_stand ~-.26 ~-1.7 ~ {Marker:1b,NoBasePlate:1b,Invisible:1b,Rotation:[90f],Pose:{Head:[0f,180f,0f]},ArmorItems:[{},{},{},{id:barrier,Count:1}]}

#west wall
execute positioned -8 58 -5 run summon minecraft:armor_stand ~.25 ~-1.7 ~ {Marker:1b,NoBasePlate:1b,Invisible:1b,Rotation:[90f],Pose:{Head:[0f,0f,0f]},ArmorItems:[{},{},{},{id:fishing_rod,Count:1}]}
execute positioned -8 58 -3 run summon minecraft:armor_stand ~.25 ~-1.7 ~ {Marker:1b,NoBasePlate:1b,Invisible:1b,Rotation:[90f],Pose:{Head:[0f,0f,0f]},ArmorItems:[{},{},{},{id:blaze_rod,Count:1}]}
#trident
execute positioned -8 58 -1 run summon armor_stand ~0.15 ~-1.5 ~0.79 {Invisible:1b,Invulnerable:1b,PersistenceRequired:1b,NoBasePlate:1b,NoGravity:1b,ShowArms:1b,Marker:1b,Rotation:[-60f],HandItems:[{},{id:"trident",Count:1}],Pose:{LeftArm:[0f,-80f,-84f]}}
#execute positioned 312 58 367 run summon minecraft:armor_stand ~.25 ~-1.7 ~ {Marker:1b,NoBasePlate:1b,Invisible:1b,Rotation:[90f],Pose:{Head:[0f,0f,0f]},ArmorItems:[{},{},{},{id:stick,Count:1}]}
execute positioned -8 58 1 run summon minecraft:armor_stand ~.25 ~-1.7 ~ {Marker:1b,NoBasePlate:1b,Invisible:1b,Rotation:[90f],Pose:{Head:[0f,0f,0f]},ArmorItems:[{},{},{},{id:bat_spawn_egg,Count:1}]}
execute positioned -8 58 3 run summon minecraft:armor_stand ~.25 ~-1.7 ~ {Marker:1b,NoBasePlate:1b,Invisible:1b,Rotation:[90f],Pose:{Head:[0f,0f,0f]},ArmorItems:[{},{},{},{id:totem_of_undying,Count:1}]}
execute positioned -8 58 5 run summon minecraft:armor_stand ~.25 ~-1.7 ~ {Marker:1b,NoBasePlate:1b,Invisible:1b,Rotation:[90f],Pose:{Head:[0f,0f,0f]},ArmorItems:[{},{},{},{id:turtle_egg,Count:1}]}
