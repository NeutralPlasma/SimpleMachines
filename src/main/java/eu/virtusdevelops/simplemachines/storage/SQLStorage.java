package eu.virtusdevelops.simplemachines.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import eu.virtusdevelops.simplemachines.SimpleMachines;
import eu.virtusdevelops.simplemachines.data.*;
import eu.virtusdevelops.virtuscore.managers.FileManager;
import eu.virtusdevelops.virtuscore.utils.ItemUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLStorage {
    private HikariDataSource dataSource;
    private SimpleMachines plugin;
    private FileManager fileManager;
    // TODO data structure blabla.

    private static String TABLE_NAME = "simplemachines_data";



    public SQLStorage(SimpleMachines plugin, FileManager fileManager){
        this.plugin = plugin;
        this.fileManager = fileManager;
        String path = plugin.getDataFolder().getPath();

        HikariConfig config = new HikariConfig();
        config.setPoolName("SimpleBeacons");
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + path + "/database.sqlite");
        config.setMaxLifetime(60000);
        config.setMaximumPoolSize(10);


        this.dataSource = new HikariDataSource(config);
    }

    public void closeConnections(){
        if(!dataSource.isClosed()){
            dataSource.close();
        }
    }


    public void createTables() {
        String sql1 = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "placed_by VARCHAR(36) NOT NULL," +
                "machine_size INT NOT NULL," +
                "fuel INT NOT NULL," +
                "speed INT NOT NULL," +
                "tool TEXT," +
                "location_x INT NOT NULL," +
                "location_y INT NOT NULL," +
                "location_z INT NOT NULL," +
                "location_world VARCHAR(60) NOT NULL," +
                "config_name VARCHAR(60) NOT NULL," +
                "machine_type VARCHAR(36) NOT NULL)";


        try (Connection connection = dataSource.getConnection();) {
            PreparedStatement st2 = connection.prepareStatement(sql1);
            st2.execute();
        } catch (SQLException error) {
            error.printStackTrace();
        }
    }

    public BaseMachine getMachine(BaseMachine machine) {
        String SQL = "SELECT * FROM " + TABLE_NAME + " WHERE location_x = ? AND location_y = ? AND location_z = ? AND location_world = ? AND placed_by = ?";

        try (Connection connection = dataSource.getConnection();) {
            PreparedStatement stmt = connection.prepareStatement(SQL);
            MachineLocation loc = machine.getLocation();
            stmt.setInt(1, loc.getX());
            stmt.setInt(2, loc.getY());
            stmt.setInt(3, loc.getZ());
            stmt.setString(4, loc.getWorldName());
            stmt.setString(5, machine.getPlacedBy().toString());
            ResultSet resultSet = stmt.executeQuery();

            machine.setId(resultSet.getInt("id"));
            return machine;


        } catch (SQLException error) {
            error.printStackTrace();
        }
        return null;
    }


    /**
     * id
     * machine_type
     * machine_level
     * config_name
     * placed_by
     * machine_fuel
     * location_x
     * location_y
     * location_z
     * location_world
     * tool
     */


    public BaseMachine addMachine(BaseMachine data, MachineType type){
        String SQL = "INSERT INTO " + TABLE_NAME + " (" +
                " placed_by," +
                " machine_size," +
                " fuel," +
                " speed," +
                " tool," +
                " location_x," +
                " location_y," +
                " location_z," +
                " location_world," +
                "config_name," +
                "machine_type) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try(Connection connection = dataSource.getConnection()){
            PreparedStatement statement = connection.prepareStatement(SQL);
            MachineLocation loc = data.getLocation();
            statement.setString(1, data.getPlacedBy().toString());
            statement.setInt(2, data.getSize());
            statement.setInt(3, data.getFuel());
            statement.setInt(4, data.getSpeed());
            statement.setString(5, ItemUtils.encodeItem(data.getTool()));
            statement.setInt(6, loc.getX());
            statement.setInt(7, loc.getY());
            statement.setInt(8, loc.getZ());
            statement.setString(9, loc.getWorldName());
            statement.setString(10, data.getConfig_name());
            statement.setString(11, data.getType().toString());
            statement.execute();
            return getMachine(data);
        }catch (SQLException error){
            error.printStackTrace();
        }
        return null;
    }


    public void updateMachine(BaseMachine data){
        String SQL = "UPDATE " + TABLE_NAME + " SET machine_size = ?, speed = ?, fuel = ?, tool = ? WHERE id = ?";
        try(Connection connection = dataSource.getConnection()){
            PreparedStatement statement = connection.prepareStatement(SQL);
            statement.setInt(1, data.getSize());
            statement.setInt(2, data.getSpeed());
            statement.setInt(3, data.getFuel());
            statement.setString(4, ItemUtils.encodeItem(data.getTool()));
            statement.setInt(5, data.getId());
            statement.execute();
        }catch (SQLException error){
            error.printStackTrace();
        }
    }

    public void removeMachine(BaseMachine data){
        String SQL = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
        try(Connection connection = dataSource.getConnection()){
            PreparedStatement statement = connection.prepareStatement(SQL);
            statement.setInt(1, data.getId());
            statement.execute();
        }catch (SQLException error){
            error.printStackTrace();
        }
    }


    /**
     * id
     * machine_type
     * machine_level
     * config_name
     * placed_by
     * machine_fuel
     * location_x
     * location_y
     * location_z
     * location_world
     * tool
     */

    public List<BaseMachine> getAllMachines(){
        List<BaseMachine> machines = new ArrayList<>();
        ConfigurationSection section = fileManager.getConfiguration("machines").getConfigurationSection("machines");
        String SQL = "SELECT * FROM " + TABLE_NAME;

        try(Connection connection = dataSource.getConnection()){
            PreparedStatement statement = connection.prepareStatement(SQL);
            ResultSet data = statement.executeQuery();
            while(data.next()){
                int id = data.getInt("id");
                MachineType type = MachineType.valueOf(data.getString("machine_type"));
                String config_name = data.getString("config_name");

                int max_speed = section.getInt(config_name + ".max_speed");  // get data from machines.yml
                int max_size = section.getInt(config_name + ".max_size");
                int max_fuel = section.getInt(config_name + ".max_fuel");
                int actual_speed = section.getInt(config_name + ".speeds." + data.getInt("speed"));

                if(actual_speed == 0 || actual_speed < 0){
                    actual_speed = section.getInt(config_name + ".speeds.-1");
                }

                switch(type){
                    case MINER -> {
                        machines.add(new MinerMachine(
                                data.getInt("id"),
                                UUID.fromString(data.getString("placed_by")),
                                new MachineLocation(data.getInt("location_x"),
                                        data.getInt("location_y"),
                                        data.getInt("location_z"),
                                        data.getString("location_world")),
                                data.getInt("machine_size"),
                                max_size,
                                data.getInt("fuel"),
                                max_fuel,
                                data.getInt("speed"),
                                max_speed,
                                ItemUtils.decodeItem(data.getString("tool")),
                                data.getString("config_name"),
                                actual_speed)
                        );
                    }
                    case CRAFTER -> {
                        machines.add(new CrafterMachine(
                                data.getInt("id"),
                                UUID.fromString(data.getString("placed_by")),
                                new MachineLocation(data.getInt("location_x"),
                                        data.getInt("location_y"),
                                        data.getInt("location_z"),
                                        data.getString("location_world")),
                                data.getInt("machine_size"),
                                max_size,
                                data.getInt("fuel"),
                                max_fuel,
                                data.getInt("speed"),
                                max_speed,
                                ItemUtils.decodeItem(data.getString("tool")),
                                data.getString("config_name"),
                                actual_speed)
                        );
                    }
                }


            }
            return machines;
        }catch (SQLException error){
            error.printStackTrace();
        }
        return machines;
    }

}
