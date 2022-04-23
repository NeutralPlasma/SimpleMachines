package eu.virtusdevelops.simplemachines;

import eu.virtusdevelops.simplemachines.commands.GiveCommand;
import eu.virtusdevelops.simplemachines.listeners.BreakListener;
import eu.virtusdevelops.simplemachines.listeners.InteractListener;
import eu.virtusdevelops.simplemachines.listeners.PlaceListener;
import eu.virtusdevelops.simplemachines.managers.MachineManager;
import eu.virtusdevelops.simplemachines.storage.SQLStorage;
import eu.virtusdevelops.simplemachines.utils.NBT.NBTUtil;
import eu.virtusdevelops.virtuscore.VirtusCore;
import eu.virtusdevelops.virtuscore.command.CommandManager;
import eu.virtusdevelops.virtuscore.command.MainCommand;
import eu.virtusdevelops.virtuscore.gui.Handler;
import eu.virtusdevelops.virtuscore.managers.FileManager;
import eu.virtusdevelops.virtuscore.utils.FileLocation;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedHashSet;
import java.util.List;

public final class SimpleMachines extends JavaPlugin {

    private static Economy econ = null;
    private MachineManager manager;
    private FileManager fileManager;
    private SQLStorage storage;
    private Handler guiHandler;
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        setupEconomy();
        saveDefaultConfig();

        this.fileManager = new FileManager(this, new LinkedHashSet<>(List.of(
                FileLocation.of("machines.yml", true, false)
        )));
        this.fileManager.loadFiles();

        this.storage = new SQLStorage(this, fileManager);
        this.storage.createTables();
        NBTUtil nbtUtil = new NBTUtil(this);

        this.manager = new MachineManager(this, storage, nbtUtil, fileManager);
        VirtusCore.plugins().registerEvents(new BreakListener(manager), this);
        VirtusCore.plugins().registerEvents(new InteractListener(manager, fileManager, this), this);
        VirtusCore.plugins().registerEvents(new PlaceListener(manager, nbtUtil), this);


        guiHandler = new Handler(this);

        commandManager = new CommandManager(this);

        registerCommands();
    }

    @Override
    public void onDisable() {
        manager.save();
        storage.closeConnections();
        // Plugin shutdown logic
    }




    // section: Economy
    public boolean setupEconomy(){
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
    public Economy getEconomy() {
        return econ;
    }
    // endsection

    public void registerCommands(){
        MainCommand command = commandManager.addMainCommand("simplemachines");

        command.addSubCommands(
                new GiveCommand(manager, fileManager)
        );
    }
}
