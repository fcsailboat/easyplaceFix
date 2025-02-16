package org.uiop.easyplacefix.Mixin.block;

import fi.dy.masa.litematica.world.SchematicWorldHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallSignBlock;
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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.until.PlayerBlockAction;


@Mixin(WallSignBlock.class)
public abstract class MixinWallSignBlock implements IBlock {

    @Shadow
    protected abstract boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos);

    @Override
    public Pair<BlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        Direction direction = blockState.get(Properties.HORIZONTAL_FACING);
        return this.canPlaceAt(blockState, MinecraftClient.getInstance().world, blockPos) ?
                new Pair<>(
                        new BlockHitResult(
                                switch (direction) {
                                    case EAST -> new Vec3d(1, 0.5, 0.5);
                                    case SOUTH -> new Vec3d(0.5, 0.5, 1);
                                    case WEST -> new Vec3d(0, 0.5, 0.5);
                                    default -> new Vec3d(0.5, 0.5, 0);
                                },
                                direction,
                                blockPos.offset(direction.getOpposite()),
                                false
                        ), 1
                ) : null;
    }

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
}
