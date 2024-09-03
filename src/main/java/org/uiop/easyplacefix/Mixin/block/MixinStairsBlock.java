package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.LookAt;

@Mixin(StairsBlock.class)
public class MixinStairsBlock implements IBlock {

    @Override
    public Pair<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
        return switch (blockState.get(Properties.HORIZONTAL_FACING)) {
            case SOUTH -> new Pair<>(LookAt.South, LookAt.GetNow.NowPitch());
            case WEST -> new Pair<>(LookAt.West, LookAt.GetNow.NowPitch());
            case EAST -> new Pair<>(LookAt.East, LookAt.GetNow.NowPitch());
            default -> new Pair<>(LookAt.North, LookAt.GetNow.NowPitch());
        };
    }

    @Override
    public Pair<BlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos) {

        BlockHalf blockHalf = blockState.get(Properties.BLOCK_HALF);
        return switch (blockHalf) {
            case TOP -> new Pair<>(
                    new BlockHitResult(
                            new Vec3d(0.5, 1, 0.5),
                            blockState.get(Properties.HORIZONTAL_FACING),
                            blockPos, false
                    ), 1);
            case BOTTOM -> new Pair<>(
                    new BlockHitResult(
                            new Vec3d(0.5, 0, 0.5),
                            blockState.get(Properties.HORIZONTAL_FACING),
                            blockPos, false
                    ), 1);
        };

    }
}
