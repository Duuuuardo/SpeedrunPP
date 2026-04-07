package com.speedrunpp.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ModItems {
    public static final Item PLAYER_TRACKER = Registry.register(
            Registries.ITEM,
            Identifier.of("speedrunpp", "player_tracker"),
            new PlayerTrackerCompassItem(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON))
    );

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(PLAYER_TRACKER);
        });
    }
}
