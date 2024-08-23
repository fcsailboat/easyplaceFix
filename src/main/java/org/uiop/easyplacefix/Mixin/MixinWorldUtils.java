package org.uiop.easyplacefix.Mixin;


import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import fi.dy.masa.litematica.materials.MaterialCache;
import fi.dy.masa.litematica.util.EntityUtils;
import fi.dy.masa.litematica.util.RayTraceUtils;
import fi.dy.masa.litematica.util.WorldUtils;

import fi.dy.masa.malilib.util.BlockUtils;
import net.minecraft.block.*;
import net.minecraft.block.enums.*;
import net.minecraft.client.MinecraftClient;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

import net.minecraft.util.math.Direction;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.uiop.easyplacefix.EasyPlaceFix;

import java.util.function.Predicate;

import static org.uiop.easyplacefix.EasyPlaceFix.blockState;
import static org.uiop.easyplacefix.EasyPlaceFix.findBlockInInventory;
import static org.uiop.easyplacefix.config.easyPlacefixConfig.LOOSEN_MODE;


@Mixin(WorldUtils.class)
public abstract class MixinWorldUtils {
    @Unique
    private static Direction side2 =null;
    @ModifyReturnValue(method = "doEasyPlaceAction",at = @At(value ="RETURN"))
    private static ActionResult ok(ActionResult original, @Local RayTraceUtils.RayTraceWrapper traceWrapper, @Share("stateSchematic")LocalRef<BlockState> stateSchematicRef){
        if (original!=ActionResult.FAIL){
            MinecraftClient mc = MinecraftClient.getInstance();
            if (traceWrapper==null){
                return original;
            }
            if (stateSchematicRef.get()==null)return ActionResult.PASS;

            BlockHitResult trace = traceWrapper.getBlockHitResult();
            BlockState stateSchematic =stateSchematicRef.get();
              Block block =  stateSchematic.getBlock();

             if (block ==Blocks.REPEATER){
                 int delay =   stateSchematic.get(Properties.DELAY);
                 for (int i=0;i<delay-1;i++){
                     mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,trace,i));
                 }
             }else if (block instanceof TrapdoorBlock||block instanceof FenceGateBlock) {
                 boolean open = stateSchematic.get(Properties.OPEN);
                 if (open){
                     mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,trace,0));
                 }

             }else if (block==Blocks.COMPARATOR){
                ComparatorMode comparatorMode = stateSchematic.get(Properties.COMPARATOR_MODE);
                if (comparatorMode ==ComparatorMode.SUBTRACT){
                    mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,trace,0));
                }
             }
