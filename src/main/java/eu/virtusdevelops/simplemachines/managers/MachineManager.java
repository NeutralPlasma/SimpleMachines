package eu.virtusdevelops.simplemachines.managers;

import eu.virtusdevelops.simplemachines.SimpleMachines;
import eu.virtusdevelops.simplemachines.data.*;
import eu.virtusdevelops.simplemachines.storage.SQLStorage;
import eu.virtusdevelops.simplemachines.utils.NBT.NBTUtil;
import eu.virtusdevelops.virtuscore.managers.FileManager;
import eu.virtusdevelops.virtuscore.utils.ItemUtils;
import eu.virtusdevelops.virtuscore.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MachineManager {
    private SimpleMachines plugin;
    private SQLStorage storage;
    private NBTUtil nbtUtil;
    private FileManager fileManager;
    private int tick;
    Map<MachineLocation, BaseMachine> machines = new HashMap<>();

    private static MachineManager INSTANCE;

    BukkitTask task;
    BukkitTask updater;

    public MachineManager(SimpleMachines plugin, SQLStorage storage, NBTUtil nbtUtil, FileManager fileManager){
        this.plugin = plugin;
        this.storage = storage;
        this.nbtUtil = nbtUtil;
        this.fileManager = fileManager;


        /*for(int i = 0; i < 1; i++) {
            MachineLocation location = new MachineLocation(-4, 72,-2 + i, "world");
            machines.put(location, new MinerMachine(
                    1,
                    UUID.randomUUID(),
                    location,
                    1,
                    10,
                    0,
                    300,
                    100,
                    2,
                    new ItemStack(Material.WOODEN_PICKAXE),
                    "simple_miner",
                    100
            ));
        }*/

        INSTANCE = this;
        load();
        start();
    }


    public void load(){
        for(BaseMachine machine : storage.getAllMachines()){
            machines.put(machine.getLocation(), machine);
        }
    }

    public void start(){
        tick = 0;
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            for(BaseMachine machine : machines.values()){
                machine.tick(tick);
            }
            tick++;

        }, 0L, 2L);

        startUpdater();
    }

    public void stop(){
        if(task != null && !task.isCancelled()){
            task.cancel();
        }
        stopUpdater();
    }

    public BaseMachine getMachineAt(MachineLocation location){
        return machines.get(location);
    }

    public void removeAndDrop(MachineLocation location){
        BaseMachine machine = machines.get(location);
        if(machine == null) return;
        ItemStack item = createItem(machines.get(location));
        location.getBukkitLocation().getWorld().dropItemNaturally(location.getBukkitLocation(), item);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, (runnable) -> {
            storage.removeMachine(machine);
        });

        machines.remove(location);
    }


    public ItemStack createItem(BaseMachine machine){
        /*ItemStack itemStack = new ItemStack(Material.DISPENSER);
        ItemMeta meta = itemStack.getItemMeta();
        meta = nbtUtil.setString(meta, machine.getConfig_name(), "config_name");
        meta = nbtUtil.setInt(meta, machine.getSize(), "size");
        meta = nbtUtil.setInt(meta, machine.getSpeed(), "speed");

        if(machine instanceof MinerMachine){
            meta = nbtUtil.setString(meta, MachineType.MINER.toString(), "type");
        }*/

        return createItem(machine.getConfig_name(), machine.getSize(), machine.getSpeed(), machine.getType());

        //itemStack.setItemMeta(meta);
        //return new ItemStack(itemStack);
    }


    public ItemStack createItem(String name, int speed, int size, MachineType type){
        ItemStack itemStack = new ItemStack(Material.DISPENSER);
        ItemMeta meta = itemStack.getItemMeta();
        meta = nbtUtil.setString(meta, name, "config_name");
        meta = nbtUtil.setInt(meta, size, "size");
        meta = nbtUtil.setInt(meta, speed, "speed");


        meta = nbtUtil.setString(meta, type.toString(), "type");

        itemStack.setItemMeta(meta);

        // rename the item
        itemStack = ItemUtils.rename(itemStack, TextUtils.colorFormat("&8[&cMachine&8]"));
        itemStack = ItemUtils.setLore(itemStack, TextUtils.colorFormatList(List.of(
                "&8&m--------------",
                "&c&l| &7Size: &c" + size + "&7x&c" +size,
                "&c&l| &7Speed: &c" + speed,
                "&c&l| &7Type: &c" + type.toString()
        )));


        return new ItemStack(itemStack);
    }

    public BaseMachine addMachine(String config_name, MachineType type, Player player, Block block, int size, int speed){
        ConfigurationSection section = fileManager.getConfiguration("machines").getConfigurationSection("machines");
        int max_speed = section.getInt(config_name + ".max_speed");  // get data from machines.yml
        int max_size = section.getInt(config_name + ".max_size");
        int max_fuel = section.getInt(config_name + ".max_fuel");
        int actual_speed = section.getInt(config_name + ".speeds." + speed);

        switch (type){
            case MINER -> {

                final BaseMachine[] machine = {new MinerMachine(
                        0,
                        player.getUniqueId(),
                        new MachineLocation(block.getX(), block.getY(), block.getZ(), block.getWorld().getName()),
                        size,
                        max_size,
                        0,
                        max_fuel,
                        speed,
                        max_speed,
                        new ItemStack(Material.WOODEN_PICKAXE),
                        config_name,
                        actual_speed
                )};

                machines.put(machine[0].getLocation(), machine[0]);
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    machines.remove(machine[0].getLocation());
                    machine[0] = storage.addMachine(machine[0], type);
                    machines.put(machine[0].getLocation(), machine[0]);
                });

                return machine[0];
            }
            case CRAFTER -> {
                final BaseMachine[] machine = {new CrafterMachine(
                        0,
                        player.getUniqueId(),
                        new MachineLocation(block.getX(), block.getY(), block.getZ(), block.getWorld().getName()),
                        size,
                        max_size,
                        0,
                        max_fuel,
                        speed,
                        max_speed,
                        new ItemStack(Material.CRAFTING_TABLE),
                        config_name,
                        actual_speed
                )};

                machines.put(machine[0].getLocation(), machine[0]);
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    machines.remove(machine[0].getLocation());
                    machine[0] = storage.addMachine(machine[0], type);
                    machines.put(machine[0].getLocation(), machine[0]);
                });

                return machine[0];
            }
        }



        return null;
    }

    public void updateMachine(MachineLocation location) {
        this.updateMachine(machines.get(location));
        //storage.updateMachine(machines.get(location));
    }

    public void updateMachine(BaseMachine machine) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, (run) -> {
            storage.updateMachine(machine);
        });
    }

    public void startUpdater(){
        if(updater != null){
            if(updater.isCancelled()) updater.cancel();
        }

        updater = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::save, 0L, 1200L);
    }

    public void stopUpdater(){
        updater.cancel();
    }


    public void save(){
        for(BaseMachine machine : machines.values()){
            if(machine.isUpdated()){
                storage.updateMachine(machine);
                machine.setUpdated(false);
            }
        }
    }


    public static MachineManager getInstance(){
        return INSTANCE;
    }


}
