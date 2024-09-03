package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.HopperBlock;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;

@Mixin(HopperBlock.class)
public class MixinHopperBlock implements IBlock {
    @Override
    public Pair<BlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos) {
        return new Pair<>(new BlockHitResult(
                new Vec3d(0.5, 0.5, 0.5),
                switch (blockState.get(Properties.HOPPER_FACING)) {
                    case SOUTH -> Direction.NORTH;
                    case EAST -> Direction.WEST;
                    case WEST -> Direction.EAST;
                    case NORTH -> Direction.SOUTH;
                    default -> Direction.UP;
                },
                blockPos, false
        ), 1);
    }
}
