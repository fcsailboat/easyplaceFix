package org.uiop.easyplacefix.Mixin;


import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import fi.dy.masa.litematica.materials.MaterialCache;
import fi.dy.masa.litematica.util.EntityUtils;
import fi.dy.masa.litematica.util.RayTraceUtils;
import fi.dy.masa.litematica.util.WorldUtils;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.uiop.easyplacefix.EasyPlaceFix;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.IClientPlayerInteractionManager;
import org.uiop.easyplacefix.LookAt;
import org.uiop.easyplacefix.until.PlayerRotationAction;

import java.util.function.Predicate;

import static org.uiop.easyplacefix.EasyPlaceFix.findBlockInInventory;
import static org.uiop.easyplacefix.EasyPlaceFix.modifyBoolean;
import static org.uiop.easyplacefix.config.easyPlacefixConfig.Allow_Interaction;
import static org.uiop.easyplacefix.config.easyPlacefixConfig.LOOSEN_MODE;

@Mixin(WorldUtils.class)
public abstract class MixinWorldUtils {
    @WrapMethod(method = "doEasyPlaceAction")
    private static ActionResult fix(MinecraftClient mc, Operation<ActionResult> original) {//修复投影本身bug
        if (mc.targetedEntity != null) {
            if (Allow_Interaction.getBooleanValue()) {
                if (mc.targetedEntity instanceof Inventory) return ActionResult.PASS;
            }
            return ActionResult.FAIL;
        }

        return original.call(mc);
    }

    @ModifyReturnValue(method = "doEasyPlaceAction", at = @At(value = "RETURN"))
    private static ActionResult ok(ActionResult original,
                                   @Local RayTraceUtils.RayTraceWrapper traceWrapper,
                                   @Share("stateSchematic") LocalRef<BlockState> stateSchematicRef) {
        if (traceWrapper == null) {
            return original;
        }
        if (original == ActionResult.FAIL) {
            if (Allow_Interaction.getBooleanValue()) {
                BlockState blockState = MinecraftClient.getInstance().world.getBlockState(traceWrapper.getBlockHitResult().getBlockPos());
                if (
                        MinecraftClient.getInstance().player.getMainHandStack().isEmpty()
                                && (MinecraftClient.getInstance().player.getOffHandStack().isEmpty()
//                                MinecraftClient.getInstance().player.getOffHandStack().get(DataComponentTypes.CAN_PLACE_ON) == null
                        )
                ) {
                    return ActionResult.PASS;
                }
                if (blockState == null) return original;
                if (((IBlock) blockState.getBlock()).IsChest()) {
                    return ActionResult.PASS;
                }


            }
        }


        return original;
    }


    @WrapOperation(method = "doEasyPlaceAction", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;interactBlock(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"
    ))
    private static ActionResult modifyHitResultArgs(ClientPlayerInteractionManager instance,
                                                    ClientPlayerEntity player,
                                                    Hand hand,
                                                    BlockHitResult hitResult,
                                                    Operation<ActionResult> original,
                                                    @Share("stateSchematic") LocalRef<BlockState> stateSchematicRef,
                                                    @Local RayTraceUtils.RayTraceWrapper traceWrapper) {
        BlockHitResult blockHitResultFirstOne =  traceWrapper.getBlockHitResult();
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayNetworkHandler net = client.getNetworkHandler();
        ClientPlayerInteractionManager interactionManager = client.interactionManager;

        ((IClientPlayerInteractionManager) interactionManager).syn();

        BlockState blockState = stateSchematicRef.get();
        Block block = blockState.getBlock();


        Pair<LookAt, LookAt> lookAtPair = ((IBlock) block).getYawAndPitch(blockState);
        if (lookAtPair != null)
            PlayerRotationAction.setServerBoundPlayerRotation(lookAtPair.getLeft().Value(), lookAtPair.getRight().Value());
        Pair<BlockHitResult, Integer> blockHitResultIntegerPair =
                ((IBlock) block).getHitResult(
                blockState,
                blockHitResultFirstOne.getBlockPos());
        if (blockHitResultIntegerPair == null) return ActionResult.FAIL;
        BlockHitResult blockHitResult = blockHitResultIntegerPair.getLeft();
        Vec3d vec3d = new Vec3d(
                blockHitResult.getBlockPos().getX() + blockHitResult.getPos().x,
                blockHitResult.getBlockPos().getY() + blockHitResult.getPos().y,
                blockHitResult.getBlockPos().getZ() + blockHitResult.getPos().z
        );
        BlockHitResult offsetBlockhitResult = new BlockHitResult(
                vec3d,
                blockHitResult.getSide(),
                blockHitResult.getBlockPos(),
                false
        );

//        BlockHitResult blockHitResult1 = offsetBlockhitResult.withBlockPos(hitResult.getBlockPos());
        if (blockState.getBlock() instanceof PistonBlock) {//TODO 了解interactBlock内部工作原理，改进这部分代码
            EasyPlaceFix.pistonBlockState = blockState;
            modifyBoolean = true;
        }

        interactionManager.interactBlock(player, hand, offsetBlockhitResult);
//        ((IClientPlayerInteractionManager) interactionManager).syn2(player,hand,blockHitResult1);
//        var PlayerInteractBlockPacket = new PlayerInteractBlockC2SPacket(
//                Hand.MAIN_HAND,
//                blockHitResult,
//                ((IClientWorld) client.world).Sequence()
//        );
//        ((IisSimpleHitPos) PlayerInteractBlockPacket).setSimpleHitPos();

//        net.sendPacket(PlayerInteractBlockPacket);
        int i = 1;
        while (i < blockHitResultIntegerPair.getRight()) {
//            Thread.sleep(1000);
//            net.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,blockHitResult1,0));

            interactionManager.interactBlock(player, hand, blockHitResultFirstOne);
//            var BlockActionPacket = new PlayerInteractBlockC2SPacket(
//                    Hand.MAIN_HAND,
//                    blockHitResult1,
//                    ((IClientWorld) client.world).Sequence()
//            );
//            ((IisSimpleHitPos) BlockActionPacket).setSimpleHitPos();
//            net.sendPacket(BlockActionPacket);
            i++;
        }
        ((IBlock) block).BlockAction(blockState, blockHitResultFirstOne);
        return ActionResult.SUCCESS;


    }

