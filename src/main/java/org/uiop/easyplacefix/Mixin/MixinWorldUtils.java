package org.uiop.easyplacefix.Mixin;


import com.llamalad7.mixinextras.injector.ModifyReturnValue;
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

import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.malilib.util.BlockUtils;
import net.minecraft.block.*;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CrafterBlockEntity;
import net.minecraft.block.enums.*;
import net.minecraft.client.MinecraftClient;


import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.uiop.easyplacefix.EasyPlaceFix;
import org.uiop.easyplacefix.until.PlayerChangeBlockstate;
import org.uiop.easyplacefix.until.PlayerRotation;

import java.util.function.Predicate;

import static org.uiop.easyplacefix.EasyPlaceFix.*;
import static org.uiop.easyplacefix.config.easyPlacefixConfig.*;


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
    private static ActionResult ok(ActionResult original, @Local RayTraceUtils.RayTraceWrapper traceWrapper, @Share("stateSchematic") LocalRef<BlockState> stateSchematicRef) {
        if (traceWrapper == null) {
            return original;
        }
        if (original == ActionResult.SUCCESS) {
            MinecraftClient mc = MinecraftClient.getInstance();

            if (stateSchematicRef.get() == null) return original;

            BlockHitResult trace = traceWrapper.getBlockHitResult();
            BlockState stateSchematic = stateSchematicRef.get();
            Block block = stateSchematic.getBlock();

            if (block == Blocks.REPEATER) {
                int delay = stateSchematic.get(Properties.DELAY);
                PlayerChangeBlockstate.InteractBlock(delay, trace);
            } else if (block instanceof TrapdoorBlock || block instanceof FenceGateBlock) {
                boolean open = stateSchematic.get(Properties.OPEN);
                if (open) {
                    mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, trace, 0));
                }

            } else if (block == Blocks.COMPARATOR) {
                ComparatorMode comparatorMode = stateSchematic.get(Properties.COMPARATOR_MODE);
                if (comparatorMode == ComparatorMode.SUBTRACT) {
                    mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, trace, 0));
                }
            } else if (block == Blocks.LEVER) {
                if (stateSchematic.get(Properties.POWERED))
                    mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, trace, 0));
            }//没有很多地方会用到大量开启的拉杆,但是拉杆往往是激活的
            else if (block == Blocks.TURTLE_EGG) {
                int eggCount = stateSchematic.get(Properties.EGGS);
                PlayerChangeBlockstate.InteractBlock(eggCount, trace);
            } else if (block == Blocks.SEA_PICKLE) {
                PlayerChangeBlockstate.InteractBlock(stateSchematic.get(Properties.PICKLES), trace);
            } else if (stateSchematic.contains(Properties.ORIENTATION)) {
                CrafterBlockEntity blockEntity = (CrafterBlockEntity) SchematicWorldHandler.getSchematicWorld().getBlockEntity(trace.getBlockPos());
                for (int i = 0; i < 9; i++) {
                    boolean isDisabled = blockEntity.isSlotDisabled(i);
                    crafterSlot.set(i, isDisabled);
                    if (!crafterOperation && isDisabled) {
                        crafterOperation = true;
                    }
                }

                mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, trace, 0));
            } else if (block instanceof CandleBlock) {
              int candles =  stateSchematic.get(Properties.CANDLES);
              PlayerChangeBlockstate.InteractBlock(candles,trace);
            } else if (block ==Blocks.CAKE) {
                int bites = stateSchematic.get(Properties.BITES);
                PlayerChangeBlockstate.InteractBlock(bites+1,trace);
            }

        } else if (original == ActionResult.FAIL) {

            if (Allow_Interaction.getBooleanValue()) {
                if (MinecraftClient.getInstance().player.getMainHandStack().isEmpty() && MinecraftClient.getInstance().player.getOffHandStack().isEmpty()) {
                    return ActionResult.PASS;
                }
                Block blockEntity = MinecraftClient.getInstance().world.getBlockState(traceWrapper.getBlockHitResult().getBlockPos()).getBlock();
                if (blockEntity instanceof AbstractChestBlock<?>) {
                    return ActionResult.PASS;
                }


            }

        }


        return original;
    }

    @Inject(method = "doEasyPlaceAction", at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/util/WorldUtils;cacheEasyPlacePosition(Lnet/minecraft/util/math/BlockPos;)V", shift = At.Shift.AFTER))
    private static void ex(MinecraftClient mc, CallbackInfoReturnable<ActionResult> cir, @Share("stateSchematic") LocalRef<BlockState> stateSchematicRef, @Share("side2") LocalRef<Direction> side2, @Share("hitVecIn") LocalRef<Vec3d> vec3dLocalRef) {
        BlockState stateSchematic = stateSchematicRef.get(); //直接使用共享的值，不需要重新获取
        @Nullable DirectionProperty property = BlockUtils.getFirstDirectionProperty(stateSchematic);
        if (property != null) {
            Block block = stateSchematic.getBlock();
            Direction d = stateSchematic.get(property);
            if (block == Blocks.HOPPER) {//漏斗
                side2.set(d.getOpposite()); //这个是我今天发现的方法
            } else if (stateSchematic.contains(Properties.BLOCK_HALF)) {
                BlockHalf blockHalf = stateSchematic.get(Properties.BLOCK_HALF);
                if (blockHalf == BlockHalf.BOTTOM)
                    side2.set(Direction.UP);
                else side2.set(Direction.DOWN);
            } else if (stateSchematic.contains(Properties.BLOCK_FACE)) {
                BlockFace blockFace = stateSchematic.get(Properties.BLOCK_FACE);
                if (blockFace == BlockFace.FLOOR) side2.set(Direction.UP);
                else if (blockFace == BlockFace.CEILING) side2.set(Direction.DOWN);
                else side2.set(d);
            } else {
                side2.set(d);
                //墙上的告示牌，末地烛，避雷针，墙上的火把
            }

            // Ctrl + Alt + V
            // 别用var 自动类型推导会降低程序可读性 好的，

            float yaw, pitch;
            // 尽量用确定的值去equals不确定的值，不确定的值有可能是null
            if (block == Blocks.OBSERVER) {
                pitch = switch (d) {
                    case DOWN -> 90f;
                    case UP -> -90f;
                    default -> 0f;
                };
                yaw = switch (d) {
                    // 我感觉你的idea需要先设置一下
                    case SOUTH -> 0F;
                    case WEST -> 90F;
                    case EAST -> -90F;
                    default -> 180F;
                };//这里不同的方块相对应的朝向有一些不同，侦测器和活塞是完全相反的，楼梯的话是只有上下不相反
            } else if (block instanceof DoorBlock) {
                Pair<Float, Float> playerRotation = PlayerRotation.SameRotationWithDoor(d, stateSchematic.get(Properties.DOOR_HINGE), vec3dLocalRef);
                yaw = playerRotation.getLeft();
                pitch = playerRotation.getRight();
            } else if
            (block instanceof StairsBlock || block instanceof FenceGateBlock || block instanceof LeverBlock || block instanceof ButtonBlock || block instanceof BedBlock) {
                Pair<Float, Float> playerRotation = PlayerRotation.SameRotation(d);
                yaw = playerRotation.getLeft();
                pitch = playerRotation.getRight();
            } else if (block == Blocks.ANVIL) {
                pitch = 0f;
                yaw = switch (d) {
                    // 我感觉你的idea需要先设置一下
                    case SOUTH -> -90F;
                    case WEST -> 180F;
                    case EAST -> -180F;
                    default -> 90F;
                };
            } else {
                pitch = switch (d) {
                    case DOWN -> -90f;
                    case UP -> 90f;
                    default -> 0f;
                };
                yaw = switch (d) {
                    case SOUTH -> 180F;
                    case WEST -> -90F;
                    case EAST -> 90F;
                    default -> 0F;
                };
                if (block instanceof PistonBlock) {
                    pistonBlockState = stateSchematic;
                    EasyPlaceFix.modifyBoolean = true;
                }
            }
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            minecraftClient.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, mc.player.isOnGround()));
        } else if (stateSchematic.contains(Properties.AXIS)) {
            Direction.Axis axis = stateSchematic.get(Properties.AXIS);
            switch (axis.ordinal()) {
                case 0 -> side2.set(Direction.EAST);//x
                case 1 -> side2.set(Direction.DOWN);//y
                case 2 -> side2.set(Direction.NORTH);//z
            }
            //AXIS属性最终在数据包中仍然依靠face字段来判断


        } else if (stateSchematic.contains(Properties.STRAIGHT_RAIL_SHAPE)) {//不能转弯的铁轨
            RailShape railShape = stateSchematic.get(Properties.STRAIGHT_RAIL_SHAPE);
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            if (railShape == RailShape.NORTH_SOUTH) {
                minecraftClient.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(0f, 0f, mc.player.isOnGround()));
            } else {
                minecraftClient.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(90f, 0f, mc.player.isOnGround()));
            }

        } else if (stateSchematic.contains(Properties.RAIL_SHAPE)) {//普通铁轨
            RailShape railShape = stateSchematic.get(Properties.RAIL_SHAPE);
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            if (railShape == RailShape.NORTH_SOUTH) {
                minecraftClient.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(0f, 0f, mc.player.isOnGround()));
            } else {
                minecraftClient.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(90f, 0f, mc.player.isOnGround()));
            }
        } else if (stateSchematic.contains(Properties.ORIENTATION)) {
            Orientation orientation = stateSchematic.get(Properties.ORIENTATION);
            Direction facing = orientation.getFacing();//决定其是垂直还是水平(水平情况附带朝向)
            Direction rotation = orientation.getRotation();//决定垂直情况下的朝向(水平情况均为UP)
            float yaw;
            float pitch = 0f;
            switch (facing) {
                case UP -> {
                    pitch = 90f;
                    yaw = switch (rotation) {
                        case SOUTH -> 0F;
                        case WEST -> 90F;
                        case EAST -> -90F;
                        default -> 180F;
                    };
                }
                case DOWN -> {
                    pitch = -90f;
                    yaw = switch (rotation) {
                        case SOUTH -> 180F;
                        case WEST -> -90F;
                        case EAST -> 90F;
                        default -> 0F;
                    };
                }
                case SOUTH -> yaw = 180F;
                case WEST -> yaw = -90F;
                case EAST -> yaw = 90F;
                default -> yaw = 0F;


            }

            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            minecraftClient.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, mc.player.isOnGround()));
        } else if (stateSchematic.contains(Properties.ROTATION)) {
            Integer rotation = stateSchematic.get(Properties.ROTATION);
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            minecraftClient.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(rotation * 22.5f, 0f, mc.player.isOnGround()));
        }


    }

    @WrapOperation(method = "doEasyPlaceAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;interactBlock(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"))
    private static ActionResult modifyHitResultArgs
            (ClientPlayerInteractionManager instance, ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, Operation<ActionResult> original,
             @Share("side2") LocalRef<Direction> side2,
             @Share("blockPos") LocalRef<BlockPos> blockPosRef,
             @Share("hitVecIn") LocalRef<Vec3d> vec3dLocalRef
            ) {
        if (vec3dLocalRef.get() != null) {
            if (blockPosRef.get() != null)
                vec3dLocalRef.set(PlayerRotation.keepHitVec3safe(blockPosRef.get(), vec3dLocalRef.get()));
            else
                vec3dLocalRef.set(PlayerRotation.keepHitVec3safe(hitResult.getBlockPos(), vec3dLocalRef.get()));

        }
        BlockHitResult blockHitResult2 = new BlockHitResult(
                vec3dLocalRef.get() != null ? vec3dLocalRef.get() : hitResult.getPos(),
                side2.get() != null ? side2.get() : hitResult.getSide(),
                blockPosRef.get() != null ? blockPosRef.get() : hitResult.getBlockPos(),
                hitResult.isInsideBlock()
        );
        return original.call(instance, player, hand, blockHitResult2);


    }

    @WrapOperation(method = "doEasyPlaceAction", at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/materials/MaterialCache;getRequiredBuildItemForState(Lnet/minecraft/block/BlockState;)Lnet/minecraft/item/ItemStack;"))
    private static ItemStack modifyGetRequiredBuildItemForState(MaterialCache instance, BlockState stateSchematic, Operation<ItemStack> original, @Share("stateSchematic") LocalRef<BlockState> stateSchematicRef) {
        stateSchematicRef.set(stateSchematic);//设置一个共享的BlockState,用于在Mixin中共享对应的投影方块
        return original.call(instance, stateSchematic);
    }

    @WrapOperation(method = "doEasyPlaceAction", at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/util/EntityUtils;getUsedHandForItem(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/util/Hand;"))
    private static Hand e(PlayerEntity player, ItemStack stack, Operation<Hand> original, @Share("stateSchematic") LocalRef<BlockState> stateSchematicRef) {
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

    @WrapOperation(method = "doEasyPlaceAction", at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/util/WorldUtils;applyBlockSlabProtocol(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"))
    private static Vec3d slabFix(BlockPos pos, BlockState state, Vec3d hitVecIn, Operation<Vec3d> original, @Share("blockPos") LocalRef<BlockPos> blockPosRef, @Share("hitVecIn") LocalRef<Vec3d> vec3dLocalRef) {
        vec3dLocalRef.set(hitVecIn);
        if (state.contains(Properties.BLOCK_FACE)) {
            BlockFace face = state.get(Properties.BLOCK_FACE);
            double y2 = pos.getY();
            if (face == BlockFace.FLOOR && state.getBlock() != Blocks.GRINDSTONE) {
                blockPosRef.set(pos.add(0, -1, 0));
            } else if (face == BlockFace.CEILING && state.getBlock() != Blocks.GRINDSTONE) {
                blockPosRef.set(pos.add(0, 1, 0));
            } else {
                @Nullable DirectionProperty property = BlockUtils.getFirstDirectionProperty(state);
                if (property != null) {
                    switch (state.get(property)) {
                        case EAST -> blockPosRef.set(pos.add(-1, 0, 0));
                        case WEST -> blockPosRef.set(pos.add(1, 0, 0));
                        case NORTH -> blockPosRef.set(pos.add(0, 0, 1));
                        case SOUTH -> blockPosRef.set(pos.add(0, 0, -1));
                    }


                }

            }

            return new Vec3d(hitVecIn.x, y2, hitVecIn.z);
        }


        return original.call(pos, state, hitVecIn);
    }

}



