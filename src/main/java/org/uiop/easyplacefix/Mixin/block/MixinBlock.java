package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;

@Mixin(Block.class)
public abstract class MixinBlock implements IBlock {

}
