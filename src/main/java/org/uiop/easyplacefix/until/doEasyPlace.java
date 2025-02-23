package org.uiop.easyplacefix.until;

import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.materials.MaterialCache;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager;
import fi.dy.masa.litematica.util.EntityUtils;
import fi.dy.masa.litematica.util.InventoryUtils;
import fi.dy.masa.litematica.util.RayTraceUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.malilib.util.LayerRange;
import io.netty.channel.Channel;
import net.fabricmc.fabric.api.client.networking.v1.C2SConfigurationChannelEvents;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.IClientPlayerInteractionManager;
import org.uiop.easyplacefix.Mixin.AccessorMixin.ClientConnectionAccessor;
import org.uiop.easyplacefix.data.LoosenModeData;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static fi.dy.masa.litematica.util.InventoryUtils.findSlotWithBoxWithItem;
import static fi.dy.masa.litematica.util.InventoryUtils.setPickedItemToHand;
import static fi.dy.masa.litematica.util.WorldUtils.getValidBlockRange;
import static fi.dy.masa.litematica.util.WorldUtils.isPositionWithinRangeOfSchematicRegions;
import static org.uiop.easyplacefix.EasyPlaceFix.*;
import static org.uiop.easyplacefix.config.easyPlacefixConfig.LOOSEN_MODE;
import static org.uiop.easyplacefix.data.LoosenModeData.items;

public class doEasyPlace {//TODO 轻松放置重写计划

    // 是否在原理图范围内
    public static boolean isSchematicBlock(BlockPos pos) {
        SchematicPlacementManager schematicPlacementManager = DataManager.getSchematicPlacementManager();
        //获取该pos所在区块已加载原理图集合
        List<SchematicPlacementManager.PlacementPart> allPlacementsTouchingChunk
                = schematicPlacementManager.getAllPlacementsTouchingChunk(pos);
        //便利集合中的原理图是否覆盖了该pos
        for (SchematicPlacementManager.PlacementPart placementPart : allPlacementsTouchingChunk) {
            if (placementPart.getBox().containsPos(pos)) {
                return true;
            }
        }
        return false;
    }

    public static ItemStack loosenMode2(HashSet<ItemStack> itemStackHashSet) {

        for (int i = 0; i < MinecraftClient.getInstance().player.getInventory().size(); i++) {
            ItemStack stack = MinecraftClient.getInstance().player.getInventory().getStack(i);
            stack = stack.copy();
//                HashSet<Item> items =new HashSet<>();
//                for (ItemStack itemStack :itemStackHashSet){
//                    items.add(itemStack.getItem());
//                }
            if (!stack.isEmpty()) {
                if (items.contains(stack.getItem())) {
//                    InventoryUtils.setPickedItemToHand(i, stack.copy(), MinecraftClient.getInstance());
                    return stack; // 找到满足条件的物品堆，返回其槽位
                }


            }
        }

        return null;


    }

    public static ItemStack loosenMode(ItemStack stack, BlockState stateSchema) {
        if (stack == null && LOOSEN_MODE.getBooleanValue()) {
            if (!stack.isEmpty()) {
                if (!EntityUtils.isCreativeMode(MinecraftClient.getInstance().player)) {
                    Block ReplacedBlock = stateSchema.getBlock();//将被替换的item对应的方块
                    Predicate<Block> predicate = null;
                    if (ReplacedBlock instanceof WallBlock)   //墙类
                        predicate = block -> block instanceof WallBlock;
                    else if (ReplacedBlock instanceof FenceGateBlock)//栅栏门
                        predicate = block -> block instanceof FenceGateBlock;
                    else if (ReplacedBlock instanceof TrapdoorBlock)//活板门
                        predicate = block -> block instanceof TrapdoorBlock;
                    else if (ReplacedBlock instanceof CoralFanBlock)//珊瑚扇
                        predicate = block -> block instanceof CoralFanBlock;
                    ItemStack hand1 = null;
                    if (predicate != null) {
                        PlayerInventory playerInventory = MinecraftClient.getInstance().player.getInventory();
                        hand1 = findBlockInInventory(playerInventory, predicate);
                    }
                    if (hand1 == null) {
                        HashSet<ItemStack> itemStackHashSet = LoosenModeData.loadFromFile();
                        return loosenMode2(itemStackHashSet);

                    }
                    return hand1;

                }
            }

        }
        return stack;
    }

