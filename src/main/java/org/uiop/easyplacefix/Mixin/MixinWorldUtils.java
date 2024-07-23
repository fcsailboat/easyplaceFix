package org.uiop.easyplacefix.Mixin;

import com.llamalad7.mixinextras.sugar.Local;
import fi.dy.masa.litematica.util.RayTraceUtils;
import fi.dy.masa.litematica.util.WorldUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.malilib.util.BlockUtils;
import net.minecraft.block.*;
import net.minecraft.block.enums.Orientation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;


@Mixin(WorldUtils.class)
public class MixinWorldUtils {
    @Unique
    private static Direction side2 =null;
    @Inject(method = "doEasyPlaceAction",at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/util/WorldUtils;cacheEasyPlacePosition(Lnet/minecraft/util/math/BlockPos;)V",shift = At.Shift.AFTER))
    private static void ex(MinecraftClient mc, CallbackInfoReturnable<ActionResult> cir, @Local RayTraceUtils.RayTraceWrapper traceWrapper){
        BlockHitResult trace = traceWrapper.getBlockHitResult();
        BlockPos pos = trace.getBlockPos();
        World world = SchematicWorldHandler.getSchematicWorld();
        BlockState stateSchematic = world.getBlockState(pos);
        @Nullable DirectionProperty property = BlockUtils.getFirstDirectionProperty(stateSchematic);
        if (property != null) {

            Block block = stateSchematic.getBlock();


            Direction d = stateSchematic.get(property);
            if (!(block instanceof TrapdoorBlock)){

                if (block ==Blocks.HOPPER){//漏斗
                    side2 = d.getOpposite();//这个是我今天发现的方法
                }
                else {
                    side2 =d;
                    //墙上的告示牌，末地烛，避雷针，墙上的火把

                }


            }
            // Ctrl + Alt + V
            // 别用var 自动类型推导会降低程序可读性 好的，

            float yaw, pitch;
            // 尽量用确定的值去equals不确定的值，不确定的值有可能是null
            if (block == Blocks.OBSERVER ) {
                pitch = switch (d) {
                    case Direction.DOWN -> 90f;
                    case Direction.UP -> -90f;
                    default -> 0f;
                };
                yaw = switch (d) {
                    // 我感觉你的idea需要先设置一下
                    case Direction.SOUTH -> 0F;
                    case  Direction.WEST-> 90F;
                    case Direction.EAST -> -90F;
                    default -> 180F;
                };//这里不同的方块相对应的朝向有一些不同，侦测器和活塞是完全相反的，楼梯的话是只有上下不相反
            }
            else if (block instanceof StairsBlock) {
                pitch = switch (d) {
                    case Direction.DOWN -> -90f;
                    case Direction.UP -> 90f;
                    default -> 0f;
                };
                yaw = switch (d) {
                    // 我感觉你的idea需要先设置一下
                    case Direction.SOUTH -> 0F;
                    case  Direction.WEST-> 90F;
                    case Direction.EAST -> -90F;
                    default -> 180F;
                };
            }
            else if (block ==Blocks.ANVIL) {
                pitch = 0f;
                yaw = switch (d) {
                    // 我感觉你的idea需要先设置一下
                    case Direction.SOUTH -> -90F;
                    case  Direction.WEST-> 180F;
                    case Direction.EAST -> -180F;
                    default -> 90F;
                };
            }


            else {
                pitch = switch (d) {
                    case Direction.DOWN -> -90f;
                    case Direction.UP -> 90f;
                    default -> 0f;
                };
                yaw = switch (d) {
                    // 我感觉你的idea需要先设置一下
                    case Direction.SOUTH -> 180F;
                    case  Direction.WEST-> -90F;
                    case Direction.EAST -> 90F;
                    default -> 0F;
                };
            }
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            minecraftClient.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, true));
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
        else if (stateSchematic.contains(Properties.ORIENTATION)) {
            Orientation orientation =stateSchematic.get(Properties.ORIENTATION);
            float yaw = 0, pitch;
            Direction facing= orientation.getFacing();
            Direction  rotation  =orientation.getRotation();
            if (facing==Direction.UP){
                pitch=90f;
            } else if (facing==Direction.DOWN) {
                pitch=-90f;
            }else {
                yaw=switch (facing){
                    case Direction.SOUTH -> 180F;
                    case  Direction.WEST-> -90F;
                    case Direction.EAST -> 90F;
                default -> 0F;
                };
                pitch=90f;
            }
            if (yaw==0){
                yaw=switch (rotation){
                    case Direction.SOUTH -> 180F;
                    case  Direction.WEST-> -90F;
                    case Direction.EAST -> 90F;
                    default -> 0F;
                };

            }
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            minecraftClient.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, true));
        }


    }

    @ModifyArgs(method = "doEasyPlaceAction",at = @At(value = "INVOKE", target = "Lnet/minecraft/util/hit/BlockHitResult;<init>(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Direction;Lnet/minecraft/util/math/BlockPos;Z)V"))
    private static void modify(Args args){
        if (side2!=null){
            args.set(1,side2);
        }
    }
}
