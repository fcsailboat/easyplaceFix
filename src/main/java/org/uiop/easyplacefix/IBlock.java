package org.uiop.easyplacefix;

import net.minecraft.block.BlockState;
import net.minecraft.block.DeadCoralFanBlock;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import static org.uiop.easyplacefix.until.PlayerRotationAction.limitYawRotation;

public interface IBlock {
    default boolean hasYawPitch() {
        return false;
    }
    default long sleepTime(BlockState blockState){return 0;}
    default Pair<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
        return null;
    }
    default Pair<Float, Float> getLimitYawAndPitch(BlockState blockState) {
        Pair<LookAt, LookAt> lookAtPair = getYawAndPitch(blockState);
        if (lookAtPair!=null){
            return new Pair<>(limitYawRotation(Direction.fromHorizontalDegrees(lookAtPair.getLeft().Value())),lookAtPair.getRight().Value());
        }
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
    default void firstAction(){}
    default void afterAction(){}
    default ActionResult isSchemaTermination(BlockPos pos, BlockState blockState, BlockState worldBlockstate){return null;}
    default ActionResult isWorldTermination(BlockPos pos, BlockState blockState,BlockState worldBlockstate){return null;}
    default Item getItemForBlockState(BlockState blockState){return null;}
}
