package org.uiop.easyplacefix;


import fi.dy.masa.litematica.util.InventoryUtils;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;


import net.minecraft.util.Hand;
import  org.uiop.easyplacefix.config.easyPlaceFixHotkeys;

import java.util.function.Predicate;

public class EasyPlaceFix implements ModInitializer {
    public static BlockState pistonBlockState =null;
    public  static boolean modifyBoolean =false;
    @Override
    public void onInitialize() {
        easyPlaceFixHotkeys.addCallbacks(MinecraftClient.getInstance());
    }
    public static Hand findBlockInInventory(PlayerInventory inv, Predicate<Block> predicate) {
        for (int slot = 0; slot < inv.size(); slot++) {
            ItemStack stack = inv.getStack(slot);
            if (!stack.isEmpty()) {
                Block block = Block.getBlockFromItem(stack.getItem());
                if (predicate.test(block)) {
                    InventoryUtils.setPickedItemToHand(slot,stack,MinecraftClient.getInstance());
                    return Hand.MAIN_HAND; // 找到满足条件的物品堆，返回其槽位
                }
            }
        }
        return null; // 如果没有找到，返回 null
    }
}
