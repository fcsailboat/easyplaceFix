package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.LookAt;

@Mixin(PistonBlock.class)
public class MixinPiston implements IBlock {
    @Override
    public boolean HasSleepTime(BlockState blockState) {
        Direction facing = blockState.get(Properties.FACING);
        return facing != Direction.UP && facing != Direction.DOWN;
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
