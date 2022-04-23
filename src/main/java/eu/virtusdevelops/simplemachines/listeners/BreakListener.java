package eu.virtusdevelops.simplemachines.listeners;

import eu.virtusdevelops.simplemachines.data.BaseMachine;
import eu.virtusdevelops.simplemachines.data.MachineLocation;
import eu.virtusdevelops.simplemachines.managers.MachineManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BreakListener implements Listener {
    private MachineManager manager;

    public BreakListener(MachineManager manager){
        this.manager = manager;
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMachineBreak(BlockBreakEvent event){
        if(event.getBlock().getType() != Material.DISPENSER) return;
        Block block = event.getBlock();
        MachineLocation location = new MachineLocation(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());
        BaseMachine machine = manager.getMachineAt(location);
        if(machine == null) return;
        event.setCancelled(true);

        if(!event.getPlayer().hasPermission("simplemachines.break")) { return;}

        manager.removeAndDrop(location);
        event.getBlock().setType(Material.AIR);
        //event.getPlayer().sendMessage("Deleted machine!");
    }
}
