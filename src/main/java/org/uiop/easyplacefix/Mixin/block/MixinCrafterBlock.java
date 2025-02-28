package org.uiop.easyplacefix.Mixin.block;

import fi.dy.masa.litematica.world.SchematicWorldHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.CrafterBlock;
import net.minecraft.block.entity.CrafterBlockEntity;
import net.minecraft.block.enums.Orientation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.SlotChangedStateC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.EasyPlaceFix;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.LookAt;
import org.uiop.easyplacefix.until.PlayerBlockAction;

import static org.uiop.easyplacefix.EasyPlaceFix.crafterOperation;
import static org.uiop.easyplacefix.EasyPlaceFix.crafterSlot;

@Mixin(value = CrafterBlock.class)
public class MixinCrafterBlock implements IBlock {
    @Override
    public boolean HasSleepTime(BlockState blockState) {
        Orientation orientation = blockState.get(Properties.ORIENTATION);
        Direction facing = orientation.getFacing();//决定其是垂直还是水平(水平情况附带朝向)
        return facing != Direction.UP && facing != Direction.DOWN;
    }

    @Override
    public Pair<LookAt, LookAt> getYawAndPitch(BlockState blockState) {

        Orientation orientation = blockState.get(Properties.ORIENTATION);
        Direction facing = orientation.getFacing();//决定其是垂直还是水平(水平情况附带朝向)
        Direction rotation = orientation.getRotation();//决定垂直情况下的朝向(水平情况均为UP)
        return switch (facing) {
            case UP -> switch (rotation) {
                case SOUTH -> new Pair<>(LookAt.South, LookAt.Down);
                case WEST -> new Pair<>(LookAt.West, LookAt.Down);
                case EAST -> new Pair<>(LookAt.East, LookAt.Down);
                default -> new Pair<>(LookAt.North, LookAt.Down);
            };
            case DOWN -> switch (rotation) {
                case SOUTH -> new Pair<>(LookAt.North, LookAt.Up);
                case WEST -> new Pair<>(LookAt.East, LookAt.Up);
                case EAST -> new Pair<>(LookAt.West, LookAt.Up);
                default -> new Pair<>(LookAt.South, LookAt.Up);
            };
            case SOUTH -> new Pair<>(LookAt.North, LookAt.Horizontal);
            case WEST -> new Pair<>(LookAt.East, LookAt.Horizontal);
            case EAST -> new Pair<>(LookAt.West, LookAt.Horizontal);
            case NORTH -> new Pair<>(LookAt.South, LookAt.Horizontal);
        };
    }

    @Override
    public void BlockAction(BlockState blockState, BlockHitResult blockHitResult) {
        ClientPlayNetworkHandler clientPlayNetworkHandler = MinecraftClient.getInstance().getNetworkHandler();
        CrafterBlockEntity blockEntity = (CrafterBlockEntity) SchematicWorldHandler.getSchematicWorld().getBlockEntity(blockHitResult.getBlockPos());
        for (int i = 0; i < 9; i++) {//TODO
            boolean isDisabled = blockEntity.isSlotDisabled(i);
            crafterSlot.set(i, isDisabled);
            if (!crafterOperation && isDisabled) {
                crafterOperation = true;
            }
        }
        if (crafterOperation) {
            PlayerBlockAction.openScreenAction.taskQueue.offer(() -> {
                for (int slot = 0; slot < crafterSlot.size(); slot++) {
                    boolean isDisable = crafterSlot.get(slot);
                    if (isDisable) {
                        clientPlayNetworkHandler.sendPacket(new SlotChangedStateC2SPacket(slot, EasyPlaceFix.screenId, false));//TODO
                        clientPlayNetworkHandler.sendPacket(new ClickSlotC2SPacket(EasyPlaceFix.screenId, EasyPlaceFix.screenId, slot, 0, SlotActionType.PICKUP, ItemStack.EMPTY, new Int2ObjectOpenHashMap<>()));
                    }

                }
                clientPlayNetworkHandler.sendPacket(new CloseHandledScreenC2SPacket(EasyPlaceFix.screenId));
            });
            clientPlayNetworkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,blockHitResult,0));
        }


//        var BlockActionPacket = new PlayerInteractBlockC2SPacket(
//                Hand.MAIN_HAND,
//                blockHitResult,
//                ((IClientWorld) MinecraftClient.getInstance().world).Sequence()
//        );
//
//        ((IisSimpleHitPos) BlockActionPacket).setSimpleHitPos();
//        clientPlayNetworkHandler.sendPacket(BlockActionPacket);
    }

}
//  if (stateSchematic.contains(Properties.ORIENTATION)) {
//CrafterBlockEntity blockEntity = (CrafterBlockEntity) SchematicWorldHandler.getSchematicWorld().getBlockEntity(trace.getBlockPos());
//                for (int i = 0; i < 9; i++) {//TODO
//boolean isDisabled = blockEntity.isSlotDisabled(i);
//                    crafterSlot.set(i, isDisabled);
//                    if (!crafterOperation && isDisabled) {
//crafterOperation = true;
//        }
//        }
//
//        }