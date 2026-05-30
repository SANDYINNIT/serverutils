package com.serverutils.claims;

// Processes simple claim rent without deleting claims.

import com.serverutils.costs.Costs;
import com.serverutils.economy.EconomyService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;

import java.util.UUID;

public final class ClaimRentService {
    private static int tickCounter;

    public static void processLogin(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            processPlayer(serverPlayer);
        }
    }

    public static void processServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !Costs.INSTANCE.rentEnabled()) {
            return;
        }
        if (++tickCounter < 1200) {
            return;
        }
        tickCounter = 0;
        event.getServer().getPlayerList().getPlayers().forEach(ClaimRentService::processPlayer);
    }

    private static void processPlayer(ServerPlayer player) {
        if (!Costs.INSTANCE.rentEnabled()) {
            return;
        }
        ClaimSavedData data = ClaimSavedData.get(player.server);
        long now = player.server.overworld().getGameTime();
        UUID owner = player.getUUID();
        data.claims().forEach((key, claim) -> {
            if (!claim.owner().equals(owner) || now < claim.nextRentDueGameTime()) {
                return;
            }
            if (EconomyService.INSTANCE.withdraw(player.server, owner, Costs.INSTANCE.claimRentCost())) {
                claim.setUnpaid(false);
                claim.setNextRentDueGameTime(now + Costs.INSTANCE.rentIntervalTicks());
            } else {
                claim.setUnpaid(true);
            }
            data.dirty();
        });
    }

    private ClaimRentService() {
    }
}
