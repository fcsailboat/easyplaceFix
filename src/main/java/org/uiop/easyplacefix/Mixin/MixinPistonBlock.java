package org.uiop.easyplacefix.Mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.uiop.easyplacefix.EasyPlaceFix;

@Mixin(PistonBlock.class)
public class MixinPistonBlock {
@ModifyReturnValue(method = "getPlacementState",at = @At(value = "RETURN"))
    private BlockState ModgetPlacementState(BlockState original){
    if (EasyPlaceFix.modifyBoolean){//仅在放置活塞时需要修改
        EasyPlaceFix.modifyBoolean=false;
        return EasyPlaceFix.pistonBlockState;//投影的方块状态
    }
    return original;
}
}
