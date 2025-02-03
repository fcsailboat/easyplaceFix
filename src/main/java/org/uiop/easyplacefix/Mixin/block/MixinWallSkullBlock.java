package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.WallSkullBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.LookAt;

import static net.minecraft.block.WallTorchBlock.canPlaceAt;

@Mixin(WallSkullBlock.class)
public class MixinWallSkullBlock implements IBlock {
    @Override
    public Pair<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
        return switch (blockState.get(Properties.HORIZONTAL_FACING)) {
            case SOUTH -> new Pair<>(LookAt.North, LookAt.Horizontal);
            case WEST -> new Pair<>(LookAt.East, LookAt.Horizontal);
            case EAST -> new Pair<>(LookAt.West, LookAt.Horizontal);
            default -> new Pair<>(LookAt.South, LookAt.Horizontal);
        };
    }

    @Override//TODO 测试可不可以凭空放在墙上的头颅
    public Pair<BlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        Direction direction = blockState.get(Properties.HORIZONTAL_FACING);
        return
                canPlaceAt(MinecraftClient.getInstance().world, blockPos, direction) ?
                        new Pair<>(
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
                                ), 1
                        ) : null;
    }
}
