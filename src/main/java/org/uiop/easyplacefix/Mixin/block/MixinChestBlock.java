package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;

@Mixin(ChestBlock.class)
public class MixinChestBlock implements IBlock {
    @Override
    public Pair<BlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos) {
        ChestType chestType = blockState.get(Properties.CHEST_TYPE);
        if (chestType != ChestType.SINGLE) {
            MinecraftClient.getInstance().getNetworkHandler().sendPacket(new ClientCommandC2SPacket(
                    MinecraftClient.getInstance().player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY
            ));
        }

        return new Pair<>(new BlockHitResult(
                new Vec3d(0.5, 0.5, 0.5),
                Direction.UP,
                blockPos, false
        ), 1);


    }

    @Override
    public void BlockAction(BlockState blockState, BlockHitResult blockHitResult) {
        ChestType chestType = blockState.get(Properties.CHEST_TYPE);
        if (chestType != ChestType.SINGLE) {
            MinecraftClient.getInstance().getNetworkHandler().sendPacket(new ClientCommandC2SPacket(
                    MinecraftClient.getInstance().player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY
            ));
        }
    }
}