    public static ActionResult doEasyPlace2(MinecraftClient mc, RayTraceUtils.RayTraceWrapper traceWrapper) {
        BlockHitResult trace = traceWrapper.getBlockHitResult();
        World schematicWorld = SchematicWorldHandler.getSchematicWorld();
        BlockPos pos = trace.getBlockPos();
        if (concurrentSet.contains(pos)) return ActionResult.FAIL;
        BlockState stateClient = mc.world.getBlockState(pos);//获取本地方块状态
        BlockState stateSchematic = schematicWorld.getBlockState(pos);
        ActionResult isTermination = ((IBlock) stateClient.getBlock()).isWorldTermination(pos, stateSchematic, stateClient);//是否终止
        if (isTermination != null) return isTermination;

        isTermination = ((IBlock) stateSchematic.getBlock()).isSchemaTermination(pos, stateSchematic, stateClient);//是否终止
        if (isTermination != null) return isTermination;


        //MISS会在指针没有目标时(列如：指向空中)，不包括投影方块
        HitResult traceVanilla = RayTraceUtils.getRayTraceFromEntity(mc.world, mc.player, false, getValidBlockRange(mc));
        if (traceVanilla.getType() == HitResult.Type.ENTITY) {
            return ActionResult.PASS;
        }
        if (traceWrapper.getHitType() == RayTraceUtils.RayTraceWrapper.HitType.SCHEMATIC_BLOCK) {

            ItemStack stack = new ItemStack(((IBlock) stateSchematic.getBlock()).getItemForBlockState(stateSchematic));
            if (!stack.isEmpty()) {

                if (stateSchematic == mc.world.getBlockState(pos))//对比
                {
                    return ActionResult.FAIL;
                }
                //删除了缓存和放置过快检查
                if (!stateClient.canReplace(
                        new ItemPlacementContext(
                                MinecraftClient.getInstance().player,
                                Hand.MAIN_HAND,
                                stack,
                                trace
                        ))
                ) return ActionResult.FAIL;


                ClientPlayerInteractionManager interactionManager = MinecraftClient.getInstance().interactionManager;
                ItemStack itemStack2 = searchItem(mc, stack);
                itemStack2 = loosenMode(itemStack2, stateSchematic);
                if (itemStack2 == null) {
                    return ActionResult.FAIL;
                }
                Block block = stateSchematic.getBlock();


                Pair<BlockHitResult, Integer> blockHitResultIntegerPair =
                        ((IBlock) block).getHitResult(
                                stateSchematic,
                                trace.getBlockPos(),
                                stateClient
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
                if (stateSchematic.getBlock() instanceof PistonBlock) {//TODO 了解interactBlock内部工作原理，改进这部分代码
                    pistonBlockState = stateSchematic;
                    modifyBoolean = true;
                }
                ItemStack finalStack = itemStack2;
                concurrentSet.add(pos);
                scheduler.schedule(() -> {
                    AtomicReference<Hand> hand = new AtomicReference<>();
                    try {
                        Channel channel = ((ClientConnectionAccessor) mc.getNetworkHandler().getConnection()).getChannel();
//
//// 发送朝向数据包
//                        channel.writeAndFlush(new PlayerMoveC2SPacket.LookAndOnGround(yawLock, pitchLock, true))
//                                .addListener(future -> {
//                                    // 延迟后发送方块交互包
//                                    channel.eventLoop().schedule(() -> {
//                                        PlayerInteractBlockC2SPacket packet = new PlayerInteractBlockC2SPacket(
//                                                hand, offsetBlockhitResult, 0
//                                        );
//                                        channel.writeAndFlush(packet);
//                                    }, ((IBlock) block).sleepTime(stateSchematic), TimeUnit.MILLISECONDS);
//                                });
                        mc.execute(()->{
//                            InventoryUtils.schematicWorldPickBlock(finalStack, pos, schematicWorld, mc);
                            pickItem(mc,finalStack);
                            hand.set(EntityUtils.getUsedHandForItem(mc.player, finalStack));
                        });
                        Pair<Float, Float> lookAtPair = ((IBlock) block).getLimitYawAndPitch(stateSchematic);
                        if (lookAtPair != null) {
                            yawLock = lookAtPair.getLeft();
                            pitchLock = lookAtPair.getRight();
                            notChangPlayerLook = true;
//                            channel.writeAndFlush( new PlayerMoveC2SPacket.LookAndOnGround(
//                                    yawLock,
//                                    pitchLock,
//                                    MinecraftClient.getInstance().player.isOnGround(),
//                                    mc.player.horizontalCollision//不知道干嘛的参数
//
//                            ));
                            mc.execute(() -> PlayerRotationAction.setServerBoundPlayerRotation(yawLock, pitchLock, mc.player.horizontalCollision));
                        }
                        long delay = ((IBlock) block).sleepTime(stateSchematic);
                        try {
                            TimeUnit.NANOSECONDS.sleep(delay);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
//                        channel.writeAndFlush( new PlayerInteractBlockC2SPacket(
//                                                hand.get(), offsetBlockhitResult, 0
//                                        ));
                        mc.execute(() -> {
                            ((IClientPlayerInteractionManager) interactionManager).syn();
                            ((IBlock) block).firstAction();
                            interactionManager.interactBlock(
                                    mc.player,
                                    hand.get(),
                                    offsetBlockhitResult
                            );
                            mc.player.swingHand(hand.get());
                            int i = 1;
                            while (i < blockHitResultIntegerPair.getRight()) {

                                interactionManager.interactBlock(
                                        mc.player,
                                        hand.get(),
                                        trace
                                );
                                mc.player.swingHand(hand.get());

                                i++;
                            }
                            ((IBlock) block).afterAction();
                            ((IBlock) block).BlockAction(stateSchematic, trace);
                            notChangPlayerLook = false;
                            PlayerRotationAction.setServerBoundPlayerRotation(
                                    mc.player.getYaw(),
                                    mc.player.getPitch(),
                                    mc.player.horizontalCollision);
                            concurrentSet.remove(pos);
                        });
                    }finally {
                        if (notChangPlayerLook){
                            notChangPlayerLook = false;
                            concurrentSet.remove(pos);
                        }
                    }







                }, 0, TimeUnit.NANOSECONDS);
                return ActionResult.SUCCESS;
            }
        }
//        if (placementRestrictionInEffect(pos))return ActionResult.FAIL;
        return ActionResult.PASS;
    }

    public static ItemStack searchItem(MinecraftClient mc, ItemStack stack) {
        if (mc.player != null && mc.interactionManager != null && mc.world != null) {
            if (!stack.isEmpty()) {
                PlayerInventory inv = mc.player.getInventory();
                stack = stack.copy();
                if (EntityUtils.isCreativeMode(mc.player)) {
                    return stack;
                } else {
                    int slot = inv.getSlotWithStack(stack);
                    boolean shouldPick = inv.selectedSlot != slot;
                    if (shouldPick && slot != -1) {
                        return stack;
                    } else if (slot == -1 && Configs.Generic.PICK_BLOCK_SHULKERS.getBooleanValue()) {
                        slot = findSlotWithBoxWithItem(mc.player.playerScreenHandler, stack, false);
                        if (slot != -1) {
                            return mc.player.playerScreenHandler.slots.get(slot).getStack();//潜影盒
                        }
                    }
                }
            }

        }
        return null;

    }
    public static void pickItem(MinecraftClient mc, ItemStack stack) {

        if (EntityUtils.isCreativeMode(mc.player)) {
           setPickedItemToHand(stack,mc);
            mc.interactionManager.clickCreativeStack(mc.player.getStackInHand(Hand.MAIN_HAND), 36 + mc.player.getInventory().selectedSlot);
        } else{
            setPickedItemToHand(stack,mc);
        }
    }
    private static boolean placementRestrictionInEffect(BlockPos pos)
    {

        ;//获取mc的准星坐标
         //目标位置不应有任何物品，
        //并且该位置位于逻辑示意图子区域内或附近
        return  isPositionWithinRangeOfSchematicRegions( pos, 2);
    }
}
