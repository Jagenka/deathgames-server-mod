package de.jagenka.shop

import de.jagenka.TrapItems
import de.jagenka.timer.minutes
import de.jagenka.timer.seconds
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items.*
import net.minecraft.potion.PotionUtil
import net.minecraft.potion.Potions
import net.minecraft.text.Text

object ShopEntries
{
    val EMPTY = ItemShopEntry(ItemStack.EMPTY, 0, "")

    val shopEntries: Map<Int, ShopEntry>
        get()
        {
            val entries = mutableMapOf(
                slot(3, 0) to ItemShopEntry(ItemStack(MELON_SLICE, 8), 5, "Melon"),
                slot(0, 8) to ItemShopEntry(ItemStack(ARROW, 4), 5, "Arrows"),
                slot(3, 4) to ItemShopEntry(ItemStack(ENDER_PEARL, 2), 10, "Ender Pearls"),
                slot(3, 1) to ItemShopEntry(ItemStack(COOKED_BEEF, 1), 10, "Beef"),
                slot(4, 7) to ItemShopEntry(ItemStack(FISHING_ROD), 30, "Fishing Rod"),
                slot(4, 6) to ItemShopEntry(ItemStack(BLAZE_ROD).withEnchantment(Enchantments.FIRE_ASPECT, 2), 40, "420 Blaze it"),
                slot(2, 0) to ItemShopEntry(
                    PotionUtil.setCustomPotionEffects(
                        POTION.defaultStack.withName("Potion of Invisibility"),
                        listOf(StatusEffectInstance(StatusEffects.INVISIBILITY, 60.seconds()))
                    ), 35, "Invis"
                ),
                slot(2, 1) to ItemShopEntry(
                    PotionUtil.setCustomPotionEffects(POTION.defaultStack.withName("Potion of Speed"), listOf(StatusEffectInstance(StatusEffects.SPEED, 3.minutes()))),
                    30,
                    "Speed"
                ),
                slot(2, 2) to ItemShopEntry(
                    PotionUtil.setCustomPotionEffects(
                        POTION.defaultStack.withName("Potion of Fire Resistance"),
                        listOf(StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 3.minutes()))
                    ), 20, "Fire Res"
                ),
                slot(2, 3) to ItemShopEntry(
                    PotionUtil.setCustomPotionEffects(POTION.defaultStack.withName("Potion of Strength"), listOf(StatusEffectInstance(StatusEffects.STRENGTH, 3.minutes()))),
                    45,
                    "Strength"
                ),
                slot(2, 4) to ItemShopEntry(
                    PotionUtil.setCustomPotionEffects(
                        POTION.defaultStack.withName("Potion of Regeneration"),
                        listOf(StatusEffectInstance(StatusEffects.REGENERATION, 3.minutes()))
                    ), 25, "Regen"
                ),
                slot(2, 5) to ItemShopEntry(
                    PotionUtil.setCustomPotionEffects(
                        POTION.defaultStack.withName("Potion of Dolphin's Grace"),
                        listOf(StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, 3.minutes()))
                    ), 20, "Aquadynamics"
                ),
                slot(2, 6) to ItemShopEntry(
                    PotionUtil.setCustomPotionEffects(
                        LINGERING_POTION.defaultStack.withName("Lingering Potion of Harming"),
                        listOf(StatusEffectInstance(StatusEffects.INSTANT_DAMAGE))
                    ),
                    20,
                    "Harming"
                ), // lingering
                slot(2, 7) to ItemShopEntry(
                    PotionUtil.setCustomPotionEffects(
                        SPLASH_POTION.defaultStack.withName("Splash Potion of Slowness"),
                        listOf(StatusEffectInstance(StatusEffects.SLOWNESS, 1.minutes() + 30.seconds()))
                    ),
                    40,
                    "Slowness"
                ),
                slot(3, 2) to ItemShopEntry(ItemStack(ENCHANTED_GOLDEN_APPLE), 150, "Gold Apple"),
                slot(4, 0) to ItemShopEntry(ItemStack(TOTEM_OF_UNDYING), 150, "Totem"),
                slot(1, 8) to ItemShopEntry(PotionUtil.setPotion(ItemStack(TIPPED_ARROW, 4), Potions.POISON), 25, "Poison Arrow"),
                slot(3, 7) to ItemShopEntry(TrapItems.SNARE_TRAP.item, 10, "Stop right there, criminal scum!"),
                slot(3, 8) to ItemShopEntry(TrapItems.VOID_TRAP.item, 10, "The Abyss"),
                slot(0, 3) to ItemShopEntry(ItemStack(SHIELD).withDamage(217), 50, "Shield"),
                slot(4, 1) to ExtraLifeShopEntry(TURTLE_EGG.defaultStack, 100, "Extra Life"),
                slot(4, 8) to ItemShopEntry(ItemStack(TRIDENT), 69_420, "Trident"),
                slot(2, 8) to ItemShopEntry(ItemStack(MILK_BUCKET), 5, "An Lüter Mülsch"),

