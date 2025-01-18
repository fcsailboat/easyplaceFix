package org.uiop.easyplacefix.Mixin.packet;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static org.uiop.easyplacefix.EasyPlaceFix.*;

@Mixin(OpenScreenS2CPacket.class)
public class MixinOpenScreenS2CPacket {
    @WrapWithCondition(
            method = "apply(Lnet/minecraft/network/listener/ClientPlayPacketListener;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/listener/ClientPlayPacketListener;" +
                            "onOpenScreen(Lnet/minecraft/network/packet/s2c/play/OpenScreenS2CPacket;)V"))
    private boolean OpenScreenFail(ClientPlayPacketListener instance, OpenScreenS2CPacket openScreenS2CPacket) {
        if (aaa!=null&&!crafterOperation){
            screenId=openScreenS2CPacket.getSyncId();
            aaa.run();
            aaa=null;
        }
        return !crafterOperation;
    }
}
