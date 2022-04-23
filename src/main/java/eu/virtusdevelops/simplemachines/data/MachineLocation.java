package eu.virtusdevelops.simplemachines.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

public class MachineLocation {
    private int x;
    private int y;
    private int z;
    private String world;


    public MachineLocation(int x, int y, int z, String world){
        this.x = x; this.y = y; this.z = z;
        this.world = world;
    }

    public double getDistance(MachineLocation beaconLocation){
        return Math.pow(x-beaconLocation.x, 2) + Math.pow(y-beaconLocation.y, 2) + Math.pow(z-beaconLocation.z, 2);
    }

    public double getDistance(Location loc){
        return Math.pow(x-loc.getBlockX(), 2) + Math.pow(y-loc.getBlockY(), 2) + Math.pow(z-loc.getBlockZ(), 2);
    }

    public Location getBukkitLocation(){
        return new Location(getWorld() , x, y, z);
    }

    public World getWorld() {
        if (this.world == null) {
            return null;
        }
        World world = Bukkit.getWorld(this.world);
        if(world == null) return null;
        return world;
    }

    public String getWorldName(){
        return this.world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public String toString(){
        return world + ":" + x + ":" + y + ":" + z;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MachineLocation location = (MachineLocation) o;
        return x == location.x && y == location.y && z == location.z && world.equals(location.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, world);
    }
}
