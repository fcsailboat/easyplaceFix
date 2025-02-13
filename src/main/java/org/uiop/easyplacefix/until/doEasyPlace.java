package org.uiop.easyplacefix.until;

import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager;
import fi.dy.masa.litematica.util.EntityUtils;
import fi.dy.masa.litematica.util.InventoryUtils;
import fi.dy.masa.litematica.util.RayTraceUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.uiop.easyplacefix.EasyPlaceFix;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.IClientPlayerInteractionManager;
import org.uiop.easyplacefix.LookAt;
import org.uiop.easyplacefix.data.LoosenModeData;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static fi.dy.masa.litematica.util.WorldUtils.getValidBlockRange;
import static org.uiop.easyplacefix.EasyPlaceFix.*;
import static org.uiop.easyplacefix.config.easyPlacefixConfig.LOOSEN_MODE;
import static org.uiop.easyplacefix.data.LoosenModeData.items;
import static org.uiop.easyplacefix.until.PlayerRotationAction.limitYawRotation;

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
    public static Hand loosenMode2(HashSet<ItemStack> itemStackHashSet){

            for (int i=0;i<MinecraftClient.getInstance().player.getInventory().size();i++){
                ItemStack stack = MinecraftClient.getInstance().player.getInventory().getStack(i);
                stack =stack.copy();
//                HashSet<Item> items =new HashSet<>();
//                for (ItemStack itemStack :itemStackHashSet){
//                    items.add(itemStack.getItem());
//                }
                if (!stack.isEmpty()) {
                   if (items.contains(stack.getItem())){
                       InventoryUtils.setPickedItemToHand(i, stack.copy(), MinecraftClient.getInstance());
                       return Hand.MAIN_HAND; // 找到满足条件的物品堆，返回其槽位
                   }


                }
            }

        return null;


    }
    public static Hand loosenMode(Hand hand,ItemStack stack,BlockState stateSchema)
    {
        if (hand == null && LOOSEN_MODE.getBooleanValue()) {
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
                Hand hand1 = null;
                if (predicate != null) {
                    PlayerInventory playerInventory = MinecraftClient.getInstance().player.getInventory();
                     hand1 = findBlockInInventory(playerInventory, predicate);
                }
                if (hand1==null){
                    HashSet<ItemStack> itemStackHashSet = LoosenModeData.loadFromFile();
                    return loosenMode2(itemStackHashSet);

                }
                return hand1;

            }}

        }
        return hand;
    }

    public static ActionResult doEasyPlace2(MinecraftClient mc){
        RayTraceUtils.RayTraceWrapper traceWrapper;
        BlockHitResult trace;
        double traceMaxRange = getValidBlockRange(mc);
        HitResult traceVanilla = RayTraceUtils.getRayTraceFromEntity(mc.world, mc.player, false, traceMaxRange);
        if (Configs.Generic.EASY_PLACE_FIRST.getBooleanValue())
        {
            // Temporary hack, using this same config here
            boolean targetFluids = Configs.InfoOverlays.INFO_OVERLAYS_TARGET_FLUIDS.getBooleanValue();
            traceWrapper = RayTraceUtils.getGenericTrace(mc.world, mc.player, traceMaxRange, true, targetFluids, false);
        }
        else
        {
//            Configs.Generic.EASY_PLACE_FIRST.setBooleanValue(true); 紧急方案[Doge]
            traceWrapper = RayTraceUtils.getFurthestSchematicWorldTraceBeforeVanilla(mc.world, mc.player, traceMaxRange);
        }
        if (traceWrapper==null)return ActionResult.PASS;
        trace= traceWrapper.getBlockHitResult();
        World schematicWorld = SchematicWorldHandler.getSchematicWorld();
        BlockPos pos = trace.getBlockPos();
        BlockState stateClient = mc.world.getBlockState(pos);//获取本地方块状态
        BlockState stateSchematic = schematicWorld.getBlockState(pos);
        if (concurrentSet.contains(pos))return ActionResult.FAIL;
        ActionResult isTermination = ((IBlock) stateClient.getBlock()).isWorldTermination(pos, stateSchematic, stateClient);//是否终止
        if (isTermination != null) return isTermination;

        isTermination = ((IBlock)stateSchematic.getBlock()).isSchemaTermination(pos,stateSchematic,stateClient);//是否终止
        if (isTermination!=null)return isTermination;


        //MISS会在指针没有目标时(列如：指向空中)，不包括投影方块
        if (traceVanilla.getType()== HitResult.Type.ENTITY)
        {
            return ActionResult.PASS;
        }
        if (traceWrapper.getHitType() == RayTraceUtils.RayTraceWrapper.HitType.SCHEMATIC_BLOCK){

            ItemStack stack = new ItemStack(((IBlock)stateSchematic.getBlock()).getItemForBlockState(stateSchematic));
            if (!stack.isEmpty()){

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
                )return ActionResult.FAIL;





                ClientPlayerInteractionManager interactionManager = MinecraftClient.getInstance().interactionManager;
                InventoryUtils.schematicWorldPickBlock(stack, pos, schematicWorld, mc);
                Hand hand = EntityUtils.getUsedHandForItem(mc.player, stack);
                hand = loosenMode(hand,stack,stateSchematic);
                if (hand == null)
                {
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
                    EasyPlaceFix.pistonBlockState = stateSchematic;
                    modifyBoolean = true;
                }
                Hand finalHand = hand;
                scheduler.schedule(() -> {
                    try {
                        concurrentSet.add(pos);
                        Pair<Float, Float> lookAtPair = ((IBlock) block).getLimitYawAndPitch(stateSchematic);
                        if (lookAtPair != null) {
                            yawLock = lookAtPair.getLeft();
                            pitchLock =lookAtPair.getRight();
                            notChangPlayerLook = true;
                            mc.execute(()->PlayerRotationAction.setServerBoundPlayerRotation(yawLock, pitchLock, mc.player.horizontalCollision));
                        }
                        long delay = ((IBlock) block).sleepTime(stateSchematic);
                        try {
                            TimeUnit.NANOSECONDS.sleep(delay);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        ((IClientPlayerInteractionManager) interactionManager).syn();//同步快捷栏的选择框
//                        BlockReRender.blockRender(stateSchematic,pos);
//                        mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(finalHand, offsetBlockhitResult, 0));
                        mc.execute(()->{
                            interactionManager.interactBlock(
                                    mc.player,
                                    finalHand,
                                    offsetBlockhitResult
                            );
                            mc.player.swingHand(finalHand);
                        });
                        int i = 1;
                        while (i < blockHitResultIntegerPair.getRight()) {
                            mc.execute(()->{
                                interactionManager.interactBlock(
                                        mc.player,
                                        finalHand,
                                        trace
                                );
                                mc.player.swingHand(finalHand);
                            });
                            i++;
                        }
                        mc.execute(()-> ((IBlock) block).BlockAction(stateSchematic, trace));

                        PlayerRotationAction.setServerBoundPlayerRotation(mc.player.getYaw(), mc.player.getPitch(), mc.player.horizontalCollision);
                    }finally {
                       mc.execute(()->{
                           notChangPlayerLook = false;
                           concurrentSet.remove(pos);
                       });
                    }


                }, 0, TimeUnit.NANOSECONDS);
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }
}
