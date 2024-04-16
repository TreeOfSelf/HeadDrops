package com.github.evancolewright.headdrops.model;

import com.github.evancolewright.headdrops.utilities.HeadUtils;
import de.tr7zw.changeme.nbtapi.NBTItem;
import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Getter
public final class PlayerHeadData
{
    private final UUID owningPlayerUUID;
    private final String displayName;
    private final List<String> lore;
    private final Location location;

    public PlayerHeadData(UUID owningPlayerUUID, String displayName, List<String> lore, Location location)
    {
        this.owningPlayerUUID = owningPlayerUUID;
        this.displayName = displayName;
        this.lore = lore;
        this.location = location;
    }

    public static PlayerHeadData fromPlacedHead(ItemStack itemStack, Location location)
    {
        ItemMeta itemMeta = itemStack.getItemMeta();
        NBTItem item = new NBTItem(itemStack);
        NamespacedKey key = new NamespacedKey("head-drop", "headdrop-user");
        String UUIDVal = "";
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        if(container.has(key , PersistentDataType.STRING)) {
            UUIDVal= container.get(key, PersistentDataType.STRING);
        }else{
            UUIDVal = item.getString("HeadDrops_Owner");
        }

        return new PlayerHeadData(UUID.fromString(UUIDVal),
                itemMeta.getDisplayName(),
                itemMeta.getLore(),
                location
        );
    }

    public ItemStack toItemStack()
    {
        return HeadUtils.createPlayerHead(this.owningPlayerUUID, null, this.displayName, this.lore);
    }
}
