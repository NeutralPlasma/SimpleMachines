package eu.virtusdevelops.simplemachines.commands;

import eu.virtusdevelops.simplemachines.data.MachineType;
import eu.virtusdevelops.simplemachines.managers.MachineManager;
import eu.virtusdevelops.virtuscore.command.AbstractCommand;
import eu.virtusdevelops.virtuscore.managers.FileManager;
import eu.virtusdevelops.virtuscore.utils.PlayerUtils;
import eu.virtusdevelops.virtuscore.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GiveCommand extends AbstractCommand {
    private MachineManager manager;
    private FileManager fileManager;

    public GiveCommand(MachineManager manager, FileManager fileManager) {
        super(CommandType.BOTH, true, "give");
        this.manager = manager;
        this.fileManager = fileManager;
    }

    @Override
    protected ReturnType runCommand(CommandSender commandSender, String... args) {
        if(args.length >= 1){
            Player target = Bukkit.getPlayer(args[0]);
            if(target == null){ commandSender.sendMessage(TextUtils.colorFormat("&cInvalid player"));return ReturnType.SUCCESS;}


            if( args.length < 2 || fileManager.getConfiguration("machines").getConfigurationSection("machines." + args[1]) == null){ commandSender.sendMessage(TextUtils.colorFormat("&cInvalid machine"));return ReturnType.SUCCESS;}
            ConfigurationSection section = fileManager.getConfiguration("machines").getConfigurationSection("machines." + args[1]);

            MachineType type = MachineType.MINER;

            if(args.length > 2){
                try {
                    type = MachineType.valueOf(args[2]);
                }catch (Exception e){
                    commandSender.sendMessage(TextUtils.colorFormat("&cInvalid type."));
                    return ReturnType.SUCCESS;
                }
            }

            int speed = 1;
            int size = 1;
            int amount = 1;

            if(args.length > 3){
                int max_speed = section.getInt("max_speed");
                int input = Integer.parseInt(args[3]);
                if(input < 1) input = 1;
                if(input > max_speed){
                    input = max_speed;
                }
                speed = input;
            }

            if(args.length > 4){
                int max_size = section.getInt("max_size");
                int input = Integer.parseInt(args[4]);
                if(input < 1) input = 1;
                if(input > max_size){
                    input = max_size;
                }
                size = input;
            }
            if(args.length > 5){
                amount = Integer.parseInt(args[5]);
                if(amount < 0){
                    amount = 1;
                }
            }

            // forgot the type shit ffs


            ItemStack item = manager.createItem(args[1], speed, size, type);
            item.setAmount(amount);
            PlayerUtils.giveItem(target, item, true);
            commandSender.sendMessage(TextUtils.colorFormat("&7Successfully given &8(&c" + amount + "&7x&8)  machine to player: &c" + target.getName()));
            return ReturnType.SUCCESS;
        }

        return ReturnType.SYNTAX_ERROR;
    }

    @Override
    protected List<String> onTab(CommandSender commandSender, String... args) {
        if(args.length == 1){
            return Bukkit.getOnlinePlayers().stream().map(player -> player.getName().toLowerCase())
                    .filter(player ->  player.contains(args[0].toLowerCase())).collect(Collectors.toList());

        }else if(args.length == 2){
            return fileManager.getConfiguration("machines").getConfigurationSection("machines")
                    .getKeys(false).stream().filter(it -> it.contains(args[1].toLowerCase())).collect(Collectors.toList());

        }else if(args.length == 3){
            return Arrays.stream(MachineType.values()).filter(it -> it.toString().contains(args[2].toLowerCase())).map(Enum::toString).toList();
        }else if(args.length == 4){
            return List.of("1", "2", "3", "4", "<speed>");
        }else if(args.length == 5){
            return List.of("1", "2", "3", "4", "<size>");
        }else if(args.length == 6){
            return List.of("1", "2", "3", "4", "<amount>");
        }


        return null;
    }

    @Override
    public String getPermissionNode() {
        return "simplemachines.command.give";
    }

    @Override
    public String getSyntax() {
        return "/simplemachines give <player> <machine> <type> <speed> <size> <amount>";
    }

    @Override
    public String getDescription() {
        return null;
    }
}
