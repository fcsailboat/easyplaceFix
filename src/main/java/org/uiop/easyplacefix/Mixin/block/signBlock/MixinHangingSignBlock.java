package org.uiop.easyplacefix.Mixin.block.signBlock;

import fi.dy.masa.litematica.world.SchematicWorldHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.HangingSignBlock;
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
import org.uiop.easyplacefix.LookAt;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;
import org.uiop.easyplacefix.until.PlayerBlockAction;

@Mixin(HangingSignBlock.class)
public abstract class MixinHangingSignBlock implements IBlock {


    @Shadow
    protected abstract boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos);
    @Override
    public Pair<Float, Float> getLimitYawAndPitch(BlockState blockState) {
        Pair<LookAt, LookAt> lookAtPair = getYawAndPitch(blockState);
        return new Pair<>(
                lookAtPair.getLeft().Value(),
                lookAtPair.getRight().Value()
        );
    }
    @Override
    public Pair<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
        return new Pair<>(LookAt.Fractionize.customize(
                blockState.get(Properties.ROTATION) * 23
        ), LookAt.GetNow.NowPitch());
    }

    @Override
    public Pair<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        return this.canPlaceAt(blockState, MinecraftClient.getInstance().world, blockPos) ?
                new Pair<>(
                        new RelativeBlockHitResult(new Vec3d(0.5, 0, 0.5),
                                Direction.DOWN,
                                blockPos.up(),
                                false)
                        , 1) : null;
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
