package eu.virtusdevelops.simplemachines.listeners;

import eu.virtusdevelops.simplemachines.SimpleMachines;
import eu.virtusdevelops.simplemachines.data.BaseMachine;
import eu.virtusdevelops.simplemachines.data.MachineLocation;
import eu.virtusdevelops.simplemachines.gui.MachineGUI;
import eu.virtusdevelops.simplemachines.managers.MachineManager;
import eu.virtusdevelops.virtuscore.managers.FileManager;
import eu.virtusdevelops.virtuscore.utils.TextUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class InteractListener implements Listener {

    private final MachineManager manager;
    private final FileManager fileManager;
    private final SimpleMachines plugin;

    public InteractListener(MachineManager manager, FileManager fileManager, SimpleMachines simpleMachines){
        this.manager = manager;
        this.fileManager = fileManager;
        this.plugin = simpleMachines;
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMachineBreak(PlayerInteractEvent event){
        if(event.getClickedBlock() == null) return;
        if(event.getClickedBlock().getType() != Material.DISPENSER) return;
        Player player = event.getPlayer();

        Block block = event.getClickedBlock();
        MachineLocation location = new MachineLocation(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());
        BaseMachine machine = manager.getMachineAt(location);

        if(event.getHand() != EquipmentSlot.HAND) return;
        if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {


            if (machine == null) return;
            event.setCancelled(true);
            if(!player.hasPermission("simplemachines.use")) return;

            if (event.getItem() != null && event.getItem().getType() == Material.COAL && !player.isSneaking()) {
                if(machine.getFuel() == machine.getMax_fuel()){
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
                    player.sendMessage(TextUtils.colorFormat("&cMachine is at max fuel!"));
                    return;
                }

                if(machine.getFuel() + 100 > machine.getMax_fuel()){
                    machine.setFuel(machine.getMax_fuel());
                }else{
                    machine.addFuel(100);
                }

                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10, 1);
                player.sendMessage(TextUtils.colorFormat("&7Added fuel &c" + machine.getFuel() + "&7/&c" + machine.getMax_fuel()));
                //machine.setUpdated(true);
                machine.update();
                if (player.getGameMode() != GameMode.CREATIVE) {
                    event.getItem().setAmount(event.getItem().getAmount() - 1);
                }

            } else if (!player.isSneaking()) {
                new MachineGUI(player, machine, fileManager, plugin);
            }
        }
    }
}
