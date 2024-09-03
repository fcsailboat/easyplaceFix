package org.uiop.easyplacefix;

import net.minecraft.client.MinecraftClient;

public enum LookAt {
    East(-90f),
    West(90f),
    North(180f),
    South(0f),
    Down(90f),
    Up(-90f),
    Horizontal(0f),
    GetNow(0f),
    Fractionize(0);
    float yawPitch;

    LookAt(float yawPitch) {
        this.yawPitch = yawPitch;
    }

    public Float Value() {
        return this.yawPitch;
    }

    public LookAt customize(float yawPitch2) {
        yawPitch = yawPitch2;
        return this;
    }

    public LookAt NowYaw() {
        yawPitch = MinecraftClient.getInstance().player.getYaw();
        return this;
    }

    public LookAt NowPitch() {
        yawPitch = MinecraftClient.getInstance().player.getPitch();
        return this;
    }
}
