package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.SeaPickleBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;

@Mixin(SeaPickleBlock.class)
public abstract class MixinSeaPickleBlock extends PlantBlock implements IBlock {
    protected MixinSeaPickleBlock(Settings settings) {
        super(settings);
    }

    @Override
    public Pair<BlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos) {
        return canPlaceAt(blockState, MinecraftClient.getInstance().world, blockPos) ? new Pair<>(
                new BlockHitResult(
                        new Vec3d(0.5, 0.5, 0.5),
                        Direction.UP,
                        blockPos, false
                ), blockState.get(Properties.PICKLES)
        ) : null;
    }
}
