package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.HangingSignBlock;
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

@Mixin(HangingSignBlock.class)
public abstract class MixinHangingSignBlock implements IBlock {


    @Shadow
    protected abstract boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos);

    @Override
    public Pair<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
        return new Pair<>(LookAt.Fractionize.customize(
                blockState.get(Properties.ROTATION) * 23
        ), LookAt.GetNow.NowPitch());
    }

    @Override
    public Pair<BlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos) {
        return this.canPlaceAt(blockState, MinecraftClient.getInstance().world, blockPos) ?
                new Pair<>(
                        new BlockHitResult(new Vec3d(0.5, 0, 0.5),
                                Direction.DOWN,
                                blockPos.up(),
                                false)
                        , 1) : null;
    }
}
