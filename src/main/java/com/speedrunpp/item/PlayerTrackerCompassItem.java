package com.speedrunpp.item;

import com.speedrunpp.SpeedrunState;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class PlayerTrackerCompassItem extends Item {

    public PlayerTrackerCompassItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            MinecraftServer server = level.getServer();
            if (server == null) return InteractionResult.PASS;

            SpeedrunState state = SpeedrunState.get(server);
            List<ServerPlayer> onlinePlayers = server.getPlayerList().getPlayers()
                    .stream()
                    .filter(p -> p != serverPlayer)
                    .toList();

            if (onlinePlayers.isEmpty()) {
                serverPlayer.sendSystemMessage(
                        Component.translatable("item.speedrunpp.player_tracker.no_players")
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return InteractionResult.FAIL;
            }

            UUID currentTarget = state.getTrackerTarget(serverPlayer.getUUID());
            int currentIndex = -1;

            if (currentTarget != null) {
                for (int i = 0; i < onlinePlayers.size(); i++) {
                    if (onlinePlayers.get(i).getUUID().equals(currentTarget)) {
                        currentIndex = i;
                        break;
                    }
                }
            }

            int nextIndex = (currentIndex + 1) % onlinePlayers.size();
            ServerPlayer targetPlayer = onlinePlayers.get(nextIndex);

            state.setTrackerTarget(serverPlayer.getUUID(), targetPlayer.getUUID());

            serverPlayer.sendSystemMessage(
                    Component.translatable("item.speedrunpp.player_tracker.tracking",
                                    targetPlayer.getName().getString())
                            .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
                    true
            );

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (entity instanceof ServerPlayer player && slot == EquipmentSlot.MAINHAND) {
            if (level.getGameTime() % 20 != 0) return;

            MinecraftServer server = level.getServer();
            if (server == null) return;

            SpeedrunState state = SpeedrunState.get(server);
            UUID targetId = state.getTrackerTarget(player.getUUID());
            if (targetId == null) return;

            ServerPlayer target = server.getPlayerList().getPlayer(targetId);
            if (target == null) {
                player.sendSystemMessage(
                        Component.literal("Target offline")
                                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
                        true
                );
                return;
            }

            double dx = target.getX() - player.getX();
            double dz = target.getZ() - player.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            String direction = getCardinalDirection(dx, dz);

            player.sendSystemMessage(
                    Component.translatable("item.speedrunpp.player_tracker.direction",
                                    target.getName().getString(),
                                    (int) distance,
                                    direction)
                            .withStyle(ChatFormatting.GREEN),
                    true
            );
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type) {
        tooltip.accept(Component.translatable("item.speedrunpp.player_tracker.tooltip").withStyle(ChatFormatting.GRAY));
    }

    private String getCardinalDirection(double dx, double dz) {
        double angle = Math.toDegrees(Math.atan2(-dx, dz));
        if (angle < 0) angle += 360;

        if (angle >= 337.5 || angle < 22.5) return "S";
        if (angle >= 22.5 && angle < 67.5) return "SW";
        if (angle >= 67.5 && angle < 112.5) return "W";
        if (angle >= 112.5 && angle < 157.5) return "NW";
        if (angle >= 157.5 && angle < 202.5) return "N";
        if (angle >= 202.5 && angle < 247.5) return "NE";
        if (angle >= 247.5 && angle < 292.5) return "E";
        if (angle >= 292.5 && angle < 337.5) return "SE";
        return "?";
    }
}
