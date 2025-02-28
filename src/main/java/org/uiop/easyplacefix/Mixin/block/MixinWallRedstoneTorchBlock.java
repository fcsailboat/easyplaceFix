package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.WallRedstoneTorchBlock;
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
import org.uiop.easyplacefix.data.RelativeBlockHitResult;

@Mixin(WallRedstoneTorchBlock.class)
public abstract class MixinWallRedstoneTorchBlock implements IBlock {
    @Shadow
    protected abstract boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos);

    @Override
    public Pair<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        Direction direction = blockState.get(Properties.HORIZONTAL_FACING);
        return this.canPlaceAt(blockState, MinecraftClient.getInstance().world, blockPos) ?
                new Pair<>(
                        new RelativeBlockHitResult(
                                switch (direction) {
                                    case EAST -> new Vec3d(1, 0.5, 0.5);
                                    case SOUTH -> new Vec3d(0.5, 0.5, 1);
                                    case WEST -> new Vec3d(0, 0.5, 0.5);
                                    default -> new Vec3d(0.5, 0.5, 0);
                                },
                                direction,
                                blockPos.offset(direction.getOpposite()),
                                false
                        ), 1
                ) : null;
    }
}
