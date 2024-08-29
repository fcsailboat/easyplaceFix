package org.uiop.easyplacefix.config;

import fi.dy.masa.malilib.MaLiLibConfigs;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;

public final class easyPlacefixConfig {
    public static final ConfigBoolean LOOSEN_MODE=
            new ConfigBoolean("loosenMode", false, "EasyPlaceFix.config.generic.comment.loosenMode");

    public static final ConfigBoolean IGNORE_NBT =
            new ConfigBoolean("nbtIgnore", false, "EasyPlaceFix.config.generic.comment.nbtIgnore");
    public static final ConfigBoolean Allow_Interaction =
            new ConfigBoolean("AllowInteraction", false, "EasyPlaceFix.config.generic.comment.AllowInteraction");
//    public static final ConfigBoolean Chain_Mode =
//            new ConfigBoolean("ChainMode", true, "EasyPlaceFix.config.generic.comment.ChainMode");





    public static IConfigBase[] getExtraGenericConfigs() {
        return new IConfigBase[] {
                LOOSEN_MODE,
                IGNORE_NBT,
                Allow_Interaction,
        };
    }
}
