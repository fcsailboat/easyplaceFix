package org.uiop.easyplacefix.until;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

public class BlockReRender {
    public static void blockRender(BlockState blockState, BlockPos pos){
        World world= MinecraftClient.getInstance().world;
        PlayerEntity player =MinecraftClient.getInstance().player;
        BlockSoundGroup blockSoundGroup = blockState.getSoundGroup();
        WorldChunk worldChunk= world.getWorldChunk(pos);
        BlockState oldBckState =  worldChunk.setBlockState(pos, blockState, true);
        world.playSound(
                player,
                pos,
                blockSoundGroup.getPlaceSound(),
                SoundCategory.BLOCKS,
                (blockSoundGroup.getVolume() + 1.0F) / 2.0F,
                blockSoundGroup.getPitch() * 0.8F
        );
        world.onBlockChanged(pos,oldBckState,blockState);
    }
}
