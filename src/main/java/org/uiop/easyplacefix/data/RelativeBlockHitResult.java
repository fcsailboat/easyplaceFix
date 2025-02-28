package org.uiop.easyplacefix.data;

import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class RelativeBlockHitResult extends BlockHitResult {
    public RelativeBlockHitResult(Vec3d pos, Direction side, BlockPos blockPos, boolean insideBlock) {
        super(pos, side, blockPos, insideBlock);
    }
    //已经存储了相对坐标，跳过加减法可以减少不必要的计算，提高性能。 -设计逻辑来自7087z
}
