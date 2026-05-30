package com.serverutils.shop;

// Performs validated cash register purchases.

import com.serverutils.costs.Costs;
import com.serverutils.economy.EconomyService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class CashRegisterTransactionService {
    public static final CashRegisterTransactionService INSTANCE = new CashRegisterTransactionService();

    private CashRegisterTransactionService() {
    }

    public boolean buy(ServerPlayer buyer, CashRegisterBlockEntity register, int slot) {
        if (register.owner() == null) {
            buyer.sendSystemMessage(Component.translatable("commands.serverutils.shop.no_owner"));
            return false;
        }
        ItemStack stack = register.getStack(slot);
        if (stack.isEmpty()) {
            buyer.sendSystemMessage(Component.translatable("commands.serverutils.shop.empty"));
            return false;
        }
        int price = register.getPrice(slot);
        var result = EconomyService.INSTANCE.transfer(buyer.server, buyer.getUUID(), register.owner(), price, Costs.INSTANCE.shopTaxPercent());
        if (!result.success()) {
            buyer.sendSystemMessage(Component.translatable(result.messageKey()));
            return false;
        }
        ItemStack bought = register.extract(slot, 1);
        if (!buyer.getInventory().add(bought)) {
            buyer.drop(bought, false);
        }
        register.setChanged();
        buyer.sendSystemMessage(Component.translatable("commands.serverutils.shop.bought", bought.getHoverName(), price, result.tax()));
        return true;
    }
}
