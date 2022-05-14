scoreboard players add shuffle timer 1
execute if score shuffle timer >= shuffleinterval settings if score timesincelastkill timer >= shufflekilldelay settings run function deathgames:shufflespawns
execute if score shuffle timer >= shuffleinterval settings if score timesincelastkill timer >= shufflekilldelay settings run scoreboard players set shuffle timer 0


#execute if score shufflespawns gamestate matches 1 if score timesincelastkill timer >= shufflekilldelay settings