package org.uiop.easyplacefix.until;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class PlayerRotationAction {

    public static void setServerBoundPlayerRotation(Float yaw, Float pitch,Boolean hor) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        minecraftClient.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.LookAndOnGround(
                        yaw,
                        pitch,
                        MinecraftClient.getInstance().player.isOnGround(),hor//不知道干嘛的参数

                )
        );
    }
}
