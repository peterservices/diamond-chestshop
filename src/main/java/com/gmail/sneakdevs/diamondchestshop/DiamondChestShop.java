package com.gmail.sneakdevs.diamondchestshop;

import com.gmail.sneakdevs.diamondchestshop.config.DiamondChestShopConfig;
import com.gmail.sneakdevs.diamondchestshop.sql.ChestshopDatabaseManager;
import com.gmail.sneakdevs.diamondchestshop.sql.ChestshopSQLiteDatabaseManager;
import com.gmail.sneakdevs.diamondeconomy.DiamondUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;

public class DiamondChestShop implements ModInitializer {
    public static final String MODID = "diamondschestshop";

    public static ChestshopDatabaseManager getDatabaseManager() {
        return new ChestshopSQLiteDatabaseManager();
    }

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> ChestshopCommand.register(dispatcher));
        DiamondUtils.registerTable("CREATE TABLE IF NOT EXISTS chestshop (id integer PRIMARY KEY AUTOINCREMENT, item text NOT NULL, nbt text NOT NULL);");
        AutoConfig.register(DiamondChestShopConfig.class, JanksonConfigSerializer::new);
    }

    public static String signTextToReadable(String text) {
        return text.replaceAll("[\\D]", "").toLowerCase();
    }

    public static DataComponentMap getComponents(ServerLevel server, String text) {
        RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, server.registryAccess());
        DataComponentMap components = DataComponentMap.CODEC.parse(registryOps, JsonParser.parseString(text)).result().orElse(DataComponentMap.EMPTY);
        return components;
    }

    public static boolean equalComponents(DataComponentMap mapOne, DataComponentMap mapTwo) {
        if (!mapOne.keySet().equals(mapTwo.keySet())) {
            return false;
        }
        for (DataComponentType<?> type : mapOne.keySet()) {
            if (!mapOne.get(type).equals(mapTwo.get(type))) {
                return false;
            }
        }
        return true;
    }
}