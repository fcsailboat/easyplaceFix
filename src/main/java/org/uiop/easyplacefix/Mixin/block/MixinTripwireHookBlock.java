package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.TripwireHookBlock;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;

@Mixin(TripwireHookBlock.class)
public class MixinTripwireHookBlock implements IBlock {
    @Override
    public Pair<BlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        Direction direction = blockState.get(Properties.HORIZONTAL_FACING);

        return new Pair<>(
                new BlockHitResult(
                        switch (direction) {
                            case EAST -> new Vec3d(1, 0.5, 0.5);
                            case SOUTH -> new Vec3d(0.5, 0.5, 1);
                            case WEST -> new Vec3d(0, 0.5, 0.5);
                            default -> new Vec3d(0.5, 0.5, 0);
                        },
                        direction,
                        blockPos.offset(direction.getOpposite()),
                        false
                ), 1);
    }
}
