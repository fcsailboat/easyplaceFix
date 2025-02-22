package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BarrelBlock;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.LookAt;

import static org.uiop.easyplacefix.config.easyPlacefixConfig.Allow_Interaction;

@Mixin(BarrelBlock.class)
public class MixinBarrelBlock implements IBlock {
    @Override
    public long sleepTime(BlockState blockState) {
        Direction facing = blockState.get(Properties.FACING);
        if (facing== Direction.UP||facing==Direction.DOWN){
            return 0;
        }else {
            return 60_000_000;
        }
    }
    @Override
    public ActionResult isWorldTermination(BlockPos pos, BlockState blockState, BlockState worldBlockstate) {
        if (Allow_Interaction.getBooleanValue())return ActionResult.PASS;

        return null;
    }

    @Override
    public Pair<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
        return switch (blockState.get(Properties.FACING)) {
            case DOWN -> new Pair<>(LookAt.GetNow.NowYaw(), LookAt.Up);
            case UP -> new Pair<>(LookAt.GetNow.NowYaw(), LookAt.Down);
            case SOUTH -> new Pair<>(LookAt.North, LookAt.Horizontal);
            case WEST -> new Pair<>(LookAt.East, LookAt.Horizontal);
            case EAST -> new Pair<>(LookAt.West, LookAt.Horizontal);
            case NORTH -> new Pair<>(LookAt.South, LookAt.Horizontal);
        };
    }
}
