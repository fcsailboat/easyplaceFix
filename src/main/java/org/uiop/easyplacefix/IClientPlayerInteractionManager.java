package org.uiop.easyplacefix;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

public interface IClientPlayerInteractionManager {
    default void syn() {
    }

    default void syn2(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult) {
    }
}
