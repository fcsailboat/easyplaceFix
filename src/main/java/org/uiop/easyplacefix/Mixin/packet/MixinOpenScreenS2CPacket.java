package org.uiop.easyplacefix.Mixin.packet;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.c2s.play.ButtonClickC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.SlotChangedStateC2SPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static org.uiop.easyplacefix.EasyPlaceFix.crafterOperation;
import static org.uiop.easyplacefix.EasyPlaceFix.crafterSlot;

@Mixin(OpenScreenS2CPacket.class)
public class MixinOpenScreenS2CPacket {
    @WrapWithCondition(
            method = "apply(Lnet/minecraft/network/listener/ClientPlayPacketListener;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/listener/ClientPlayPacketListener;" +
                            "onOpenScreen(Lnet/minecraft/network/packet/s2c/play/OpenScreenS2CPacket;)V"))
    private boolean OpenScreenFail(ClientPlayPacketListener instance, OpenScreenS2CPacket openScreenS2CPacket) {
        if (crafterOperation) {
            ClientPlayNetworkHandler clientPlayNetworkHandler = MinecraftClient.getInstance().getNetworkHandler();
            int screenId = openScreenS2CPacket.getSyncId();
            for (int slot = 0; slot < crafterSlot.size(); slot++) {
                boolean isDisable = crafterSlot.get(slot);
                if (isDisable) {
                    clientPlayNetworkHandler.sendPacket(new SlotChangedStateC2SPacket(slot, screenId, false));
                    clientPlayNetworkHandler.sendPacket(new ClickSlotC2SPacket(screenId, screenId, slot, 0, SlotActionType.PICKUP, ItemStack.EMPTY, new Int2ObjectOpenHashMap<>()));
                }

            }
            crafterOperation = false;
            clientPlayNetworkHandler.sendPacket(new CloseHandledScreenC2SPacket(screenId));
            return false;
        }
        return true;
    }
}
