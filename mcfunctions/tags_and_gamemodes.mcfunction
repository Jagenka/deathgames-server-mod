#make in lobby players leave their teams
team leave @a[tag=inlobby,tag=!admin]

#spectators
gamemode spectator @a[tag=spectator,tag=!ingame]
tag @a[team=] remove ingame
tag @a[team=,tag=!admin] add spectator

#players
gamemode adventure @a[tag=ingame,team=!]

#admin stuff
tag @a[tag=admin] remove spectator
tag @a[tag=admin] remove ingame
team leave @a[tag=admin]

#players in barrier get damage
effect give @a[tag=ingame,tag=!inarena] minecraft:wither 1 9