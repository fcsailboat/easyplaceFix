package org.uiop.easyplacefix.Mixin.config;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fi.dy.masa.litematica.gui.GuiConfigs;
import fi.dy.masa.litematica.gui.GuiRenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.uiop.easyplacefix.EasyPlaceFix;

@Mixin(GuiRenderLayer.class)
public abstract class MixinGuiRenderLayer {

    @WrapOperation(at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/gui/GuiRenderLayer;createTabButton(IIILfi/dy/masa/litematica/gui/GuiConfigs$ConfigGuiTab;)I", ordinal = 5), method = "initGui")
    private int renderMyButton(GuiRenderLayer instance, int x, int y, int width, GuiConfigs.ConfigGuiTab tab, Operation<Integer> original) {
        Integer call = original.call(instance, x, y, width, tab);
        createTabButton(call + x, y ,-1 , EasyPlaceFix.EASY_FIX);
        return call;
    }
    @Shadow(remap = false)
    protected abstract int createTabButton(int x, int y, int width, GuiConfigs.ConfigGuiTab tab);
}
