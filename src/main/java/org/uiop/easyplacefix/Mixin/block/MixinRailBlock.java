package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.RailBlock;
import net.minecraft.block.enums.RailShape;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.LookAt;

@Mixin(RailBlock.class)
public class MixinRailBlock implements IBlock {

    @Override
    public Pair<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
        RailShape railShape = blockState.get(Properties.RAIL_SHAPE);
        if (railShape == RailShape.NORTH_SOUTH) {
            return new Pair<>(LookAt.North, LookAt.GetNow.NowPitch());
        } else {
            return new Pair<>(LookAt.East, LookAt.GetNow.NowPitch());
        }
    }
}
