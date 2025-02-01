package org.uiop.easyplacefix.until;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class doEasyPlace {//TODO 轻松放置重写计划

    // 是否在原理图范围内
    public static boolean isSchematicBlock(BlockPos pos) {
        SchematicPlacementManager schematicPlacementManager = DataManager.getSchematicPlacementManager();

        List<SchematicPlacementManager.PlacementPart> allPlacementsTouchingChunk
                = schematicPlacementManager.getAllPlacementsTouchingChunk(pos);

        for (SchematicPlacementManager.PlacementPart placementPart : allPlacementsTouchingChunk) {
            if (placementPart.getBox().containsPos(pos)) {
                return true;
            }
        }
        return false;
    }
}
