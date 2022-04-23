package eu.virtusdevelops.simplemachines.data;

import eu.virtusdevelops.simplemachines.utils.BlockUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class MinerMachine extends BaseMachine{



    public MinerMachine(int id, UUID placedBy, MachineLocation location, int size,
                        int max_size, int fuel, int max_fuel, int speed, int max_speed, ItemStack tool, String config_name, int actual_speed){
        super(id, placedBy, location, size, max_size, fuel, max_fuel, speed, max_speed, tool, config_name, MachineType.MINER, actual_speed);
    }




    // TODO: add option to store items in chest behind the machine.

    @Override
    public void tick(int tick) {
        if(tick % getActual_speed() != 0) return;
        if(!isChunkLoaded()) return;
        Block block = getLocation().getBukkitLocation().getBlock();
        if(getFuel() < 1) return;


        BlockData data = block.getBlockData();

        if(data instanceof Directional){
            List<Block> blocks = BlockUtil.getFlatSquare(block, getSize()/2, ((Directional) data).getFacing());
            setUpdated(true);

            removeFuel(1);
            for(Block block1 : blocks){
                if(block1.getType().isSolid()) {
                    block1.getWorld().playEffect(block1.getLocation(), Effect.STEP_SOUND, block1.getType());
                    block1.breakNaturally(getTool());
                }
            }
        }

    }



}
