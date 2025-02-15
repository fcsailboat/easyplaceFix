package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.SkullBlock;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.LookAt;

@Mixin(SkullBlock.class)
public class MixinSkullBlock implements IBlock {
    @Override
    public Pair<Float, Float> getLimitYawAndPitch(BlockState blockState) {
        Pair<LookAt, LookAt> lookAtPair = getYawAndPitch(blockState);
        return new Pair<>(
                lookAtPair.getLeft().Value(),
                lookAtPair.getRight().Value()
        );
    }
    @Override
    public Pair<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
        return new Pair<>(LookAt.Fractionize.customize(
                blockState.get(Properties.ROTATION) * 23
        ), LookAt.GetNow.NowPitch());
    }

    @Override
    public Pair<BlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        return   new Pair<>(
                new BlockHitResult(
                        new Vec3d(0.5, 0.5, 0.5),
                        Direction.UP,
                        blockPos,
                        false
                ), 1);
    }
}
