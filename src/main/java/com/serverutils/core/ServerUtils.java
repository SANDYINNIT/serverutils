package com.serverutils.core;

// Main Forge entrypoint for the Server Utils mod.

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(ServerUtils.MODID)
public final class ServerUtils {
    public static final String MODID = "serverutils";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ServerUtils(FMLJavaModLoadingContext context) {
        var modBus = context.getModEventBus();

        ModRegistries.register(modBus);
        context.registerConfig(ModConfig.Type.COMMON, ServerUtilsConfig.SPEC);

        MinecraftForge.EVENT_BUS.register(ServerEvents.class);

        LOGGER.info("Server Utils loaded for Forge 1.21.1");
    }
}
