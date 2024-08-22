package org.uiop.easyplacefix;


import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;


import  org.uiop.easyplacefix.config.easyPlaceFixHotkeys;

public class EasyPlaceFix implements ModInitializer {
    public static BlockState blockState =null;
    public  static boolean modifyBoolean =false;
    @Override
    public void onInitialize() {
        easyPlaceFixHotkeys.addCallbacks(MinecraftClient.getInstance());
    }
    public static  <T> ItemStack findBlockInInventory(PlayerInventory inv, Class<T> blockClass) {
        for (int slot = 0; slot < inv.size(); slot++) {
            ItemStack stack = inv.getStack(slot);
            if (!stack.isEmpty()) {
                Block block = Block.getBlockFromItem(stack.getItem());
                if (blockClass.isInstance(block)) {
                    return stack; // 找到满足条件的物品堆，返回其槽位
                }
            }
        }
        return null; // 如果没有找到，返回 -1
    }
}
