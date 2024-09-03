package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.LookAt;

@Mixin(DoorBlock.class)
public abstract class MixinDoorBlock implements IBlock {
    @Shadow
    protected abstract boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos);

    @Shadow
    @Final
    private BlockSetType blockSetType;

    @Override
    public Pair<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
        return switch (blockState.get(Properties.HORIZONTAL_FACING)) {
            case SOUTH -> new Pair<>(LookAt.South, LookAt.GetNow);
            case WEST -> new Pair<>(LookAt.West, LookAt.GetNow);
            case EAST -> new Pair<>(LookAt.East, LookAt.GetNow);
            default -> new Pair<>(LookAt.North, LookAt.GetNow);
        };
    }

    @Override
    public Pair<BlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos) {
        Direction direction = blockState.get(Properties.HORIZONTAL_FACING);
        DoorHinge doorHinge = blockState.get(Properties.DOOR_HINGE);
        if (blockState.get(Properties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) blockPos = blockPos.down();
        return this.canPlaceAt(blockState, MinecraftClient.getInstance().world, blockPos) ?
                new Pair<>(switch (direction) {
                    case SOUTH -> {
                        if (doorHinge == DoorHinge.LEFT)
                            yield new BlockHitResult(new Vec3d(0.8, 1, 0), Direction.UP, blockPos.down(), false);
                        else
                            yield new BlockHitResult(new Vec3d(0.2, 1, 0), Direction.UP, blockPos.down(), false);

                    }
                    case WEST -> {
                        if (doorHinge == DoorHinge.LEFT)
                            yield new BlockHitResult(new Vec3d(0, 1, 0.8), Direction.UP, blockPos.down(), false);
                        else
                            yield new BlockHitResult(new Vec3d(0, 1, 0.2), Direction.UP, blockPos.down(), false);

                    }
                    case EAST -> {
                        if (doorHinge == DoorHinge.LEFT)
                            yield new BlockHitResult(new Vec3d(0, 1, 0.2), Direction.UP, blockPos.down(), false);
                        else
                            yield new BlockHitResult(new Vec3d(0, 1, 0.8), Direction.UP, blockPos.down(), false);

                    }
                    default -> {
                        if (doorHinge == DoorHinge.LEFT)
                            yield new BlockHitResult(new Vec3d(0.2, 0, 0), Direction.UP, blockPos, false);
                        else
                            yield new BlockHitResult(new Vec3d(0.8, 0, 0), Direction.UP, blockPos, false);

                    }
                }, blockState.get(Properties.OPEN) && this.blockSetType.canOpenByHand() ? 2 : 1) : null;
    }
}
