package eu.virtusdevelops.simplemachines.data;
import eu.virtusdevelops.simplemachines.managers.MachineManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public abstract class BaseMachine {


    private int id;
    private String config_name;


    private UUID placedBy;
    private MachineLocation location;


    private int speed;
    private int max_speed;
    private int actual_speed;

    private int size;
    private int max_size;

    private int fuel;
    private int max_fuel;

    private ItemStack tool;
    private MachineType type;


    private boolean updated = false;

    public BaseMachine(int id, UUID placedBy, MachineLocation location, int size,
                       int max_size, int fuel, int max_fuel, int speed, int max_speed, ItemStack tool,
                       String config_name, MachineType type, int actual_speed){
        this.id = id;
        this.placedBy = placedBy;
        this.location = location;

        this.size = size;
        this.max_size = max_size;

        this.fuel = fuel;
        this.max_fuel = max_fuel;

        this.speed = speed;
        this.max_speed = max_speed;
        this.actual_speed = actual_speed;

        this.config_name = config_name;

        this.tool = tool;
        this.type = type;
    }



    public int getSize() {
        return size;
    }


    public void tick(int tick) {

    }


    public int getFuel() {
        return this.fuel;
    }


    public void addFuel(int amount) {
        this.fuel += amount;
    }

    public void removeFuel(int amount){
        this.fuel-= amount;
    }

    public ItemStack getTool(){
        return tool;
    }


    public UUID getPlacedBy(){
        return this.placedBy;
    }

    public int getSpeed() {
        return speed;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPlacedBy(UUID placedBy) {
        this.placedBy = placedBy;
    }

    public void setLocation(MachineLocation location) {
        this.location = location;
    }

    public void setFuel(int fuel) {
        this.fuel = fuel;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setTool(ItemStack tool) {
        this.tool = tool;
    }

    public int getId(){
        return this.id;
    }

    public int getMax_fuel() {
        return max_fuel;
    }

    public void setMax_fuel(int max_fuel) {
        this.max_fuel = max_fuel;
    }

    public String getConfig_name() {
        return config_name;
    }

    public void setConfig_name(String config_name) {
        this.config_name = config_name;
    }

    public int getMax_size() {
        return max_size;
    }

    public int getMax_speed() {
        return max_speed;
    }

    public void setMax_size(int max_size) {
        this.max_size = max_size;
    }

    public void setMax_speed(int max_speed) {
        this.max_speed = max_speed;
    }

    public int getActual_speed() {
        return actual_speed;
    }

    public void setActual_speed(int actual_speed) {
        this.actual_speed = actual_speed;
    }

    public void setType(MachineType type) {
        this.type = type;
    }

    public MachineType getType() {
        return type;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public boolean isChunkLoaded(){
        Location loc = location.getBukkitLocation();
        Block block = loc.getBlock();
        if (block.getWorld().isChunkLoaded(location.getX() >> 4, location.getZ() >> 4)) {
            return block.getType() == Material.DISPENSER;
        }
        return false;
    }

    public void update(){
        MachineManager.getInstance().updateMachine(this);
    }

    public MachineLocation getLocation() {
        return location;
    }
}
