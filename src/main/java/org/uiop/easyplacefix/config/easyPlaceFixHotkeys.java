package org.uiop.easyplacefix.config;

import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.KeyCallbackToggleBooleanConfigWithMessage;
import net.minecraft.client.MinecraftClient;

public final class easyPlaceFixHotkeys {
    public static final ConfigHotkey LOOSEN_MODE_HOTKEY =new ConfigHotkey("loosenModeHotkey","","easyPlaceFix.config.hotkeys.comment.loosenmode")
            .translatedName("easyPlaceFix.config.hotkeys.name.loosenmode");

    public static ConfigHotkey[] getExtraHotkeys() {
        return new ConfigHotkey[] {
                LOOSEN_MODE_HOTKEY,
        };
    }

    public static void addCallbacks(MinecraftClient mc) {
        LOOSEN_MODE_HOTKEY.getKeybind().setCallback(new KeyCallbackToggleBooleanConfigWithMessage(easyPlacefixConfig.LOOSEN_MODE));
    }

}