//             else if (block ==Blocks.LEVER) {
//
//             }没有很多地方会用到大量开启的拉杆


        }


        return original;
    }
    @Inject(method = "doEasyPlaceAction",at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/util/WorldUtils;cacheEasyPlacePosition(Lnet/minecraft/util/math/BlockPos;)V",shift = At.Shift.AFTER))
    private static void ex(MinecraftClient mc, CallbackInfoReturnable<ActionResult> cir, @Share("stateSchematic")LocalRef<BlockState> stateSchematicRef){
       BlockState stateSchematic = stateSchematicRef.get(); //直接使用共享的值，不需要重新获取
        @Nullable DirectionProperty property = BlockUtils.getFirstDirectionProperty(stateSchematic);
        if (property != null) {
            Block block = stateSchematic.getBlock();
            Direction d = stateSchematic.get(property);

                if (block ==Blocks.HOPPER){//漏斗
                    side2 = d.getOpposite();//这个是我今天发现的方法
                }
                else if (stateSchematic.contains(Properties.BLOCK_HALF)){
                  BlockHalf blockHalf = stateSchematic.get(Properties.BLOCK_HALF);
                  if (blockHalf==BlockHalf.BOTTOM){
                      side2=Direction.UP;
                  }else side2=Direction.DOWN;
                }
                else {
                    side2 =d;
                    //墙上的告示牌，末地烛，避雷针，墙上的火把
                }

            // Ctrl + Alt + V
            // 别用var 自动类型推导会降低程序可读性 好的，

            float yaw, pitch;
            // 尽量用确定的值去equals不确定的值，不确定的值有可能是null
            if (block == Blocks.OBSERVER ) {
                pitch = switch (d) {
                    case DOWN -> 90f;
                    case UP -> -90f;
                    default -> 0f;
                };
                yaw = switch (d) {
                    // 我感觉你的idea需要先设置一下
                    case SOUTH -> 0F;
                    case  WEST-> 90F;
                    case EAST -> -90F;
                    default -> 180F;
                };//这里不同的方块相对应的朝向有一些不同，侦测器和活塞是完全相反的，楼梯的话是只有上下不相反
            }
            else if (block instanceof StairsBlock||block instanceof FenceGateBlock ||block instanceof DoorBlock) {
                pitch = switch (d) {
                    case DOWN -> -90f;
                    case UP -> 90f;
                    default -> 0f;
                };
                yaw = switch (d) {
                    case SOUTH -> 0F;
                    case  WEST-> 90F;
                    case EAST -> -90F;
                    default -> 180F;
                };
            }
            else if (block ==Blocks.ANVIL) {
                pitch = 0f;
                yaw = switch (d) {
                    // 我感觉你的idea需要先设置一下
                    case SOUTH -> -90F;
                    case WEST-> 180F;
                    case EAST -> -180F;
                    default -> 90F;
                };
            }


            else {
                pitch = switch (d) {
                    case DOWN -> -90f;
                    case UP -> 90f;
                    default -> 0f;
                };
                yaw = switch (d) {
                    case SOUTH -> 180F;
                    case WEST-> -90F;
                    case EAST -> 90F;
                    default -> 0F;
                };
                if (block instanceof PistonBlock){
                    blockState =stateSchematic;
                   EasyPlaceFix.modifyBoolean=true;
                }
            }
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            minecraftClient.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, mc.player.isOnGround()));
        }
        else if(stateSchematic.contains(Properties.AXIS)){
            Direction.Axis axis =   stateSchematic.get(Properties.AXIS);
            if (axis.getType().getFacingCount()==4){
                side2=Direction.EAST;
            }else{
                side2=Direction.DOWN;

            }
            //AXIS属性最终在数据包中仍然依靠face字段来判断



        }
        else if (stateSchematic.contains(Properties.RAIL_SHAPE)) {
              RailShape railShape= stateSchematic.get(Properties.RAIL_SHAPE);
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            if (railShape==RailShape.NORTH_SOUTH){
                minecraftClient.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(0f, 0f, mc.player.isOnGround()));
           }else{
                minecraftClient.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(90f, 0f, mc.player.isOnGround()));
           }

        }
        else if (stateSchematic.contains(Properties.ORIENTATION)) {
            Orientation orientation =stateSchematic.get(Properties.ORIENTATION);
            Direction facing= orientation.getFacing();//决定其是垂直还是水平(水平情况附带朝向)
            Direction  rotation  =orientation.getRotation();//决定垂直情况下的朝向(水平情况均为UP)
            float yaw;
            float pitch = 0f;
           switch (facing){
                case UP ->{
                    pitch=90f;
                     yaw = switch (rotation){
                        case SOUTH ->0F;
                        case WEST-> 90F;
                        case EAST -> -90F;
                        default -> 180F;
                    };
                }case DOWN -> {
                    pitch=-90f;
                    yaw=switch (rotation){
                        case SOUTH ->180F;
                        case WEST-> -90F;
                        case EAST -> 90F;
                        default -> 0F;
                    };
               }
               case SOUTH ->yaw=180F;
               case WEST-> yaw=-90F;
               case EAST -> yaw=90F;
               default -> yaw=0F;


           }

            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            minecraftClient.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, mc.player.isOnGround()));
        }


    }

    @ModifyArgs(method = "doEasyPlaceAction",at = @At(value = "INVOKE", target = "Lnet/minecraft/util/hit/BlockHitResult;<init>(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Direction;Lnet/minecraft/util/math/BlockPos;Z)V"))
    private static void modify(Args args){
        if (side2!=null){
            args.set(1,side2);
            side2=null;//虽然放置在地上的火把没有side属性,但是会受到数据包中错误的side字段的影响
        }
    }

    @WrapOperation(method = "doEasyPlaceAction", at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/materials/MaterialCache;getRequiredBuildItemForState(Lnet/minecraft/block/BlockState;)Lnet/minecraft/item/ItemStack;"))
    private static ItemStack modifyGetRequiredBuildItemForState(MaterialCache instance, BlockState stateSchematic, Operation<ItemStack> original, @Share("stateSchematic") LocalRef<BlockState> stateSchematicRef) {


        stateSchematicRef.set(stateSchematic);//设置一个共享的BlockState,用于在Mixin中共享对应的投影方块
        ItemStack stack = original.call(instance,stateSchematic); //覆盖方法返回值先调用原方法
        if (LOOSEN_MODE.getBooleanValue()) {
            if ( !EntityUtils.isCreativeMode(MinecraftClient.getInstance().player)){
                if (!stack.isEmpty()){

                    PlayerInventory playerInventory =  MinecraftClient.getInstance().player.getInventory();
                            Block ReplacedBlock = stateSchematic.getBlock();//将被替换的item对应的方块
                            Predicate<Block> predicate = null;

                            if ( ReplacedBlock instanceof  WallBlock)   //墙类
                                predicate =block -> block instanceof WallBlock;
                            else if(ReplacedBlock instanceof FenceGateBlock)//栅栏门
                                predicate =block -> block instanceof FenceGateBlock;
                            else if (ReplacedBlock instanceof TrapdoorBlock)//活板门
                                predicate =block -> block instanceof TrapdoorBlock ;
                            else if (ReplacedBlock instanceof CoralFanBlock)//珊瑚扇
                                predicate =block -> block instanceof CoralFanBlock;

                            if (predicate!=null){
                                if (playerInventory.getSlotWithStack(stack)==-1) {//如果找不到item对应的slot就进行替换
                                  return findBlockInInventory(playerInventory,predicate);
                                }
                            }
                }
            }
        }
        return stack;
    }


}
