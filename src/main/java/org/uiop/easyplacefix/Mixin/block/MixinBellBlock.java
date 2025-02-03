package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BellBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.Attachment;
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
import org.uiop.easyplacefix.LookAt;

@Mixin(BellBlock.class)
public abstract class MixinBellBlock implements IBlock {

    @Shadow
    protected abstract boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos);

    @Override
    public Pair<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
        Attachment attachment = blockState.get(Properties.ATTACHMENT);
        if (attachment == Attachment.DOUBLE_WALL || attachment == Attachment.SINGLE_WALL) return null;
        return switch (blockState.get(Properties.HORIZONTAL_FACING)) {
            case SOUTH -> new Pair<>(LookAt.South, LookAt.Horizontal);
            case WEST -> new Pair<>(LookAt.West, LookAt.Horizontal);
            case EAST -> new Pair<>(LookAt.East, LookAt.Horizontal);
            default -> new Pair<>(LookAt.North, LookAt.Horizontal);
        };
    }

    @Override
    public Pair<BlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        Direction facing = blockState.get(Properties.HORIZONTAL_FACING);
        return this.canPlaceAt(blockState, MinecraftClient.getInstance().world, blockPos) ?
                new Pair<>(switch (blockState.get(Properties.ATTACHMENT)) {
                    case CEILING -> new BlockHitResult(
                            new Vec3d(0.5, 0, 0.5),
                            Direction.DOWN,
                            blockPos.up(), false
                    );
                    case FLOOR -> new BlockHitResult(
                            new Vec3d(0.5, 1, 0.5),
                            Direction.UP,
                            blockPos.down(), false
                    );
                    case SINGLE_WALL, DOUBLE_WALL -> new BlockHitResult(
                            switch (facing) {
                                case EAST -> new Vec3d(1, 0.5, 0.5);
                                case SOUTH -> new Vec3d(0.5, 0.5, 1);
                                case WEST -> new Vec3d(0, 0.5, 0.5);
                                default -> new Vec3d(0.5, 0.5, 0);
                            },
                            facing,
                            blockPos.offset(facing.getOpposite()),
                            false);
                }, 1) : null;
    }
}
