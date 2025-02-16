package org.uiop.easyplacefix.Mixin.packet;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.ScreenHandlerPropertyUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.uiop.easyplacefix.until.PlayerBlockAction;

import static org.uiop.easyplacefix.EasyPlaceFix.*;

@Mixin(ScreenHandlerPropertyUpdateS2CPacket.class)
public class MixinScreenHandlerPropertyUpdateS2CPacket {//这是更新插槽状态的数据包

    @WrapWithCondition(
            method = "apply(Lnet/minecraft/network/listener/ClientPlayPacketListener;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/listener/ClientPlayPacketListener;" +
                            "onScreenHandlerPropertyUpdate(Lnet/minecraft/network/packet/s2c/play/ScreenHandlerPropertyUpdateS2CPacket;)V"
            ))
    private boolean updateFail(ClientPlayPacketListener instance, ScreenHandlerPropertyUpdateS2CPacket screenHandlerPropertyUpdateS2CPacket) {
        return PlayerBlockAction.openScreenAction.waitAction();
    }
}
