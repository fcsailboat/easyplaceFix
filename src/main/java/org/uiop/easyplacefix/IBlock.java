package org.uiop.easyplacefix;

import net.minecraft.block.BlockState;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public interface IBlock {
    default boolean hasYawPitch() {
        return false;
    }

    default Pair<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
        return null;
    }

    default Direction getSide(BlockState blockState) {
        return null;
    }

    default Pair<BlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos) {
        return new Pair<>(new BlockHitResult(
                new Vec3d(0.5, 0.5, 0.5),
                Direction.UP,
                blockPos, false
        ), 1);
    }

    default void BlockAction(BlockState blockState, BlockHitResult blockHitResult) {
    }

    default boolean IsChest() {
        return false;
    }
}
