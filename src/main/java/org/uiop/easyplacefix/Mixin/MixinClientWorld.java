package org.uiop.easyplacefix.Mixin;

import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IClientWorld;

@Mixin(ClientWorld.class)
public class MixinClientWorld implements IClientWorld {
    @Shadow
    @Final
    private PendingUpdateManager pendingUpdateManager;

    @Override
    public int Sequence() {
        return this.pendingUpdateManager.incrementSequence().getSequence();
    }
}
