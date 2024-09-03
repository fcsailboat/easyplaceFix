package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.client.MinecraftClient;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IBlock;

@Mixin(LeverBlock.class)
public abstract class MixinLeverBlock extends MixinWallMountedBlock implements IBlock {
    @Shadow
    @Final
    public static BooleanProperty POWERED;


    @Override
    public Pair<BlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos) {
        BlockFace blockFace = blockState.get(Properties.BLOCK_FACE);
        Direction direction = blockState.get(Properties.HORIZONTAL_FACING);
        return canPlaceAt(blockState, MinecraftClient.getInstance().world, blockPos) ?
                switch (blockFace) {//TODO 后续可以将null改为连锁放置，需要一个接受pos的轻松放置方法
                    case FLOOR -> new Pair<>(
                            new BlockHitResult(new Vec3d(0.5, 1, 0.5),
                                    Direction.UP,
                                    blockPos.down(), false
                            ), blockState.get(Properties.POWERED) ? 2 : 1);
                    case CEILING -> new Pair<>(
                            new BlockHitResult(new Vec3d(0.5, 0, 0.5),
                                    Direction.DOWN,
                                    blockPos.up(), false
                            ), blockState.get(Properties.POWERED) ? 2 : 1);

                    case WALL -> new Pair<>(
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
                            ), blockState.get(Properties.POWERED) ? 2 : 1);
                } : null;
    }

}
