package org.uiop.easyplacefix.config;

import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import fi.dy.masa.malilib.hotkeys.IKeybindProvider;

import static org.uiop.easyplacefix.config.easyPlacefixConfig.OBSERVER_DETECT;

public class Hotkeys {

    public static void init(){
        InputEventHandler.getKeybindManager().registerKeybindProvider(new IKeybindProvider() {
            @Override
            public void addKeysToMap(IKeybindManager iKeybindManager) {
                iKeybindManager.addKeybindToMap(OBSERVER_DETECT.getKeybind());
            }

            @Override
            public void addHotkeys(IKeybindManager iKeybindManager) {

            }
        });
    }
}
