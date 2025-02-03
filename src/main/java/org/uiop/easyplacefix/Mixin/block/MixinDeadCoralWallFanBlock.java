package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.DeadCoralWallFanBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IBlock;

@Mixin(DeadCoralWallFanBlock.class)
public abstract class MixinDeadCoralWallFanBlock implements IBlock {
    @Shadow
    protected abstract boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos);

    @Override
    public Pair<BlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        Direction direction = blockState.get(Properties.HORIZONTAL_FACING);
        return canPlaceAt(blockState, MinecraftClient.getInstance().world, blockPos) ? new Pair<>(
                new BlockHitResult(
                        switch (direction) {
                            case EAST -> new Vec3d(1, 0.5, 0.5);
                            case SOUTH -> new Vec3d(0.5, 0.5, 1);
                            case WEST -> new Vec3d(0, 0.5, 0.5);
                            default -> new Vec3d(0.5, 0.5, 0);
                        }, direction,
                        blockPos.offset(direction.getOpposite()), false
                ), 1
        ) : null;
    }
}

