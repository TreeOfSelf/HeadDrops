package com.github.evancolewright.headdrops.api;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class HeadDropsAPI
{
    /**
     * Checks whether an {@link org.bukkit.inventory.ItemStack} is a player head
     * that is created by the HeadDrops plugin.
     *
     * @param itemStack the item to check
     * @return true if the iteStack is a player head, false otherwise
     */
    public static boolean isPlayerHead(ItemStack itemStack)
    {
        if (itemStack == null) return false;
        if (itemStack.getType() == Material.AIR) return false;


        NamespacedKey key = new NamespacedKey("head-drop", "headdrop-user");
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();

        NBTItem nbtItem = new NBTItem(itemStack);

        if(container.has(key , PersistentDataType.STRING) || (nbtItem.hasNBTData() && nbtItem.hasCustomNbtData() && nbtItem.getString("HeadDrops_Owner")!=null) ) {

            return true;
        }else{
            return false;
        }

    }
}
