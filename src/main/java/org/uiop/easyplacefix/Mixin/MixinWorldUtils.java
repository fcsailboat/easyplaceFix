package org.uiop.easyplacefix.Mixin;


import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.materials.MaterialCache;
import fi.dy.masa.litematica.util.EntityUtils;
import fi.dy.masa.litematica.util.RayTraceUtils;
import fi.dy.masa.litematica.util.WorldUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.uiop.easyplacefix.*;
import org.uiop.easyplacefix.until.PlayerRotationAction;

import java.util.function.Predicate;

import static fi.dy.masa.litematica.util.WorldUtils.getValidBlockRange;
import static org.uiop.easyplacefix.EasyPlaceFix.findBlockInInventory;
import static org.uiop.easyplacefix.EasyPlaceFix.modifyBoolean;
import static org.uiop.easyplacefix.config.easyPlacefixConfig.*;

@Mixin(WorldUtils.class)
public abstract class MixinWorldUtils {
    @WrapMethod(method = "doEasyPlaceAction")
    private static ActionResult fix(MinecraftClient mc, Operation<ActionResult> original) {//修复投影本身bug
        double traceMaxRange = getValidBlockRange(mc);
        RayTraceUtils.RayTraceWrapper traceWrapper;
        boolean targetFluids = Configs.InfoOverlays.INFO_OVERLAYS_TARGET_FLUIDS.getBooleanValue();
        traceWrapper = RayTraceUtils.getGenericTrace(mc.world, mc.player, traceMaxRange, true, targetFluids, false);
        if (traceWrapper==null)return ActionResult.PASS;


        BlockPos pos = traceWrapper.getBlockHitResult().getBlockPos();
        BlockState worldBlock = mc.world.getBlockState(pos);
        BlockState blockstate =  SchematicWorldHandler.getSchematicWorld().getBlockState(pos);
        ActionResult isTermination = ((IBlock)worldBlock.getBlock()).isWorldTermination(pos,blockstate,worldBlock);//是否终止
        if (isTermination!=null)return isTermination;

        isTermination = ((IBlock)blockstate.getBlock()).isSchemaTermination(pos,blockstate,worldBlock);//是否终止
        if (isTermination!=null)return isTermination;

//


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

//                if (stateSchematicRef.get()!=null){
//                    BlockState blockState =stateSchematicRef.get();
//                    if (blockState.contains(Properties.WATERLOGGED)){
//                        if (blockState.get(Properties.WATERLOGGED)){
//                            ItemStack itemStack1= MinecraftClient.getInstance().player.getMainHandStack();
//                            ItemStack itemStack2= MinecraftClient.getInstance().player.getOffHandStack();
//                            PlayerEntity player=MinecraftClient.getInstance().player;
//                            if (itemStack1.getItem()== Items.WATER_BUCKET){
//                                MinecraftClient.getInstance().getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(
//                                        Hand.MAIN_HAND,0,player.headYaw,player.getPitch()
//                                ));
//                                return ActionResult.SUCCESS;
//                            }
//                            if (itemStack2.getItem()==Items.WATER_BUCKET){
//                                MinecraftClient.getInstance().getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(
//                                        Hand.OFF_HAND,0,player.headYaw,player.getPitch()
//                                ));
//                                return ActionResult.SUCCESS;
//                            }
//                            schematicWorldPickBlock(Items.WATER_BUCKET.getDefaultStack(),traceWrapper.getBlockHitResult().getBlockPos(), MinecraftClient.getInstance().world, MinecraftClient.getInstance());
//                        }
//                    }
//                }这是含水方块放置的代码，还缺少炼药锅岩浆



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
                                                    @Local RayTraceUtils.RayTraceWrapper traceWrapper
    ) throws InterruptedException {
        BlockHitResult blockHitResultFirstOne = traceWrapper.getBlockHitResult();
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayNetworkHandler net = client.getNetworkHandler();
        ClientPlayerInteractionManager interactionManager = client.interactionManager;


        BlockState blockState = stateSchematicRef.get();
        Block block = blockState.getBlock();
        Pair<LookAt, LookAt> lookAtPair = ((IBlock) block).getYawAndPitch(blockState);
        if (lookAtPair != null) {
            PlayerRotationAction.setServerBoundPlayerRotation(lookAtPair.getLeft().Value(), lookAtPair.getRight().Value(),player.horizontalCollision);
        }




        ((IClientPlayerInteractionManager) interactionManager).syn();//同步快捷栏的选择框

        Pair<BlockHitResult, Integer> blockHitResultIntegerPair =
                ((IBlock) block).getHitResult(
                        blockState,
                        blockHitResultFirstOne.getBlockPos()
                );

        if (blockHitResultIntegerPair == null) return ActionResult.FAIL;

        BlockHitResult blockHitResult = blockHitResultIntegerPair.getLeft();//获取操作数据(blockHitResult)
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
        if (blockState.getBlock() instanceof PistonBlock) {//TODO 了解interactBlock内部工作原理，改进这部分代码
            EasyPlaceFix.pistonBlockState = blockState;
            modifyBoolean = true;
        }

        interactionManager.interactBlock(player, hand, offsetBlockhitResult);
        int i = 1;
        while (i < blockHitResultIntegerPair.getRight()) {
            interactionManager.interactBlock(player, hand, blockHitResultFirstOne);
            i++;
        }
        ((IBlock) block).BlockAction(blockState, blockHitResultFirstOne);
//        if (blockState.get(Pro))
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
                    else if (ReplacedBlock instanceof CoralFanBlock)//珊瑚扇
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

}



