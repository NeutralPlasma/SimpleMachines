 package eu.virtusdevelops.simplemachines.utils;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockUtil {


    public static List<Block> getFlatSquare(Block start, int radius, BlockFace direction){
        if(radius < 0) return Collections.emptyList();
        int iterations = (radius * 2) + 1;
        int x1 = 0;
        int y1 = 0;
        int z1 = 0;
        switch(direction){
            case SOUTH -> {
                z1 = radius+1;
            }
            case EAST -> {
                x1 = radius+1;
            }
            case WEST -> {
                x1 = - (radius+1);
            }
            case NORTH -> {
                z1 = - (radius+1);
            }
            case UP -> {
                y1 = radius+1;
            }
            case DOWN -> {
                y1 = - (radius+1);
            }
        }

        List<Block> blocks = new ArrayList<>(iterations * iterations * iterations);
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                blocks.add(start.getRelative(x + x1, y1, z + z1));
            }
        }
        return blocks;
    }
}