    @WrapOperation(method = "doEasyPlaceAction", at = @At(
            value = "INVOKE",
            target = "Lfi/dy/masa/litematica/materials/MaterialCache;getRequiredBuildItemForState(Lnet/minecraft/block/BlockState;)Lnet/minecraft/item/ItemStack;"
    ))
    private static ItemStack
    modifyGetRequiredBuildItemForState(MaterialCache instance,
                                       BlockState stateSchematic,
                                       Operation<ItemStack> original,
                                       @Share("stateSchematic") LocalRef<BlockState> stateSchematicRef
    ) {
        stateSchematicRef.set(stateSchematic);//设置一个共享的BlockState,用于在Mixin中共享对应的投影方块
        return original.call(instance, stateSchematic);
    }

    @WrapOperation(method = "doEasyPlaceAction", at = @At(
            value = "INVOKE",
            target = "Lfi/dy/masa/litematica/util/EntityUtils;getUsedHandForItem(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/util/Hand;"
    ))
    private static Hand e(PlayerEntity player,
                          ItemStack stack, Operation<Hand> original,
                          @Share("stateSchematic") LocalRef<BlockState> stateSchematicRef
    ) {
        Hand hand = original.call(player, stack);
        if (hand == null && LOOSEN_MODE.getBooleanValue()) {
            if (!stack.isEmpty()) {
                if (!EntityUtils.isCreativeMode(player)) {
                    Block ReplacedBlock = stateSchematicRef.get().getBlock();//将被替换的item对应的方块
                    Predicate<Block> predicate = null;
                    if (ReplacedBlock instanceof WallBlock)   //墙类
                        predicate = block -> block instanceof WallBlock;
                    else if (ReplacedBlock instanceof FenceGateBlock)//栅栏门
                        predicate = block -> block instanceof FenceGateBlock;
                    else if (ReplacedBlock instanceof TrapdoorBlock)//活板门
                        predicate = block -> block instanceof TrapdoorBlock;
                    else if (ReplacedBlock instanceof CoralParentBlock)//珊瑚扇
                        predicate = block -> block instanceof CoralFanBlock;

                    if (predicate != null) {
                        PlayerInventory playerInventory = MinecraftClient.getInstance().player.getInventory();

                        return findBlockInInventory(playerInventory, predicate);

                    }
                }
            }

        }
        return hand;
    }
//@WrapWithCondition(method = "doEasyPlaceAction",
//        at = @At(value = "INVOKE",
//                target = "Lfi/dy/masa/litematica/util/RayTraceUtils;" +
//                        "getRayTraceFromEntity(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;ZD)" +
//                        "Lnet/minecraft/util/hit/HitResult;")
//)
//    private static boolean aa(World distance, Entity entityTmp, boolean optionalTmp, double i) {
//
//    return false;
//}

}



