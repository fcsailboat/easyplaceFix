package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.WallHangingSignBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.LookAt;

@Mixin(WallHangingSignBlock.class)
public abstract class MixinWallHangingSignBlock implements IBlock {
    @Shadow
    public abstract boolean canAttachAt(BlockState state, WorldView world, BlockPos pos);

    @Override
    public long sleepTime(BlockState blockState) {
        return 50_000_000;
    }

    //TODO 这里可以不用朝向数据包，但是我没看懂她是怎么判断周围有没有可以依附的方块的，暂时先发送朝向数据包
//这里要发送朝向数据包，因为朝向不同文字的面也不同
@Override
public Pair<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
    return switch (blockState.get(Properties.HORIZONTAL_FACING)) {
        case SOUTH -> new Pair<>(LookAt.North, LookAt.GetNow);
        case WEST -> new Pair<>(LookAt.East, LookAt.GetNow);
        case EAST -> new Pair<>(LookAt.West, LookAt.GetNow);
        default -> new Pair<>(LookAt.South, LookAt.GetNow);
    };
}
//    @Override
//    public Pair<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
//        switch (blockState.get(Properties.FACING)){
//            case WEST ->this.canAttachAt()
//        }
//    }

    @Override
    public Pair<BlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        return canAttachAt(blockState, MinecraftClient.getInstance().world, blockPos) ?
                new Pair<>(
                        new BlockHitResult(new Vec3d(0.5, 0.5, 0.5),
                                blockState.get(Properties.HORIZONTAL_FACING).rotateYClockwise(),
                                blockPos,
                                false
                        ), 1) : null;
    }
}
