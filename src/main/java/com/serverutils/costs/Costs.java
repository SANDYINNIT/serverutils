package com.serverutils.costs;

// Exposes the shared cost service singleton.

public final class Costs {
    public static final CostService INSTANCE = new CostService();

    private Costs() {
    }
}
