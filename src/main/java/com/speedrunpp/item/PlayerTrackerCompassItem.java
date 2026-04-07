package com.speedrunpp.item;

import com.speedrunpp.SpeedrunState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

public class PlayerTrackerCompassItem extends Item {

    public PlayerTrackerCompassItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient && user instanceof ServerPlayerEntity serverPlayer) {
            MinecraftServer server = serverPlayer.getServer();
            if (server == null) return TypedActionResult.pass(stack);

            SpeedrunState state = SpeedrunState.get(server);
            List<ServerPlayerEntity> onlinePlayers = server.getPlayerManager().getPlayerList()
                    .stream()
                    .filter(p -> p != serverPlayer)
                    .toList();

            if (onlinePlayers.isEmpty()) {
                serverPlayer.sendMessage(
                        Text.translatable("item.speedrunpp.player_tracker.no_players")
                                .formatted(Formatting.RED),
                        true
                );
                return TypedActionResult.fail(stack);
            }

            UUID currentTarget = state.getTrackerTarget(serverPlayer.getUuid());
            int currentIndex = -1;

            if (currentTarget != null) {
                for (int i = 0; i < onlinePlayers.size(); i++) {
                    if (onlinePlayers.get(i).getUuid().equals(currentTarget)) {
                        currentIndex = i;
                        break;
                    }
                }
            }

            int nextIndex = (currentIndex + 1) % onlinePlayers.size();
            ServerPlayerEntity targetPlayer = onlinePlayers.get(nextIndex);

            state.setTrackerTarget(serverPlayer.getUuid(), targetPlayer.getUuid());

            serverPlayer.sendMessage(
                    Text.translatable("item.speedrunpp.player_tracker.tracking",
                                    targetPlayer.getName().getString())
                            .formatted(Formatting.AQUA, Formatting.BOLD),
                    true
            );

            return TypedActionResult.success(stack);
        }

        return TypedActionResult.success(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient && entity instanceof ServerPlayerEntity player && selected) {
            if (world.getTime() % 20 != 0) return;

            MinecraftServer server = player.getServer();
            if (server == null) return;

            SpeedrunState state = SpeedrunState.get(server);
            UUID targetId = state.getTrackerTarget(player.getUuid());
            if (targetId == null) return;

            ServerPlayerEntity target = server.getPlayerManager().getPlayer(targetId);
            if (target == null) {
                player.sendMessage(
                        Text.literal("Target offline")
                                .formatted(Formatting.GRAY, Formatting.ITALIC),
                        true
                );
                return;
            }

            double dx = target.getX() - player.getX();
            double dz = target.getZ() - player.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            String direction = getCardinalDirection(dx, dz);

            player.sendMessage(
                    Text.translatable("item.speedrunpp.player_tracker.direction",
                                    target.getName().getString(),
                                    (int) distance,
                                    direction)
                            .formatted(Formatting.GREEN),
                    true
            );
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.speedrunpp.player_tracker.tooltip").formatted(Formatting.GRAY));
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
