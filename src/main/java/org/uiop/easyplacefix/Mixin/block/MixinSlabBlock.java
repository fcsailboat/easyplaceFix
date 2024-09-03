package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;

@Mixin(SlabBlock.class)
public class MixinSlabBlock implements IBlock {
    @Override
    public Pair<BlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos) {
        SlabType slabType = blockState.get(Properties.SLAB_TYPE);
        return switch (slabType) {
            case TOP -> new Pair<>(new BlockHitResult(new Vec3d(0.5, 1, 0.5), Direction.DOWN, blockPos, false), 1);
            case BOTTOM -> new Pair<>(new BlockHitResult(new Vec3d(0.5, 0, 0.5), Direction.UP, blockPos, false), 1);
            case DOUBLE -> new Pair<>(new BlockHitResult(new Vec3d(0.5, 0.5, 0.5), Direction.UP, blockPos, false), 2);
        };

    }
}
