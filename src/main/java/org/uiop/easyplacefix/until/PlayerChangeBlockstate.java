package org.uiop.easyplacefix.until;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

public class PlayerChangeBlockstate {
    public static void InteractBlock(int InteractCount, BlockHitResult trace) {
        for (int i = 0; i < InteractCount - 1; i++) {
            MinecraftClient.getInstance().getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, trace, i));
        }
    }
}
