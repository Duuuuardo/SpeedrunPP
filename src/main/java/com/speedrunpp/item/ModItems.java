package com.speedrunpp.item;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class ModItems {
    public static final Item PLAYER_TRACKER = Registry.register(
            BuiltInRegistries.ITEM,
            Identifier.of("speedrunpp", "player_tracker"),
            new PlayerTrackerCompassItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.UNCOMMON))
    );

    public static void register() {
        CreativeModeTabEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.accept(PLAYER_TRACKER);
        });
    }
}
