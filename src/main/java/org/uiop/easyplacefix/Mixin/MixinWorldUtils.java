package org.uiop.easyplacefix.Mixin;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.util.*;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;

import static fi.dy.masa.litematica.util.WorldUtils.getValidBlockRange;
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
            RayTraceUtils.RayTraceWrapper traceWrapper;

            double traceMaxRange = getValidBlockRange(mc);
            HitResult traceVanilla = RayTraceUtils.getRayTraceFromEntity(mc.world, mc.player, false, traceMaxRange);
            if (Configs.Generic.EASY_PLACE_FIRST.getBooleanValue())
            {
                // Temporary hack, using this same config here
                boolean targetFluids = Configs.InfoOverlays.INFO_OVERLAYS_TARGET_FLUIDS.getBooleanValue();
                traceWrapper = RayTraceUtils.getGenericTrace(mc.world, mc.player, traceMaxRange, true, targetFluids, false);
            }
            else
            {
//            Configs.Generic.EASY_PLACE_FIRST.setBooleanValue(true); 紧急方案[Doge]
                traceWrapper = RayTraceUtils.getFurthestSchematicWorldTraceBeforeVanilla(mc.world, mc.player, traceMaxRange);
            }
            if (traceWrapper==null)return ActionResult.PASS;
            return doEasyPlace2(mc,traceVanilla,traceWrapper);
        }
        return original.call(mc);
    }
//    @WrapMethod(method = "insertSignTextFromSchematic")
//    private static void insertSignTextFromSchematic(SignBlockEntity beClient, String[] screenTextArr, boolean front, Operation<Void> original) {
//        original.call(beClient,screenTextArr,front);
//        MinecraftClient.getInstance().player.closeHandledScreen();
//
//    }
}



