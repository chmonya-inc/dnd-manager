package com.dnd.helper.domain.model

@Suppress("MaximumLineLength")
object ItemTemplates {
    val genericItems = listOf(
        Item(
            id = "template-health-potion",
            name = "Potion of Healing",
            slot = null,
            rarity = ItemRarity.COMMON,
            description = "You regain 2d4 + 2 hit points when you drink this potion. The potion's red liquid glimmers when agitated.",
            imageUrl = "https://www.dndbeyond.com/avatars/7/163/636272379768564177.jpeg"
        ),
        Item(
            id = "template-mana-potion",
            name = "Potion of Mana",
            slot = null,
            rarity = ItemRarity.UNCOMMON,
            description = "Regain 1d4 spell slots when consumed.",
            imageUrl = "https://www.dndbeyond.com/avatars/7/161/636272379568160000.jpeg"
        ),
        Item(
            id = "template-greater-health",
            name = "Potion of Greater Healing",
            slot = null,
            rarity = ItemRarity.UNCOMMON,
            description = "You regain 4d4 + 4 hit points when you drink this potion.",
            imageUrl = "https://www.dndbeyond.com/avatars/7/163/636272379768564177.jpeg"
        ),
        Item(
            id = "template-scroll-fireball",
            name = "Spell Scroll: Fireball",
            slot = null,
            rarity = ItemRarity.RARE,
            description = "A spell scroll bears the words of a single spell, " +
                "written in a mystical cipher. " +
                "If the spell is on your class's spell list, you can read the scroll " +
                "and cast its spell without providing any material components.",
            imageUrl = "https://www.dndbeyond.com/avatars/7/413/636272449033325000.jpeg"
        ),
        Item(
            id = "template-bag-of-holding",
            name = "Bag of Holding",
            slot = null,
            rarity = ItemRarity.UNCOMMON,
            description = "This bag has an interior space considerably larger than its outside dimensions, " +
                "roughly 2 feet in diameter at the mouth and 4 feet deep. " +
                "The bag can hold up to 500 pounds, not exceeding a volume of 64 cubic feet.",
            imageUrl = "https://www.dndbeyond.com/avatars/7/111/636272314545560000.jpeg"
        ),
        Item(
            id = "template-longsword-1",
            name = "Longsword +1",
            slot = EquipmentSlot.MAIN_HAND,
            rarity = ItemRarity.RARE,
            description = "You have a +1 bonus to attack and damage rolls made with this magic weapon.",
            imageUrl = null
        ),
        Item(
            id = "template-shield-1",
            name = "Shield +1",
            slot = EquipmentSlot.OFF_HAND,
            rarity = ItemRarity.RARE,
            description = "While holding this shield, you have a +1 bonus to AC. This bonus is in addition to the shield's normal bonus to AC.",
            imageUrl = null
        )
    )
}
