package com.github.evancolewright.headdrops.utilities;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class HeadUtils
{


    public static String[] getPlayerTexture(UUID playerUUID) throws IOException {
        URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + playerUUID.toString());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);

        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder jsonResponse = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonResponse.append(line);
            }
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = null;
            try {
                jsonObject = (JSONObject) parser.parse(jsonResponse.toString());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            JSONArray properties = (JSONArray) jsonObject.get("properties");
            String displayName = (String) jsonObject.get("name");


            for (Object property : properties) {
                JSONObject propObject = (JSONObject) property;
                String name = (String) propObject.get("name");
                String value = (String) propObject.get("value");

                if (name.equals("textures")) {
                    byte[] decodedBytes = Base64.getDecoder().decode(value);
                    String jsonString = new String(decodedBytes, StandardCharsets.UTF_8);
                    JSONParser parserTwo = new JSONParser();
                    JSONObject jsonObjectTwo = null;
                    try {
                        jsonObjectTwo = (JSONObject) parserTwo.parse(jsonString);
                    } catch (ParseException e) {
                        return null;
                    }
                    JSONObject texturesObject = (JSONObject) jsonObjectTwo.get("textures");
                    JSONObject skinObject = (JSONObject) texturesObject.get("SKIN");
                    String skinURL = (String) skinObject.get("url");
                    String[] result = new String[2];
                    result[0] = displayName;
                    result[1] = skinURL;
                    return result;
                }
            }
        }

        return null;
    }

    public static ItemStack createPlayerHead(UUID playerUUID, Player killer, String displayName, List<String> lore, PlayerProfile blockOwner, boolean silkTouched)
    {
        ItemStack playerHead = getVersionIndependentHead();
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();

        if (!silkTouched) {
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
            try {
                String[] results = getPlayerTexture(playerUUID);
                if (results != null) {
                    displayName = "&a&l" + results[0] + "'s &7&lHead";
                    URL url = new URL(results[1]);
                    PlayerTextures texture = profile.getTextures();
                    texture.setSkin(url);
                    skullMeta.setOwnerProfile(profile);
                }else{
                    if (blockOwner != null) {
                        skullMeta.setOwnerProfile(blockOwner);
                    }
                }
            } catch (IOException e) {
                if (blockOwner != null) {
                    skullMeta.setOwnerProfile(blockOwner);
                }
            }
        }else{
            if (blockOwner!=null) {
                skullMeta.setOwnerProfile(blockOwner);
            }
        }

        skullMeta.setDisplayName(replacePlaceHolders(displayName, playerUUID, killer));
        skullMeta.setLore(lore.stream()
                .map(s -> replacePlaceHolders(s, playerUUID, killer))
                .collect(Collectors.toList()));

        NamespacedKey key = new NamespacedKey("head-drop", "headdrop-user");
        skullMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, playerUUID.toString());
        playerHead.setItemMeta(skullMeta);

        return playerHead;
    }

    private static String replacePlaceHolders(String string, UUID playerUUID, Player killer)
    {
        string = ChatColor.translateAlternateColorCodes('&', string.replace("{PLAYER}", Bukkit.getOfflinePlayer(playerUUID).getName())
                .replace("{TIMESTAMP}", new SimpleDateFormat("MMM dd, yyy")
                        .format(new Date(System.currentTimeMillis()))));
        if (killer == null)
            return string;

        ItemStack murderWeapon = killer.getInventory().getItemInHand();
        String murderWeaponName;

        if (murderWeapon.getType() == Material.AIR)
            murderWeaponName = "Fist";
        else if (!murderWeapon.hasItemMeta() || murderWeapon.getItemMeta().getDisplayName().equals(""))
            murderWeaponName = StringUtils.capitalizeMultiWordMaterialString(murderWeapon.getType().toString().toLowerCase(), "_");
        else
            murderWeaponName = murderWeapon.getItemMeta().getDisplayName();
        string = string.replace("{KILLER}", killer.getName())
                .replace("{MURDER_WEAPON}", murderWeaponName);

        return string;
    }

    private static ItemStack getVersionIndependentHead()
    {
        Material material = Material.getMaterial("PLAYER_HEAD");
        if (material == null)
            return new ItemStack(Material.getMaterial("SKULL_ITEM"), 1, (byte) 3);
        return new ItemStack(material);
    }
}
