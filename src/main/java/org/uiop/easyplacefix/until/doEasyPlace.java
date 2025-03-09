package org.uiop.easyplacefix.until;

import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager;
import fi.dy.masa.litematica.util.EntityUtils;
import fi.dy.masa.litematica.util.RayTraceUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import io.netty.channel.Channel;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.IClientPlayerInteractionManager;
import org.uiop.easyplacefix.Mixin.AccessorMixin.ClientConnectionAccessor;
import org.uiop.easyplacefix.data.LoosenModeData;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import static org.uiop.easyplacefix.until.PlayerBlockAction.useItemOnAction.*;
import static fi.dy.masa.litematica.util.InventoryUtils.findSlotWithBoxWithItem;
import static fi.dy.masa.litematica.util.InventoryUtils.setPickedItemToHand;
import static fi.dy.masa.litematica.util.WorldUtils.getValidBlockRange;
import static fi.dy.masa.litematica.util.WorldUtils.isPositionWithinRangeOfSchematicRegions;
import static org.uiop.easyplacefix.EasyPlaceFix.findBlockInInventory;
import static org.uiop.easyplacefix.EasyPlaceFix.scheduler;
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
                ItemStack stack1 = null;
                if (predicate != null) {
                    PlayerInventory playerInventory = MinecraftClient.getInstance().player.getInventory();
                    stack1 = findBlockInInventory(playerInventory, predicate);
                }
                if (stack1 == null) {
                    HashSet<ItemStack> itemStackHashSet = LoosenModeData.loadFromFile();
                    return loosenMode2(itemStackHashSet);

                }
                return stack1;

            }


        }
        return stack;
    }

    public static ActionResult doEasyPlace2(MinecraftClient mc, RayTraceUtils.RayTraceWrapper traceWrapper) {
        BlockHitResult trace = traceWrapper.getBlockHitResult();//这是投影获取玩家指向的方法
        World schematicWorld = SchematicWorldHandler.getSchematicWorld();
        BlockPos pos = trace.getBlockPos();//这是投影获取玩家指向方块坐标的方法
        if (concurrentMap.containsKey(pos)) return ActionResult.FAIL;
        BlockState stateClient = mc.world.getBlockState(pos);//获取本地方块状态
        BlockState stateSchematic = schematicWorld.getBlockState(pos);
        ActionResult isTermination = ((IBlock) stateClient.getBlock()).isWorldTermination(pos, stateSchematic, stateClient);//是否终止
        if (isTermination != null) return isTermination;
        //是否终止操作，分为两次判断⬆⬇
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
                if (itemStack2 == null) {//没有找到对应物品即为无法放置
                    return ActionResult.FAIL;
                }

                Block block = stateSchematic.getBlock();//获取要操作的block实例
                Pair<RelativeBlockHitResult, Integer> blockHitResultIntegerPair =
                        ((IBlock) block).getHitResult(
                                stateSchematic,
                                trace.getBlockPos(),
                                stateClient
                        );

                if (blockHitResultIntegerPair == null) return ActionResult.FAIL;
                RelativeBlockHitResult offsetBlockHitResult = blockHitResultIntegerPair.getLeft();//获取操作数据(blockHitResult)
                if (stateSchematic.getBlock() instanceof PistonBlock) {//TODO 了解interactBlock内部工作原理，改进这部分代码
                    pistonBlockState = stateSchematic;
                    modifyBoolean = true;
                }
                ItemStack finalStack = itemStack2;
                concurrentMap.put(pos,0L);

                AtomicReference<Hand> hand = new AtomicReference<>();

                Channel channel = ((ClientConnectionAccessor) MinecraftClient.getInstance().getNetworkHandler().getConnection()).getChannel();
                Pair<Float, Float> lookAtPair = ((IBlock) block).getLimitYawAndPitch(stateSchematic);
                boolean wantActionAck = ((IBlock) block).HasSleepTime(stateSchematic);
                if (wantActionAck) {
                    scheduler.execute(() -> {
                        Runnable runnable = (() ->
                                mc.execute(() -> {
                                    pickItem(mc, finalStack);
                                    hand.set(EntityUtils.getUsedHandForItem(mc.player, finalStack));
                                    ((IClientPlayerInteractionManager) interactionManager).syn();
                                    ((IBlock) block).firstAction();
                                    interactionManager.interactBlock(
                                            mc.player,
                                            hand.get(),
                                            offsetBlockHitResult
                                    );
                                 concurrentMap.put(pos,System.currentTimeMillis());//设置缓存
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
                                }));

                        yawLock = lookAtPair.getLeft();
                        pitchLock = lookAtPair.getRight();
                        notChangPlayerLook = true;
                        channel.writeAndFlush(new PlayerMoveC2SPacket.LookAndOnGround(
                                yawLock,
                                pitchLock,
                                MinecraftClient.getInstance().player.isOnGround(),
                                mc.player.horizontalCollision//不知道干嘛的参数
                        ));
                        channel.writeAndFlush(new PlayerActionC2SPacket(
                                PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK,
                                pos,
                                Direction.DOWN
                        ));
                        PlayerBlockAction.useItemOnAction.taskQueue.offer(runnable);

                        try {
                            semaphore.acquire();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            // 处理中断异常
                        }


                    });


                } else {
                    pickItem(mc, finalStack);
                    ((IClientPlayerInteractionManager) interactionManager).syn();
                    hand.set(EntityUtils.getUsedHandForItem(mc.player, finalStack));
                    if (lookAtPair != null) {
                        PlayerRotationAction.setServerBoundPlayerRotation(
                                lookAtPair.getLeft(),
                                lookAtPair.getRight(),
                                mc.player.horizontalCollision
                        );
                    }
                    ((IBlock) block).firstAction();
                    interactionManager.interactBlock(
                            mc.player,
                            hand.get(),
                            offsetBlockHitResult
                    );
                    mc.player.swingHand(hand.get());
                    concurrentMap.put(pos, 10086L);
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
                    if (lookAtPair != null) PlayerRotationAction.restRotation();
                }


                return ActionResult.SUCCESS;
            }
        }
        if (placementRestrictionInEffect(pos)) return ActionResult.FAIL;
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
                    if (slot != -1) {
                        return stack;
                    } else if (slot == -1 && Configs.Generic.PICK_BLOCK_SHULKERS.getBooleanValue()) {
                        slot = findSlotWithBoxWithItem(mc.player.playerScreenHandler, stack, false);
                        if (slot != -1) {
                            pickItem(mc, mc.player.playerScreenHandler.slots.get(slot).getStack());
                            return null;//潜影盒
                        }
                    }
                }
            }

        }
        return null;

    }

    public static void pickItem(MinecraftClient mc, ItemStack stack) {

        if (EntityUtils.isCreativeMode(mc.player)) {
            setPickedItemToHand(stack, mc);
            mc.interactionManager.clickCreativeStack(mc.player.getStackInHand(Hand.MAIN_HAND), 36 + mc.player.getInventory().selectedSlot);
        } else {
            setPickedItemToHand(stack, mc);
        }
    }

    private static boolean placementRestrictionInEffect(BlockPos pos) {

        ;//获取mc的准星坐标
        //目标位置不应有任何物品，
        //并且该位置位于逻辑示意图子区域内或附近
        return isPositionWithinRangeOfSchematicRegions(pos, 2);
    }
}
