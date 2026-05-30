package com.serverutils.costs;

// Centralizes configurable prices and fee settings.

import com.serverutils.core.ServerUtilsConfig;

public final class CostService {
    public int claimCost() {
        return ServerUtilsConfig.CLAIM_COST.get();
    }

    public boolean rentEnabled() {
        return ServerUtilsConfig.CLAIM_RENT_ENABLED.get();
    }

    public int claimRentCost() {
        return ServerUtilsConfig.CLAIM_RENT_COST.get();
    }

    public long rentIntervalTicks() {
        return ServerUtilsConfig.CLAIM_RENT_INTERVAL_DAYS.get() * 24000L;
    }

    public int setHomeCost() {
        return ServerUtilsConfig.SET_HOME_COST.get();
    }

    public int homeCost() {
        return ServerUtilsConfig.HOME_COST.get();
    }

    public int tpaCost() {
        return ServerUtilsConfig.TPA_COST.get();
    }

    public int spawnCost() {
        return ServerUtilsConfig.SPAWN_COST.get();
    }

    public int backCost() {
        return ServerUtilsConfig.BACK_COST.get();
    }

    public int payTaxPercent() {
        return ServerUtilsConfig.PAY_TAX_PERCENT.get();
    }

    public int shopTaxPercent() {
        return ServerUtilsConfig.SHOP_TAX_PERCENT.get();
    }

    public int maxHomes() {
        return ServerUtilsConfig.MAX_HOMES.get();
    }

    public long tpaExpireTicks() {
        return ServerUtilsConfig.TPA_EXPIRE_SECONDS.get() * 20L;
    }
}
