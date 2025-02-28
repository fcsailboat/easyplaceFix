package org.uiop.easyplacefix.Mixin.packet;

import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.PlayerActionResponseS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerActionResponseS2CPacket.class)
public class MixinPlayerActionResponseS2CPacket {

    @Inject(method = "apply(Lnet/minecraft/network/listener/ClientPlayPacketListener;)V", at = @At("HEAD"))
    public void apply(ClientPlayPacketListener clientPlayPacketListener, CallbackInfo ci) {

    }
}
