package org.uiop.easyplacefix.Mixin.packet;

import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.uiop.easyplacefix.until.PlayerBlockAction;

@Mixin(BlockUpdateS2CPacket.class)
public class MixinBlockUpdateS2CPacket {
    @Shadow @Final private BlockPos pos;

    @Inject(method = "apply(Lnet/minecraft/network/listener/ClientPlayPacketListener;)V",
    at = @At(value = "HEAD"))
    public void apply(ClientPlayPacketListener clientPlayPacketListener, CallbackInfo ci){
        PlayerBlockAction.useItemOnAction.upDateBlock(this.pos);
    }
}
