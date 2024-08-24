package org.uiop.easyplacefix.Mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import fi.dy.masa.litematica.util.InventoryUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static org.uiop.easyplacefix.config.easyPlacefixConfig.IGNORE_NBT;

@Mixin(InventoryUtils.class)
public class MixinInventoryUtils {
    @WrapOperation(method = "schematicWorldPickBlock",at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;getSlotWithStack(Lnet/minecraft/item/ItemStack;)I"))
    private static int getSlotWithNbtIgnore(PlayerInventory instance, ItemStack stack, Operation<Integer> original, @Share("ItemStackWithNbt")LocalRef<ItemStack> itemStackRef){
       int slot = original.call(instance,stack);
        if (IGNORE_NBT.getBooleanValue()){
            for (int i = 0; i < instance.main.size(); ++i) {//关键步骤⬇(忽视itemStack的components组件)
                if (instance.main.get(i).isEmpty() || !ItemStack.areItemsEqual(stack, instance.main.get(i))) continue;
                itemStackRef.set(instance.main.get(i));
                return i;
            }
        }
        return slot;
    }
    @ModifyArgs(method = "schematicWorldPickBlock",at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/util/InventoryUtils;setPickedItemToHand(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/MinecraftClient;)V"))
    private static void SetNBT4SetPickedItemToHand(Args args,@Share("ItemStackWithNbt")LocalRef<ItemStack> itemStackRef){
                if (itemStackRef.get()!=null)args.set(0,itemStackRef.get());
    }
    @ModifyArgs(method = "schematicWorldPickBlock",at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/util/InventoryUtils;findSlotWithBoxWithItem(Lnet/minecraft/screen/ScreenHandler;Lnet/minecraft/item/ItemStack;Z)I"))
    private static void SetNBT4FindSlotWithBoxWithItem(Args args,@Share("ItemStackWithNbt")LocalRef<ItemStack> itemStackRef){
                if (itemStackRef.get()!=null)args.set(1,itemStackRef.get());
    }
}
