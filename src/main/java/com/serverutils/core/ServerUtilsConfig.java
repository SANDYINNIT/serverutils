package com.serverutils.core;

// Defines the Forge config values for Server Utils.

import net.minecraftforge.common.ForgeConfigSpec;

public final class ServerUtilsConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.IntValue STARTING_BALANCE = BUILDER
            .comment("Coins a player receives the first time their balance is created.")
            .defineInRange("economy.startingBalance", 100, 0, Integer.MAX_VALUE);
    public static final ForgeConfigSpec.IntValue PAY_TAX_PERCENT = BUILDER
            .comment("Percent removed from /pay transfers.")
            .defineInRange("economy.payTaxPercent", 2, 0, 100);
    public static final ForgeConfigSpec.IntValue SHOP_TAX_PERCENT = BUILDER
            .comment("Percent removed from cash register sales.")
            .defineInRange("economy.shopTaxPercent", 5, 0, 100);

    public static final ForgeConfigSpec.IntValue CLAIM_COST = BUILDER
            .defineInRange("costs.claimCost", 500, 0, Integer.MAX_VALUE);
    public static final ForgeConfigSpec.BooleanValue CLAIM_RENT_ENABLED = BUILDER
            .define("claims.rentEnabled", true);
    public static final ForgeConfigSpec.IntValue CLAIM_RENT_COST = BUILDER
            .defineInRange("claims.rentCost", 100, 0, Integer.MAX_VALUE);
    public static final ForgeConfigSpec.IntValue CLAIM_RENT_INTERVAL_DAYS = BUILDER
            .comment("Minecraft days between rent payments. One Minecraft day is 24000 game ticks.")
            .defineInRange("claims.rentIntervalDays", 7, 1, 365);

    public static final ForgeConfigSpec.IntValue SET_HOME_COST = BUILDER
            .defineInRange("costs.setHomeCost", 50, 0, Integer.MAX_VALUE);
    public static final ForgeConfigSpec.IntValue HOME_COST = BUILDER
            .defineInRange("costs.homeCost", 25, 0, Integer.MAX_VALUE);
    public static final ForgeConfigSpec.IntValue TPA_COST = BUILDER
            .defineInRange("costs.tpaCost", 25, 0, Integer.MAX_VALUE);
    public static final ForgeConfigSpec.IntValue SPAWN_COST = BUILDER
            .defineInRange("costs.spawnCost", 25, 0, Integer.MAX_VALUE);
    public static final ForgeConfigSpec.IntValue BACK_COST = BUILDER
            .defineInRange("costs.backCost", 50, 0, Integer.MAX_VALUE);
    public static final ForgeConfigSpec.IntValue MAX_HOMES = BUILDER
            .defineInRange("teleport.maxHomes", 3, 1, 100);
    public static final ForgeConfigSpec.IntValue TPA_EXPIRE_SECONDS = BUILDER
            .defineInRange("teleport.tpaExpireSeconds", 60, 5, 600);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private ServerUtilsConfig() {
    }
}