                slot(0, 0) to UpgradeableShopEntry(
                    UpgradeType.ARMOR,
                    mutableListOf(
                        mutableListOf(LEATHER_HELMET.unbreakable(), LEATHER_CHESTPLATE.unbreakable(), LEATHER_LEGGINGS.unbreakable(), LEATHER_BOOTS.unbreakable()),
                        mutableListOf(IRON_HELMET.unbreakable(), IRON_LEGGINGS.unbreakable()),
                        mutableListOf(IRON_CHESTPLATE.unbreakable(), IRON_BOOTS.unbreakable()),
                        mutableListOf(DIAMOND_HELMET.unbreakable(), DIAMOND_LEGGINGS.unbreakable()),
                        mutableListOf(DIAMOND_CHESTPLATE.unbreakable(), DIAMOND_BOOTS.unbreakable()),
                    ),
                    mutableListOf(40, 60, 80, 100, 120),
                    "Armor Upgrade"
                ),
                slot(0, 1) to UpgradeableShopEntry(
                    UpgradeType.SWORD,
                    mutableListOf(
                        mutableListOf(WOODEN_SWORD.unbreakable()),
                        mutableListOf(STONE_SWORD.unbreakable()),
                        mutableListOf(IRON_SWORD.unbreakable()),
                        mutableListOf(DIAMOND_SWORD.unbreakable()),
                        mutableListOf(NETHERITE_SWORD.unbreakable()),
                    ),
                    mutableListOf(50, 50, 50, 50, 50),
                    "Sword Upgrade"
                ),
                slot(0, 2) to UpgradeableShopEntry(
                    UpgradeType.AXE,
                    mutableListOf(
                        mutableListOf(WOODEN_AXE.unbreakable()),
                        mutableListOf(STONE_AXE.unbreakable()),
                        mutableListOf(IRON_AXE.unbreakable()),
                        mutableListOf(DIAMOND_AXE.unbreakable()),
                        mutableListOf(NETHERITE_AXE.unbreakable()),
                    ),
                    mutableListOf(120, 40, 40, 40, 40),
                    "Axe Upgrade"
                ),
                slot(0, 6) to UpgradeableShopEntry(
                    UpgradeType.BOW,
                    mutableListOf(
                        mutableListOf(BOW.unbreakable()),
                        mutableListOf(BOW.unbreakable().withEnchantment(Enchantments.POWER, 1)),
                        mutableListOf(BOW.unbreakable().withEnchantment(Enchantments.POWER, 2)),
                        mutableListOf(BOW.unbreakable().withEnchantment(Enchantments.POWER, 3)),
                        mutableListOf(BOW.unbreakable().withEnchantment(Enchantments.POWER, 4)),
                        mutableListOf(BOW.unbreakable().withEnchantment(Enchantments.POWER, 5)),
                    ),
                    mutableListOf(75, 100, 100, 100, 100, 100),
                    "Bow Upgrade"
                ),
                slot(0, 7) to UpgradeableShopEntry(
                    UpgradeType.CROSSBOW,
                    mutableListOf(
                        mutableListOf(CROSSBOW.unbreakable()),
                        mutableListOf(CROSSBOW.unbreakable().withEnchantment(Enchantments.QUICK_CHARGE, 1)),
                        mutableListOf(CROSSBOW.unbreakable().withEnchantment(Enchantments.QUICK_CHARGE, 2)),
                        mutableListOf(CROSSBOW.unbreakable().withEnchantment(Enchantments.QUICK_CHARGE, 3)),
                        mutableListOf(CROSSBOW.unbreakable().withEnchantment(Enchantments.QUICK_CHARGE, 4)),
                        mutableListOf(CROSSBOW.unbreakable().withEnchantment(Enchantments.QUICK_CHARGE, 5)),
                    ),
                    mutableListOf(75, 25, 50, 100, 150, 150),
                    "Crossbow Upgrade"
                ),
                slot(5, 8) to LeaveShopEntry()
            )

            entries[slot(0, 0)]?.let { entries[slot(1, 0)] = RefundShopEntry(it) }
            entries[slot(0, 1)]?.let { entries[slot(1, 1)] = RefundShopEntry(it) }
            entries[slot(0, 2)]?.let { entries[slot(1, 2)] = RefundShopEntry(it) }
            entries[slot(0, 6)]?.let { entries[slot(1, 6)] = RefundShopEntry(it) }
            entries[slot(0, 7)]?.let { entries[slot(1, 7)] = RefundShopEntry(it) }

            return entries.toMap()
        }

    private fun Item.unbreakable(): ItemStack = ItemStack(this).makeUnbreakable()

    private fun ItemStack.makeUnbreakable(): ItemStack
    {
        this.orCreateNbt.putInt("Unbreakable", 1)
        return this
    }

    private fun ItemStack.withEnchantment(enchantment: Enchantment, level: Int): ItemStack
    {
        this.addEnchantment(enchantment, level)
        return this
    }

    private fun ItemStack.withName(name: String): ItemStack
    {
        this.setCustomName(Text.of(name))
        return this
    }

    private fun ItemStack.withDamage(damage: Int): ItemStack
    {
        this.damage = damage
        return this
    }

    private fun slot(row: Int, column: Int): Int
    {
        return (row * 9 + column).coerceAtLeast(0).coerceAtMost(Shop.slotAmount)
    }
}