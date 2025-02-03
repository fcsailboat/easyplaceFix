package org.uiop.easyplacefix;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResult;
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

    default Pair<BlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        return new Pair<>(new BlockHitResult(
                new Vec3d(0.5, 0.5, 0.5),
                Direction.UP,
                blockPos, false
        ), 1);
    }

    default void BlockAction(BlockState blockState, BlockHitResult blockHitResult) {
    }
    default ActionResult isSchemaTermination(BlockPos pos, BlockState blockState, BlockState worldBlockstate){return null;}
    default ActionResult isWorldTermination(BlockPos pos, BlockState blockState,BlockState worldBlockstate){return null;}
    default Item getItemForBlockState(BlockState blockState){return null;}
}
