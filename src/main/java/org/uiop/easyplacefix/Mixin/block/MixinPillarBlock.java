package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;

import static net.minecraft.util.math.Direction.Axis;

@Mixin(PillarBlock.class)
public class MixinPillarBlock implements IBlock {

    @Override
    public Direction getSide(BlockState blockState) {
        Axis axis = blockState.get(Properties.AXIS);
        return switch (axis) {
            case X -> Direction.EAST;//x
            case Y -> Direction.DOWN;//y
            case Z -> Direction.NORTH;//z
        };
    }
}
