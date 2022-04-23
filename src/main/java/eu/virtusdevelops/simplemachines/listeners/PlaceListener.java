package eu.virtusdevelops.simplemachines.listeners;

import eu.virtusdevelops.simplemachines.data.BaseMachine;
import eu.virtusdevelops.simplemachines.data.MachineType;
import eu.virtusdevelops.simplemachines.managers.MachineManager;
import eu.virtusdevelops.simplemachines.utils.NBT.NBTUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class PlaceListener implements Listener {

    private MachineManager manager;
    private NBTUtil nbtUtil;

    public PlaceListener(MachineManager manager, NBTUtil nbtUtil){
        this.manager = manager;
        this.nbtUtil = nbtUtil;
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event){
        if(event.getBlock().getType() != Material.DISPENSER) return;
        String container_data =  nbtUtil.getString(event.getItemInHand(), "config_name");
        if(container_data.equalsIgnoreCase("none")) return;
        if(!event.getPlayer().hasPermission("simplemachines.place")) { event.setCancelled(true); return;}
        int size = nbtUtil.getInt(event.getItemInHand(), "size");
        int speed = nbtUtil.getInt(event.getItemInHand(), "speed");
        MachineType type = MachineType.valueOf(nbtUtil.getString(event.getItemInHand(), "type"));
        Block block = event.getBlock();
        BaseMachine machine = manager.addMachine(container_data, type, event.getPlayer(), block, size, speed);

    }
}
