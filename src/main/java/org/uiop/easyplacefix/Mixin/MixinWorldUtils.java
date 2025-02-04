package org.uiop.easyplacefix.Mixin;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import fi.dy.masa.litematica.util.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.screen.CustomInventoryScreen;

import static org.uiop.easyplacefix.EasyPlaceFix.loosenMode;
import static org.uiop.easyplacefix.until.doEasyPlace.doEasyPlace2;

@Mixin(WorldUtils.class)
public abstract class MixinWorldUtils {
    @WrapMethod(method = "doEasyPlaceAction")
    private static ActionResult fix(MinecraftClient mc, Operation<ActionResult> original) {//主要逻辑调用入口
//        if (loosenMode){
//            MinecraftClient.getInstance().setScreen(
//                    new CustomInventoryScreen(MinecraftClient.getInstance().player,
//                            MinecraftClient.getInstance().player.networkHandler.getEnabledFeatures(),
//                            true
//                    )
//            );
//            return ActionResult.FAIL;
//        }
        if (PlacementHandler.getEffectiveProtocolVersion()== EasyPlaceProtocol.SLAB_ONLY){
            return doEasyPlace2(mc);
        }
        return original.call(mc);
    }
}



