package com.speedrunpp.item;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class ModItems {
    private static final Identifier PLAYER_TRACKER_ID = Identifier.fromNamespaceAndPath("speedrunpp", "player_tracker");

    public static final Item PLAYER_TRACKER = Registry.register(
            BuiltInRegistries.ITEM,
            PLAYER_TRACKER_ID,
            new PlayerTrackerCompassItem(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, PLAYER_TRACKER_ID))
                    .stacksTo(1)
                    .rarity(Rarity.UNCOMMON))
    );

    public static void register() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> {
            output.accept(PLAYER_TRACKER);
        });
    }
}
