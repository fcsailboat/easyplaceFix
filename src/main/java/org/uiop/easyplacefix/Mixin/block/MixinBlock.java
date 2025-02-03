package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IBlock;

@Mixin(Block.class)
public abstract class MixinBlock implements IBlock {
    @Shadow public abstract Item asItem();

    @Override
    public Item getItemForBlockState(BlockState blockState) {
       return this.asItem();
    }
}
