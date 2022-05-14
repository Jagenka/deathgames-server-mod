#shift if allready occupied
execute if score rnd shufflespawns >= spawn1 shufflespawns run scoreboard players add rnd shufflespawns 1
execute if score rnd shufflespawns >= spawn2 shufflespawns run scoreboard players add rnd shufflespawns 1
execute if score rnd shufflespawns >= spawn3 shufflespawns run scoreboard players add rnd shufflespawns 1
execute if score rnd shufflespawns >= spawn4 shufflespawns run scoreboard players add rnd shufflespawns 1
execute if score rnd shufflespawns >= spawn5 shufflespawns run scoreboard players add rnd shufflespawns 1
execute if score rnd shufflespawns >= spawn6 shufflespawns run scoreboard players add rnd shufflespawns 1
execute if score rnd shufflespawns >= spawn7 shufflespawns run scoreboard players add rnd shufflespawns 1
execute if score rnd shufflespawns >= spawn8 shufflespawns run scoreboard players add rnd shufflespawns 1

#spawn no longer free
execute if score rnd shufflespawns matches 0 run scoreboard players set spawn1 shufflespawns 0
execute if score rnd shufflespawns matches 1 run scoreboard players set spawn2 shufflespawns 1
execute if score rnd shufflespawns matches 2 run scoreboard players set spawn3 shufflespawns 2
execute if score rnd shufflespawns matches 3 run scoreboard players set spawn4 shufflespawns 3
execute if score rnd shufflespawns matches 4 run scoreboard players set spawn5 shufflespawns 4
execute if score rnd shufflespawns matches 5 run scoreboard players set spawn6 shufflespawns 5
execute if score rnd shufflespawns matches 6 run scoreboard players set spawn7 shufflespawns 6
execute if score rnd shufflespawns matches 7 run scoreboard players set spawn8 shufflespawns 7