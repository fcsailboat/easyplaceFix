package org.uiop.easyplacefix.Mixin.packet;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.SignEditorOpenS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.uiop.easyplacefix.until.PlayerBlockAction;

@Mixin(SignEditorOpenS2CPacket.class)
public class MixinSignEditorOpenS2CPacket {
    @WrapWithCondition(
            method = "apply(Lnet/minecraft/network/listener/ClientPlayPacketListener;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/listener/ClientPlayPacketListener;onSignEditorOpen(Lnet/minecraft/network/packet/s2c/play/SignEditorOpenS2CPacket;)V")
    )
    private boolean signEditorOpen(ClientPlayPacketListener instance, SignEditorOpenS2CPacket signEditorOpenS2CPacket){
      return PlayerBlockAction.openSignEditorAction.run();
    }
}
