package org.uiop.easyplacefix.Mixin.config;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.gui.GuiConfigs;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.uiop.easyplacefix.EasyPlaceFix;

import java.util.Arrays;
import java.util.List;

import static org.uiop.easyplacefix.config.easyPlacefixConfig.getExtraGenericConfigs;

@Mixin(GuiConfigs.class)
public abstract class MixinGuiConfigs {

    @WrapOperation(at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/gui/GuiConfigs;createButton(IIILfi/dy/masa/litematica/gui/GuiConfigs$ConfigGuiTab;)I", ordinal = 5), method = "initGui")
    private int t1(GuiConfigs instance, int x, int y, int width, GuiConfigs.ConfigGuiTab tab, Operation<Integer> original) {
        Integer call = original.call(instance, x, y, width, tab);
        createButton(call + x, y ,-1 , EasyPlaceFix.EASY_FIX);
        return call;
    }
    @Inject(at =@At("HEAD"),method = "getConfigs", cancellable = true,remap = false)
    private void t2(CallbackInfoReturnable<List<GuiConfigsBase.ConfigOptionWrapper>> cir) {
        GuiConfigs.ConfigGuiTab tab = DataManager.getConfigGuiTab();
        if (EasyPlaceFix.EASY_FIX.equals(tab)) {
            List<IConfigBase> list1 = Arrays.asList(getExtraGenericConfigs());
            cir.setReturnValue(GuiConfigsBase.ConfigOptionWrapper.createFor(list1));
        }
    }

    @Shadow(remap = false)
    protected abstract int createButton(int x, int y, int width, GuiConfigs.ConfigGuiTab tab);
}

