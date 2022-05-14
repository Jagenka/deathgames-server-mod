function deathgames:calc_upgradesum
execute as @a[scores={refundrequest=1,armor=1..}] run function deathgames:refund_armor
execute as @a[scores={refundrequest=2,sword=1..}] run function deathgames:refund_sword
execute as @a[scores={refundrequest=3,axe=1..}] run function deathgames:refund_axe
execute as @a[scores={refundrequest=4,bow=1..}] run function deathgames:refund_bow
execute as @a[scores={refundrequest=5,crossbow=1..}] run function deathgames:refund_crossbow