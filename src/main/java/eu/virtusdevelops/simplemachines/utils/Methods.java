package eu.virtusdevelops.simplemachines.utils;

import eu.virtusdevelops.virtuscore.compatibility.ServerVersion;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ListIterator;


public class Methods {

    public static boolean isSimilarMaterial(ItemStack is1, ItemStack is2) {
        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ||
                is1.getDurability() == Short.MAX_VALUE || is2.getDurability() == Short.MAX_VALUE) {
            // Durability of Short.MAX_VALUE is used in recipes if the durability should be ignored
            return is1.getType() == is2.getType();
        } else {
            return is1.getType() == is2.getType() && (is1.getDurability() == -1 || is2.getDurability() == -1 || is1.getDurability() == is2.getDurability());
        }
    }


    public static boolean hasSpace(Inventory inventory, ItemStack itemStack) {
        ListIterator var2 = inventory.iterator();

        ItemStack item;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            item = (ItemStack)var2.next();
            if (item == null) {
                return true;
            }
        } while(!item.isSimilar(itemStack) || item.getAmount() + itemStack.getAmount() > item.getMaxStackSize() );

        return true;
    }

}