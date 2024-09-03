package org.uiop.easyplacefix.Mixin.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.uiop.easyplacefix.IisSimpleHitPos;

@Mixin(PlayerInteractBlockC2SPacket.class)
public class MixinPlayerInteractBlockC2SPacket implements IisSimpleHitPos {
    @Shadow
    @Final
    private Hand hand;
    @Shadow
    @Final
    private BlockHitResult blockHitResult;
    @Shadow
    @Final
    private int sequence;
    @Unique
    boolean isSimpleHitPos;

    @Override
    public void setSimpleHitPos() {
        isSimpleHitPos = true;
    }

    @Inject(method = "write", at = @At(value = "HEAD"), cancellable = true)
    public void operationWrite(PacketByteBuf buf, CallbackInfo ci) {
        if (isSimpleHitPos) {
            buf.writeEnumConstant(hand);
            BlockPos blockPos = blockHitResult.getBlockPos();
            buf.writeBlockPos(blockPos);
            buf.writeEnumConstant(blockHitResult.getSide());
            Vec3d vec3d = blockHitResult.getPos();
            buf.writeFloat((float) vec3d.x);
            buf.writeFloat((float) vec3d.y);
            buf.writeFloat((float) vec3d.z);
            buf.writeBoolean(blockHitResult.isInsideBlock());
            buf.writeVarInt(sequence);
            ci.cancel();
        }
    }
}
