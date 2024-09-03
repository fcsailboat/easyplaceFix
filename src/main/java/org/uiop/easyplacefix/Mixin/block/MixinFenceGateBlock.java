package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.LookAt;

@Mixin(FenceGateBlock.class)
public class MixinFenceGateBlock implements IBlock {
    @Override
    public Pair<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
        return switch (blockState.get(Properties.HORIZONTAL_FACING)) {
            case SOUTH -> new Pair<>(LookAt.South, LookAt.Up);
            case WEST -> new Pair<>(LookAt.West, LookAt.Up);
            case EAST -> new Pair<>(LookAt.East, LookAt.Up);
            default -> new Pair<>(LookAt.North, LookAt.Up);
        };
    }

    @Override
    public Pair<BlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos) {
        Direction direction = blockState.get(Properties.HORIZONTAL_FACING);
        return new Pair<>(new BlockHitResult(
                new Vec3d(0.5, 0.5, 0.5),
                direction, blockPos, false
        ), blockState.get(Properties.OPEN) ? 2 : 1);
    }
}
