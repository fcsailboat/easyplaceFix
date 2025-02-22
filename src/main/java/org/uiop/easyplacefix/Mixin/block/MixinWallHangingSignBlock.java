package org.uiop.easyplacefix.Mixin.block;

import fi.dy.masa.litematica.world.SchematicWorldHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallHangingSignBlock;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.LookAt;
import org.uiop.easyplacefix.until.PlayerBlockAction;

@Mixin(WallHangingSignBlock.class)
public abstract class MixinWallHangingSignBlock implements IBlock {
    @Shadow
    public abstract boolean canAttachAt(BlockState state, WorldView world, BlockPos pos);
    @Override
    public void BlockAction(BlockState blockState, BlockHitResult blockHitResult) {
        ClientPlayNetworkHandler clientPlayNetworkHandler = MinecraftClient.getInstance().getNetworkHandler();
        SignBlockEntity blockEntity = (SignBlockEntity) SchematicWorldHandler.getSchematicWorld().getBlockEntity(blockHitResult.getBlockPos());
        SignText backText = blockEntity.getBackText();
        SignText frontText = blockEntity.getFrontText();
        PlayerBlockAction.openSignEditorAction.taskQueue.offer(() -> {

            clientPlayNetworkHandler.sendPacket(
                    new UpdateSignC2SPacket(
                            blockHitResult.getBlockPos(),
                            true,
                            frontText.getMessage(0, false).getString(),
                            frontText.getMessage(1, false).getString(),
                            frontText.getMessage(2, false).getString(),
                            frontText.getMessage(3, false).getString()


                    )
            );

            for (int i = 0; i < backText.getMessages(false).length; i++) {
                if (!backText.getMessage(i, false).getString().isEmpty()) {
                    clientPlayNetworkHandler.sendPacket(new PlayerInteractBlockC2SPacket(
                            Hand.MAIN_HAND,
                            blockHitResult,
                            0

                    ));

                    PlayerBlockAction.openSignEditorAction.taskQueue.offer(() -> {
                        clientPlayNetworkHandler.sendPacket(
                                new UpdateSignC2SPacket(
                                        blockHitResult.getBlockPos(),
                                        true,
                                        backText.getMessage(0, false).getString(),
                                        backText.getMessage(1, false).getString(),
                                        backText.getMessage(2, false).getString(),
                                        backText.getMessage(3, false).getString()


                                )
                        );

                    });
                    break;
                }
            }
        });
    }
    @Override
    public long sleepTime(BlockState blockState) {
        return 60_000_000;
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
