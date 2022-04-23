package eu.virtusdevelops.simplemachines.gui;

import eu.virtusdevelops.simplemachines.SimpleMachines;
import eu.virtusdevelops.simplemachines.data.BaseMachine;
import eu.virtusdevelops.simplemachines.managers.MachineManager;
import eu.virtusdevelops.virtuscore.gui.Icon;
import eu.virtusdevelops.virtuscore.gui.InventoryCreator;
import eu.virtusdevelops.virtuscore.managers.FileManager;
import eu.virtusdevelops.virtuscore.utils.ItemUtils;
import eu.virtusdevelops.virtuscore.utils.TextUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MachineGUI {
    private InventoryCreator inventory;
    private Player player;
    private BaseMachine baseMachine;
    private ConfigurationSection section;
    private SimpleMachines plugin;


    public MachineGUI(Player player, BaseMachine baseMachine, FileManager fileManager, SimpleMachines plugin){
        this.player = player;
        this.baseMachine = baseMachine;
        this.section = fileManager.getConfiguration("machines").getConfigurationSection("machines." + baseMachine.getConfig_name());
        this.plugin = plugin;
        inventory = new InventoryCreator(9, TextUtils.colorFormat("&8[&cMachine configuration&8]"));

        load();

    }

    private void infoItem(){
        ItemStack machineInfoItem = new ItemStack(Material.DISPENSER);
        machineInfoItem = ItemUtils.rename(machineInfoItem, TextUtils.colorFormat("&8[&cMachine info&8]"));
        machineInfoItem = ItemUtils.setLore(machineInfoItem, TextUtils.colorFormatList(List.of(
                "&8&m----------------",
                "&c&l| &7Speed: &c" + baseMachine.getSpeed() + "&7/&c" + baseMachine.getMax_speed() ,
                "&c&l| &7Size: &c" + baseMachine.getSize() + "&7x&c" + baseMachine.getSize() + " &7/&c " + baseMachine.getMax_size() + "&7x&c" + baseMachine.getMax_size(),
                "&c&l| &7Fuel: &c" + baseMachine.getFuel() + "&7/&c" + baseMachine.getMax_fuel()
        )));
        Icon machineInfo = new Icon(machineInfoItem);
        machineInfo.addClickAction(player1 -> {
            player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 10, 1);
            load();
        });

        inventory.setIcon(4, machineInfo);
    }

    private void toolItem(){
        ItemStack machineInfoItem = baseMachine.getTool();
        machineInfoItem = ItemUtils.rename(machineInfoItem, TextUtils.colorFormat("&8[&cMachine tool&8]"));
        machineInfoItem = ItemUtils.setLore(machineInfoItem, TextUtils.colorFormatList(List.of(
                "&8&m----------------",
                "&c&l| &7Drag & drop"
        )));
        Icon machineInfo = new Icon(machineInfoItem);
        machineInfo.addDragItemIntoAction((player,item) -> {
            if(item.getType() == Material.AIR){
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
                return;
            }
            player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 10, 1);
            baseMachine.setTool(item.clone());
            item.setAmount(0);

            baseMachine.update();
            //manager.updateMachine(baseMachine);
            load();
        });

        inventory.setIcon(5, machineInfo);
    }


    private void sizeUpgrade(){
        ItemStack machineInfoItem = new ItemStack(Material.CARROT);
        machineInfoItem = ItemUtils.rename(machineInfoItem, TextUtils.colorFormat("&8[&cMachine size&8]"));

        boolean canUpgrade = false;

        double balance = plugin.getEconomy().getBalance(player);

        if(baseMachine.getSize() < baseMachine.getMax_size()){
            int price = section.getInt("prices.size." + (baseMachine.getSize()+1));

            if(balance >= price) {
                machineInfoItem = ItemUtils.setLore(machineInfoItem, TextUtils.colorFormatList(List.of(
                        "&8&m----------------",
                        "&c&l| &7Click to upgrade",
                        "&c&l| &7Price: $&a" + price
                )));
                canUpgrade = true;
            }else{
                machineInfoItem = ItemUtils.setLore(machineInfoItem, TextUtils.colorFormatList(List.of(
                        "&8&m----------------",
                        "&c&l| &7Click to upgrade",
                        "&c&l| &7Price: $&c" + price
                )));
                canUpgrade = false;
            }

        }else{
            machineInfoItem = ItemUtils.setLore(machineInfoItem, TextUtils.colorFormatList(List.of(
                    "&8&m----------------",
                    "&c&l| &7Maxed"
            )));
        }


        Icon machineInfo = new Icon(machineInfoItem);
        boolean finalCanUpgrade = canUpgrade;

        machineInfo.addClickAction((player) -> {


            if(finalCanUpgrade){
                // check money blab blablaa
                int price = section.getInt("prices.size." + (baseMachine.getSpeed()+1));
                plugin.getEconomy().withdrawPlayer(player, price);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10, 1);
                baseMachine.setSize(baseMachine.getSize()+1);
                baseMachine.update();
                //manager.updateMachine(baseMachine);
            }else{
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
            }
            load();
        });


        inventory.setIcon(3, machineInfo);
    }


    private void speedUpgrade(){
        ItemStack machineInfoItem = new ItemStack(Material.SUGAR);
        machineInfoItem = ItemUtils.rename(machineInfoItem, TextUtils.colorFormat("&8[&cMachine speed&8]"));

        boolean canUpgrade = false;

        double balance = plugin.getEconomy().getBalance(player);

        if(baseMachine.getSpeed() < baseMachine.getMax_speed()){
            int price = section.getInt("prices.speed." + (baseMachine.getSpeed()+1));

            if(balance >= price) {
                machineInfoItem = ItemUtils.setLore(machineInfoItem, TextUtils.colorFormatList(List.of(
                        "&8&m----------------",
                        "&c&l| &7Click to upgrade",
                        "&c&l| &7Price: $&a" + price
                )));
                canUpgrade = true;
            }else{
                machineInfoItem = ItemUtils.setLore(machineInfoItem, TextUtils.colorFormatList(List.of(
                        "&8&m----------------",
                        "&c&l| &7Click to upgrade",
                        "&c&l| &7Price: $&c" + price
                )));
                canUpgrade = false;
            }
        }else{
            machineInfoItem = ItemUtils.setLore(machineInfoItem, TextUtils.colorFormatList(List.of(
                    "&8&m----------------",
                    "&c&l| &7Maxed"
            )));
        }


        Icon machineInfo = new Icon(machineInfoItem);
        boolean finalCanUpgrade = canUpgrade;

        machineInfo.addClickAction((player) -> {
            if(finalCanUpgrade){
                int speed = section.getInt("speeds." + (baseMachine.getSpeed()+1));

                if(speed == 0 || speed < 0){
                    speed = section.getInt("speeds.-1");
                }

                int price = section.getInt("prices.speed." + (baseMachine.getSpeed()+1));
                plugin.getEconomy().withdrawPlayer(player, price);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10, 1);
                baseMachine.setSpeed(baseMachine.getSpeed()+1);
                baseMachine.setActual_speed(speed);

                baseMachine.update();
                //manager.updateMachine(baseMachine);
            }else{
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
            }
            load();
        });

        inventory.setIcon(1, machineInfo);
    }

    private void load(){
        infoItem();
        toolItem();
        sizeUpgrade();
        speedUpgrade();



        inventory.setBackground(new ItemStack(Material.WHITE_STAINED_GLASS_PANE));
        player.openInventory(inventory.getInventory(InventoryType.DISPENSER));
    }
}
