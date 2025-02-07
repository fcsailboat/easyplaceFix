package org.uiop.easyplacefix.Mixin.block;

import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import net.minecraft.block.BlockState;
import net.minecraft.block.ObserverBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.LookAt;

import static org.uiop.easyplacefix.config.easyPlacefixConfig.OBSERVER_DETECT;
import static org.uiop.easyplacefix.until.doEasyPlace.isSchematicBlock;

@Mixin(ObserverBlock.class)
public class MixinObserverBlock implements IBlock {
    @Override
    public long sleepTime(BlockState blockState) {
      Direction facing = blockState.get(Properties.FACING);
        if (facing== Direction.UP||facing==Direction.DOWN){
            return 0;
        }else {
            return 40_000_000;
        }
    }

    @Override
    public Pair<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
        return switch (blockState.get(Properties.FACING)) {
            case DOWN -> new Pair<>(LookAt.GetNow.NowYaw(), LookAt.Down);
            case UP -> new Pair<>(LookAt.GetNow.NowYaw(), LookAt.Up);
            case SOUTH -> new Pair<>(LookAt.South, LookAt.Horizontal);
            case WEST -> new Pair<>(LookAt.West, LookAt.Horizontal);
            case EAST -> new Pair<>(LookAt.East, LookAt.Horizontal);
            case NORTH -> new Pair<>(LookAt.North, LookAt.Horizontal);
        };
    }

    @Override
    public ActionResult isSchemaTermination(BlockPos pos, BlockState blockState, BlockState worldBlockstate) {
        // 侦测器放置检测
        if (OBSERVER_DETECT.getBooleanValue()) {
            Direction direction = blockState.get(Properties.FACING);
            BlockPos offset = pos.offset(direction);
            WorldSchematic schematicWorld = SchematicWorldHandler.getSchematicWorld();
            // 判断侦测器看向的是否在投影范围内
            if (isSchematicBlock(offset) && schematicWorld != null) {
                BlockState lookBlock = MinecraftClient.getInstance().world.getBlockState(offset);
                if (!schematicWorld.getBlockState(offset).getBlock().equals(lookBlock.getBlock()))
                    return ActionResult.FAIL;
            }
        }//需要参数:对应朝向，pos
        return null;
    }
}
